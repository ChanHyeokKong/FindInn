package com.inn.service;

import com.inn.data.booking.BookingDto;
import com.inn.data.booking.BookingEntity;

import java.time.LocalDate;

public interface BookingService {

    String generateMerchantUid();

    boolean isOverlappingBookingExists(Long roomId, LocalDate checkin, LocalDate checkout);

    BookingEntity insert(BookingDto dto);

    BookingEntity updateStatusToCanceled(Long id);

    // 기타 예약 관련 메서드들 선언...
}
