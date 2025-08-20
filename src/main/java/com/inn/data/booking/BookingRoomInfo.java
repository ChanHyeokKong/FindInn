package com.inn.data.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRoomInfo {
    private Long hotelIdx;
    private String hotelImage;
    private String hotelName;
    private String roomName;
    private Long roomNumber;
    private Long roomPrice;
    private Long capacity;      // 수용 인원
    private String description; // 객실 설명
}
