package com.inn.data.room;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Room")
public class RoomEntity {
	
	
	@Id
	private Integer r_idx;
	
	@Column(nullable = false)
	private Integer h_idx;
	
	@Column(nullable = false)
	private Integer r_price;
	
	private Integer r_min;
	private Integer r_max;

}
