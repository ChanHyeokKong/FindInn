package com.inn.data.booking;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class BookingDto {

    private Long idx;
    private String merchantUid;
    private Long roomIdx;
    private Long memberIdx;
    private LocalDate checkin;
    private LocalDate checkout;
    private Long price;
    private String status;
    private LocalDateTime canceledAt;

}
