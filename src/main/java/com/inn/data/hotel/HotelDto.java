package com.inn.data.hotel;

import java.util.ArrayList;

import lombok.Data;

@Data
public class HotelDto {
	
	private Integer h_idx;
	private Integer m_idx;
	private String h_name;
	private Integer h_empty;
	private ArrayList<String> h_images;



}
