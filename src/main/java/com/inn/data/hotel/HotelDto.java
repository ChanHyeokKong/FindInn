package com.inn.data.hotel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class HotelDto {
	
	private Integer hotelIdx;
	private Integer memberIdx;
	private String hotelName;
	private Integer hotelEmpty;
	private List<String> hotelImages;

	 public HotelDto(Integer hotelIdx, String hotelName, Integer hotelEmpty,
			 List<String> hotelImages,Integer memberIdx) {
	        this.hotelIdx = hotelIdx;
	        this.hotelName = hotelName;
	        this.hotelEmpty = hotelEmpty;
	        this.hotelImages = hotelImages;
	        this.memberIdx= memberIdx;
	    }	

}
