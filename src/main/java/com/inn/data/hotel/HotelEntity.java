package com.inn.data.hotel;

import com.inn.data.rooms.RoomTypes;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

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

	private Long status;

	@Lob
	private String description;
	
	@Transient
	private Integer priceRange;
	
	@Column(nullable = false)
	private String hotelImage;

	@OneToMany(
			mappedBy = "hotel", // **IMPORTANT**: Tells JPA to look at the "hotel" field in the RoomTypes class to find the foreign key configuration.
			cascade = CascadeType.ALL, // Ensures that if you save a hotel, its rooms are also saved. If you delete a hotel, its rooms are also deleted.
			orphanRemoval = true // Removes RoomTypes from the database if they are removed from this list.
	)
	private List<RoomTypes> roomTypes = new ArrayList<>();

}