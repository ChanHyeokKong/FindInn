package com.inn.data.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCompleteInfo {
    private String merchantUid;
    private LocalDate checkin;
    private LocalDate checkout;
    private String checkinDay;
    private String checkoutDay;
    private String roomName;
    private Long roomNumber;
    private Long paidAmount;
}
