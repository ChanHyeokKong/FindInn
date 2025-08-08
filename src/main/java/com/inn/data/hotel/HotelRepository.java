package com.inn.data.hotel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<HotelEntity, Long>, JpaSpecificationExecutor<HotelEntity> {

	List<HotelEntity> findByHotelNameContainingAndHotelCategory(String keyword, String category);	 
	List<HotelEntity> findByHotelNameContaining(String keyword);
	
	@Query(value = """
		    SELECT hotel_idx
		    FROM hotel_tag
		    WHERE hotel_tag IN (:tags)
		    GROUP BY hotel_idx
		    HAVING COUNT(DISTINCT hotel_tag) = :tagCount
		""", nativeQuery = true)
		List<Long> findHotelIdxByAllTags(@Param("tags") List<String> tags, @Param("tagCount") int tagCount);

	 
	 
	 
	 

}
