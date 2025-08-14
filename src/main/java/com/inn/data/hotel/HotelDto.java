package com.inn.data.hotel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Lob;
import lombok.Data;

@Data
public class HotelDto {
	
	private Long idx;
	private Long memberIdx;
	private String hotelName;	
	private List<String> hotelImages;
	private String hotelAddress;
	private String hotelTel;
	private String hotelCategory;
	private List<String> hotelTag;
	private Integer priceRange;


	 public HotelDto(Long idx, String hotelName,
                    List<String> hotelImages, Long memberIdx) {
        this.idx = idx;
        this.hotelName = hotelName;
        this.hotelImages = hotelImages;
        this.memberIdx = memberIdx;
    }

	public HotelDto() {
	}
}
