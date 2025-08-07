package com.inn.data.reserve;

import lombok.Data;

import java.time.LocalDate;
@Data
public class ReserveForm {
    private Long userId;
    private Long hotelId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String method;
    private Long price;
    private Long roomTypeId;
    private int person;
}