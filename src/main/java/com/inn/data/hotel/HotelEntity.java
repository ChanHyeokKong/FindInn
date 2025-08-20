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
			mappedBy = "hotel",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<RoomTypes> roomTypes = new ArrayList<>();

	@OneToOne(
			mappedBy = "hotel",
			cascade = CascadeType.ALL
	)
	private TagEntity tag;

	public void addRoomType(RoomTypes roomType) {
		roomTypes.add(roomType);
		roomType.setHotel(this); // Set the parent (this Hotel instance) on the child
	}


}