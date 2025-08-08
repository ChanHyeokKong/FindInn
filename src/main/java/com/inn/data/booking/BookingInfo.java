package com.inn.data.booking;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BookingInfo {
    private String merchantUid;
    private String roomIdx;
    private String checkin;
    private String checkout;
    private String guestPhone;
}
