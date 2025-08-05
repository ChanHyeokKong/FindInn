package com.inn.data.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelWithManagerDto {
    private HotelEntity hotelEntity;
    private String memberName;
}
