package com.inn.service;

import com.inn.data.booking.*;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.payment.PaymentEntity;
import com.inn.data.payment.PaymentRepository;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.Rooms;
import com.inn.data.rooms.RoomsRepository;
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
    private final RoomsRepository roomsRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 현재 시간(초) + 랜덤 6자리 영문 대문자 조합으로 고유 merchantUid 생성
     */
    @Override
    public String generateMerchantUid() {
        StringBuilder sb = new StringBuilder();

        // 현재 시간 초 단위 (1970년 기준)
        sb.append(System.currentTimeMillis() / 1000);

        // 랜덤 6자리 대문자 추가
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int idx = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * 객실 정보 조회
     */
    @Override
    public BookingRoomInfo getBookingRoomInfo(Long roomIdx) {
        Rooms room = roomsRepository.findById(roomIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 객실입니다."));

        HotelEntity hotel = hotelRepository.findById(room.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 호텔입니다."));

        RoomTypes roomType = room.getRoomType();

        String firstImage = null;
        List<String> images = hotel.getHotelImages();
        if (images != null && !images.isEmpty()) {
            firstImage = images.get(0);
        }

        return BookingRoomInfo.builder()
                .hotelImage(firstImage)
                .hotelName(hotel.getHotelName())
                .roomName(roomType.getTypeName())
                .roomNumber(room.getRoomNumber())
                .roomPrice(roomType.getPrice())
                .capacity(roomType.getCapacity())
                .description(roomType.getDescription())
                .build();
    }

    /**
     * 요일 추출
     */
    @Override
    public String getKoreanShortDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY    -> "(월)";
            case TUESDAY   -> "(화)";
            case WEDNESDAY -> "(수)";
            case THURSDAY  -> "(목)";
            case FRIDAY    -> "(금)";
            case SATURDAY  -> "(토)";
            case SUNDAY    -> "(일)";
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

    /**
     * 예약 완료 정보 조회
     */
    @Override
    public BookingCompleteInfo getBookingCompleteInfo(Long bookingIdx) {
        BookingEntity booking = bookingRepository.findById(bookingIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        PaymentEntity payment = paymentRepository.findByBookingIdx(bookingIdx)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 없습니다."));

        Rooms room = roomsRepository.findById(booking.getRoomIdx())
                .orElseThrow(() -> new IllegalArgumentException("객실 정보를 찾을 수 없습니다."));

        // 체크인/체크아웃 요일 (ex: (월))
        String checkinDay = getKoreanShortDayOfWeek(booking.getCheckin());
        String checkoutDay = getKoreanShortDayOfWeek(booking.getCheckout());

        return BookingCompleteInfo.builder()
                .merchantUid(booking.getMerchantUid())
                .checkin(booking.getCheckin())
                .checkout(booking.getCheckout())
                .checkinDay(checkinDay)
                .checkoutDay(checkoutDay)
                .roomName(room.getRoomType().getTypeName())
                .roomNumber(room.getRoomNumber())
                .paidAmount(payment.getPaidAmount())
                .build();
    }
}
