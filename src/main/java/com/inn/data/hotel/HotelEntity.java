package com.inn.data.hotel;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.JoinColumn;

@Data
@Entity
@Table(name = "hotel")
public class HotelEntity {
	
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long idx;
	
	@Column(nullable = false)
	private Long memberIdx;
	@Column(nullable = false)
	private String hotelName;
	
	
	
	@ElementCollection
	@CollectionTable(name = "hotelImages", joinColumns = @JoinColumn(name = "hotel_idx"))
	@Column(name = "hotelImages")
	private List<String> hotelImages;
	
	
	
	@Column(nullable = false)
	private String hotelAddress;
	
	@Column(nullable = false)
	private String hotelTel;
	
	@Column(nullable = true) //추후 수정
	private String hotelCategory;
	
	@ElementCollection
	@CollectionTable(name = "hotelTag", joinColumns = @JoinColumn(name = "hotel_idx"))
	@Column(name = "hotelTag")
	private List<String> hotelTag;
	

}