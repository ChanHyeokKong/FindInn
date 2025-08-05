package com.inn.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.hotel.HotelWithManagerDto;

@Service
public class HotelService {

	@Autowired
	private HotelRepository hotelRepository;

	//전체 데이터
	public List<HotelEntity> getAllHotelData() {

		return hotelRepository.findAll();		
	}
	
	public List<HotelDto> getHotelData(String keyword){

		List<HotelEntity> hotels = hotelRepository.findByhotelNameContaining(keyword);

		//List<String> list=new ArrayList<>(Array.asList(hotels));
		
		
	    // Entity → DTO 변환
		return hotels.stream()
			    .map(hotel -> new HotelDto(
			        hotel.getHotelIdx(),
			        hotel.getHotelName(),
			        hotel.getHotelEmpty(),
			        hotel.getHotelImages(),  // List<String>
			        hotel.getMemberIdx()
			    ))
			    .collect(Collectors.toList());
	}
	
	public List<HotelWithManagerDto> GetAllForAdmin(){
		return hotelRepository.findAllWithManagerName();
	}
	
	
	
	
	
	
	
	
}
