package com.inn.data.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingSmsInfo {
    private String merchantUid;
    private String hotelName;
    private String roomName;
    private String checkin;
    private String checkout;
    private String checkinDay;
    private String checkoutDay;
    private String guestPhone;
}
