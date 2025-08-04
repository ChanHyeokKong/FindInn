package com.inn.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;

@Service
public class HotelService {

	@Autowired
	private HotelRepository hotelRepository;
	
	
	
	//전체 데이터
	public List<HotelEntity> getAllHotelData() {

		return hotelRepository.findAll();		
	}
	
	public List<HotelEntity> getHotelData(String name){
		
		//return hotelRepository.findAllByH_name(name);
		List<HotelEntity> hotelEntities = new ArrayList<>();
		return hotelEntities;
	}
	
	
	
	
	
	
	
	
	
	
}
