package com.inn.data.member.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomTypeSummaryDto {

    private Long hotelId;
    private String hotelName;
    private String memberName;
    private String roomTypeName;
    private String roomTypeDescription;
    private Long roomTypeCapacity;
    private Long roomCount;



}
