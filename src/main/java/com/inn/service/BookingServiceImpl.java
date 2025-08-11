package com.inn.service;

import com.inn.data.booking.BookingDto;
import com.inn.data.booking.BookingEntity;
import com.inn.data.booking.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 현재 시간(밀리초) + 랜덤 6자리 영문 대문자 조합으로 고유 merchantUid 생성
     */
    @Override
    public String generateMerchantUid() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int idx = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * 요일 추출
     */
    @Override
    public String getKoreanShortDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY    -> "월";
            case TUESDAY   -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY  -> "목";
            case FRIDAY    -> "금";
            case SATURDAY  -> "토";
            case SUNDAY    -> "일";
        };
    }

    /**
     * 같은 객실의 동일한 기간에 예약이 이미 존재하는지 확인
     */
    @Override
    public boolean isOverlappingBookingExists(Long roomIdx, LocalDate checkin, LocalDate checkout) {
        List<BookingEntity> overlapping = bookingRepository.findOverlappingBookings(roomIdx, checkin, checkout);
        return overlapping.isEmpty();
    }

    /**
     * 예약 저장
     */
    @Override
    public BookingEntity insert(BookingDto dto) {
        BookingEntity booking = BookingEntity.builder()
                .merchantUid(dto.getMerchantUid())
                .roomIdx(dto.getRoomIdx())
                .memberIdx(dto.getMemberIdx())
                .checkin(dto.getCheckin())
                .checkout(dto.getCheckout())
                .price(dto.getPrice())
                .build();
        return bookingRepository.save(booking);
    }

    /**
     * 예약 취소 변경
     */
    @Override
    @Transactional
    public BookingEntity updateStatusToCanceled(Long idx) {
        BookingEntity booking = bookingRepository.findById(idx)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with idx: " + idx));
        booking.setStatus("CANCELED");
        booking.setCanceledAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    // 기타 예약 비즈니스 로직 구현...

}
