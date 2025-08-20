package com.inn.service;

import com.inn.data.booking.*;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingRoomInfo getBookingRoomInfo(Long roomIdx);

    String getKoreanShortDayOfWeek(LocalDate date);

    String generateMerchantUid();

    boolean isOverlappingBookingExists(Long roomIdx, LocalDate checkin, LocalDate checkout);

    BookingEntity insert(BookingDto dto);

    BookingEntity updateStatusToCanceled(Long idx);

    BookingCompleteInfo getBookingCompleteInfo(Long bookingIdx);

    List<BookingListInfo> getBookingsByStatus(Long memberIdx, String status);

    BookingDetailInfo getBookingDetailInfo(String merchantUid);

    // 기타 예약 관련 메서드들 선언...

}
