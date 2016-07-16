package com.news.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.news.dto.NewsDTO;
import com.news.models.NewsDao;



/**
 * A class to test interactions with the MySQL database using the UserDao class.
 *
 * @author VILS
 */
@RestController
public class NewsController {
	
	 @Autowired private NewsDao newsDao;

	private Map<String, Set<Long>> contentMap = new HashMap<String, Set<Long>>();
	 
	 // REST path

  private static final String NEWS = "/news";
  private static final String NEWS_BY_ID =  NEWS + "/{id}";
  private static final String NEWS_BY_CATEGORY = "/cat/{category}";
  private static final String NEWS_BY_TITLE = "/tit/{title}";
  private static final String NEWS_BY_CONTENT = "/con/{context}";
  
  @PostConstruct
  public void init() { // выполняется первым 
	  Iterable<NewsDTO> news = newsDao.findAll();
	  for(NewsDTO s:news){
		  String context = s.getContent();
		  if (context != null) putContext(context, s.getId());
	  }
	  System.out.println (" **** Create Hash Map: " + contentMap.size());
  }
	
   private void putContext(String context, Long id) {
	   String[] cxt = context.trim().split(" "); //разбивает на слова
	   for (int i = 0; i < cxt.length; i++){
		   if (cxt[i].length()>2) { //предлоги не смотрим
			   Set<Long> set = contentMap.get(cxt[i]);
			   if (set == null) {
				   set = new HashSet<Long>();
			   }
			   set.add(id);
			   contentMap.put(cxt[i], set);
			//   System.out.println (" **** Hash Map: " + contentMap.size());
			// для того что бы убедиться, что contentMap увеличивается можно разкоментить строку выше.			   
		   }
	   }
  }

@RequestMapping(value = NEWS, method = RequestMethod.POST)
    public NewsDTO addCustomer(@RequestBody NewsDTO news) {
	   System.out.print("Title: " + news.getTitle());
	   news.setPublished(new Date().toString());
	   String context = news.getContent();
	if (context != null) putContext(context, news.getId());
       return newsDao.save(news);
    }
   
   @RequestMapping(value = NEWS, method = RequestMethod.GET)
   public List<NewsDTO> getAllNews() {
	   Iterable<NewsDTO> list = newsDao.findAll();
	   List<NewsDTO> result = new ArrayList<NewsDTO>();
	   for(NewsDTO l:list) result.add(l);
	   return result;
   }
	
  @RequestMapping(value = NEWS_BY_ID, method = RequestMethod.GET)
  public NewsDTO getById(@PathVariable  Long id) {
	return newsDao.findOne(id);
  }
  
// тут начинается поиск по контенту с приоритетом вхождений слов
  @RequestMapping(value = NEWS_BY_CONTENT, method = RequestMethod.GET)
  public List<NewsDTO> getByContext(@PathVariable  String context) {
   Map<Long, Integer> rating = new HashMap<Long, Integer>();
    
   String[] cxt = context.trim().split(" "); // создаем массив слов, разделитель пробел
   
    for (int i = 0; i < cxt.length; i++) {
     if (cxt[i].length()>2) { // берем слова длиньше 2 символов
      Set<Long> set = contentMap.get(cxt[i]);
      addRating(set, rating); //
      }
    }
    
    Map<Long, Integer> rate = sortByValues(rating);
    
    List<NewsDTO> result = new ArrayList<NewsDTO>();
    for(Long id:rate.keySet()) result.add(newsDao.findOne(id));
    return result;
  }
  
  // V extends Comparable<V> - чтобы можно было сортировать
  public  <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
     Comparator<K> valueComparator =  new Comparator<K>() {
         public int compare(K k1, K k2) {
             int compare = map.get(k2).compareTo(map.get(k1)); // если равны 0 если меньше -1 если больше 1
             if (compare == 0) return 1;
             else return compare;
         }
     };
     Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
     sortedByValues.putAll(map);
     return sortedByValues;
 }

  private void addRating(Set<Long> set, Map<Long, Integer> rating) { // добавляет рейтинг словам, те которые в первый раз встретились получают рейтинг 1.
     for(Long r:set){
    	 Integer i = rating.get(r);
    	 if (i==null) i =0; // это слово еще не встречалось поэтому у него будет рейтинг 0
    	 i++;
    	 rating.put(r, i);
     }
 }

@RequestMapping(value = NEWS_BY_CATEGORY, method = RequestMethod.GET)
  public List<NewsDTO> getByCategory(@PathVariable  String category) {
	  return newsDao.findByCategory(category);
  }
  
  @RequestMapping(value = NEWS_BY_TITLE, method = RequestMethod.GET)
  public List<NewsDTO> getByTitle(@PathVariable  String title) {
	  return newsDao.findByTitle(title);
  }
  
  @RequestMapping(value = NEWS_BY_ID, method = RequestMethod.DELETE)
  public String delete(@PathVariable  Long id) {
	  try {
		  NewsDTO news = new NewsDTO(id);
		  newsDao.delete(news);
	  }
	  catch (Exception ex) {
		  return "Error deleting the news: " + ex.toString();
	  }
	  return id + ":Ok";
  }
  
  @RequestMapping(value = NEWS_BY_ID, method = RequestMethod.PUT)
  public long updateNews(@PathVariable  Long id, @RequestBody NewsDTO news) {
	  NewsDTO n = newsDao.findOne(id);
	  n.setCategory(news.getCategory());
	  n.setContent(news.getContent());
	//  n.setPublished(news.getPublished());
	  n.setPublished(new Date().toString());
	  n.setTitle(news.getTitle());
	  newsDao.save(n);
	  String context = n.getContent();
	  if (context != null) putContext(context, n.getId());
	return id;
	  
  }
  
} 