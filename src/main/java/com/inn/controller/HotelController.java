package com.inn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.inn.data.hotel.HotelDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.service.HotelService;

import lombok.Value;

@Controller
public class HotelController {
	
	@Autowired
    private HotelService hotelService;
	
	
	
	@org.springframework.beans.factory.annotation.Value("${kakao.map.javascript-key}")
	private String kakaoJsKey;
	
	
	@GetMapping("/h_list")
	public String hotelList (Model model) {
		List<HotelEntity> hotels;
		
		
		
			hotels = hotelService.getAllHotelData();
			
		
		model.addAttribute("kakaoJsKey",kakaoJsKey);
		model.addAttribute("hotels", hotels);	
		return "/hotel/hotelList";
	}
	
	@GetMapping("/h_search")
	@ResponseBody
	public List<HotelDto> getHotelData(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(defaultValue = "all", value = "category", required = false) String hotelCategory) {
	
		
		return hotelService.getHotelDataByKeywordAndCategory(keyword,hotelCategory);
	}
	
	
	
	
	
	

	
	
	
	
	

}
