package com.inn.service;

import com.inn.data.booking.BookingDto;
import com.inn.data.booking.BookingEntity;
import com.inn.data.booking.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 현재 시간(밀리초) + 랜덤 6자리 영문 대문자 조합으로 고유 merchantUid 생성
     */
    public String generateMerchantUid() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis()); // 현재 시간 밀리초

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int idx = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * 같은 객실의 동일한 기간에 예약이 이미 존재하는지 확인
     */
    public boolean isOverlappingBookingExists(int roomId, LocalDate checkin, LocalDate checkout) {
        List<BookingEntity> overlapping = bookingRepository.findOverlappingBookings(roomId, checkin, checkout);
        return overlapping.isEmpty(); // 겹치는 예약이 없으면 예약 가능 = true
    }

    // insert
    public BookingEntity insert(BookingDto dto) {
        BookingEntity booking = BookingEntity.builder()
                .merchantUid(dto.getMerchantUid())
                .roomId(dto.getRoomId())
                .memberId(dto.getMemberId())
                .checkin(dto.getCheckin())
                .checkout(dto.getCheckout())
                .price(dto.getPrice())
                .status("PENDING")
                .build();
        return bookingRepository.save(booking);
    }

    // 그 외 예약 관련 비즈니스 로직 메서드들...

}