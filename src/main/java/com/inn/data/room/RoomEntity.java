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
	private Integer roomIdx;
	
	@Column(nullable = false)
	private Integer hotelIdx;
	
	@Column(nullable = false)
	private Integer roomPrice;
	
	private Integer roomMin;
	private Integer roomMax;

}
