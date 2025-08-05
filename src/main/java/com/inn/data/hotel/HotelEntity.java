package com.inn.data.hotel;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.JoinColumn;

@Data
@Entity
@Table(name = "hotel")
public class HotelEntity {
	
	
	@Id
	private Integer hotelIdx;
	
	@Column(nullable = false)
	private Integer memberIdx;
	@Column(nullable = false)
	private String hotelName;
	private Integer hotelEmpty;
	
	@ElementCollection
	@CollectionTable(name = "hotelImages", joinColumns = @JoinColumn(name = "hotelIdx"))
	@Column(name = "hotelImages")
	private List<String> hotelImages;

}
