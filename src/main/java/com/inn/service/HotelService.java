package com.inn.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;

@Service
public class 	HotelService {

	@Autowired
	private HotelRepository hotelRepository;

	//전체 데이터
	public List<HotelEntity> getAllHotelData() {

		return hotelRepository.findAll();		
	}
	
	public List<HotelDto> getHotelDataByKeywordAndCategory(String keyword, String category){

		List<HotelEntity> hotels;
		if("all".equals(category)) {
			hotels = hotelRepository.findByHotelNameContaining(keyword);
		}else
		{	hotels = hotelRepository.findByHotelNameContainingAndHotelCategory(keyword, category);


		}
		

		//List<String> list=new ArrayList<>(Array.asList(hotels));
		
		
		
	    // Entity → DTO 변환
		return hotels.stream()
			    .map(hotel -> new HotelDto(
			        hotel.getIdx(),
			        hotel.getHotelName(),
			        hotel.getHotelImages(),  // List<String>
			        hotel.getMemberIdx()
			    ))
			    .collect(Collectors.toList());
	}
	
	public Optional<HotelEntity> getHotelDataById(long idx) {
		return hotelRepository.findById(idx);
	}


}
	
	

	  
	 

	
	
	
	
	
	
	
	

