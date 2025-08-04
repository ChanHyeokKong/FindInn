package com.inn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inn.data.hotel.HotelEntity;
import com.inn.service.HotelService;

@Controller
public class HotelController {
	
	@Autowired
    private HotelService hotelService;
	
	
	@GetMapping("/h_list")
	public String hotelList(Model model) {
		List<HotelEntity> hotelList=hotelService.getAllHotelData();
		model.addAttribute("hotels", hotelList);	
		return "/hotel/hotelList";
	}
	
	@GetMapping("/h_search")
	public String hotelSearch(@RequestParam String name) {
		
		hotelService.getHotelData(name);
		
		return "/hotel/hotelList";
	}

	
	
	
	
	

}
