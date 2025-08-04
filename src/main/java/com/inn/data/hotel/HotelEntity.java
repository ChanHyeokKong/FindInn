package com.inn.data.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "hotel")
public class HotelEntity {
	
	
	@Id
	private Integer h_idx;
	
	@Column(nullable = false)
	private Integer m_idx;
	@Column(nullable = false, name = "h_name")
	private String hName;
	private Integer h_empty;
	private String h_images;

}
