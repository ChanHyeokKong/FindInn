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
import com.inn.data.hotel.HotelSearchCondition;
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
	public List<HotelDto> searchHotels(
	        @RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam(value = "category", required = false) String category,
	        @RequestParam(value = "tags", required = false) List<String> tags
	    ) {
		
		System.out.println("tags = " + tags);
		
	        HotelSearchCondition condition = new HotelSearchCondition();
	        condition.setKeyword(keyword);
	        condition.setCategory(category);
	        condition.setTags(tags);

	        return hotelService.searchHotels(condition);
	}
	
	
	
	
	
	

	
	
	
	
	

}
