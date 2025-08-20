package com.inn.data.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {

    private Long idx;
    private String merchantUid;
    private Long roomIdx;
    private Long memberIdx;
    private Long couponIdx;
    private LocalDate checkin;
    private LocalDate checkout;
    private Long price;
    private String status;
    private LocalDateTime canceledAt;

}
