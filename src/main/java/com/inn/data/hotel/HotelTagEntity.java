/*package com.inn.data.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "hotelTag")
public class HotelTagEntity {
	
	@Id
	@ManyToOne(fetch = FetchType.LAZY)  // 여러 태그가 하나 호텔에 속할 때
    @JoinColumn(name = "hotel_idx")
	private HotelEntity hotel;
	
	
	
    @Column(name = "hotel_tag")
    private String hotelTag;

}*/
