package com.news.models;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

import com.news.dto.NewsDTO;

/**
 * A DAO for the entity News is simply created by extending the CrudRepository
 * interface provided by spring. The following methods are some of the ones
 * available from such interface: save, delete, deleteAll, findOne and findAll.
 * The magic is that such methods must not be implemented, and moreover it is
 * possible create new query methods working only by defining their signature!
 * 
 * @author VILS
 */
@Transactional
public interface NewsDao extends CrudRepository<NewsDTO, Long> {

	public List<NewsDTO> findByCategory(String category);

	public List<NewsDTO> findByTitle(String title);

}
