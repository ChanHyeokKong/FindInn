package com.inn.data.member.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomTypeSummaryDto {

    private Integer hotelId;
    private String hotelName;
    private String memberName;
    private String roomTypeName;
    private String roomTypeDescription;
    private long roomTypeCapacity;
    private Long roomCount;



}
