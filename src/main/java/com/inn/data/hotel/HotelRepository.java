package com.inn.data.hotel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface HotelRepository extends JpaRepository<HotelEntity, Integer> {
	
	
	
	List<HotelEntity> findAllByHName(String name);
	
	
	
    
    
    
}
