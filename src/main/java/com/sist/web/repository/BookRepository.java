package com.sist.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sist.web.entity.WikiBook;


@Repository
public interface BookRepository extends JpaRepository<WikiBook, String> {
	@Query(value = """
		SELECT * FROM wikibook 
		ORDER BY RAND() 
		LIMIT 1
		""", nativeQuery = true)
	Optional<WikiBook> mainBookData();
	
}
