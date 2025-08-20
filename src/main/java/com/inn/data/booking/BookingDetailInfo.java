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
public class BookingDetailInfo {

    private boolean isMember; // 비회원 여부
    private boolean canCancel;    // 예약 취소 가능 여부

    // 예약 정보
    private String merchantUid;
    private LocalDate checkin;
    private LocalDate checkout;
    private String checkinDay;
    private String checkoutDay;
    private String status;

    // 예약자 정보 (결제 관련)
    private String buyerName;
    private String buyerTel;

    // 호텔 정보
    private String hotelName;
    private String hotelImage;
    private String hotelAddress;

    // 객실 정보
    private String roomName;
    private Long roomNumber;
    private Long roomPrice;
    private Long capacity;
    private String description;

    // 결제 정보
    private String payMethod;
    private Long paidAmount;
    private LocalDateTime createdAt; // 결제일시

}
