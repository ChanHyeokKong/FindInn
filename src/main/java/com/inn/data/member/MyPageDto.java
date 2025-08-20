package com.inn.data.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {

    String hotelName;
    String hotelAddress;
    Long hotelIdx;
    String roomName;
    Long roomNumber;
    LocalDate checkIn;
    LocalDate checkOut;
    Long reserveIdx;
    String memberName;
    String status;

}
