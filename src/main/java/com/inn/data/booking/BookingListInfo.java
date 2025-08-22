package com.inn.data.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingListInfo {

    // 예약 정보
    private Long bookingIdx;
    private String merchantUid;
    private String status;

    // 호텔 정보
    private String hotelName;
    private String hotelImage;

    // 객실 정보
    private String roomName;
    private Long roomNumber;

}
