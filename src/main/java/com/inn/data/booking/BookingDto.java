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

    private Integer id;
    private String merchantUid;
    private Integer roomId;
    private Integer memberId;
    private LocalDate checkin;
    private LocalDate checkout;
    private Integer price;
    private String status;
    private LocalDateTime canceledAt;

}
