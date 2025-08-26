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
import java.util.Optional;
import java.util.stream.Collectors;

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

        HotelEntity hotel = Optional.ofNullable(room.getHotel())
                .orElseThrow(() -> new IllegalArgumentException("호텔 정보를 찾을 수 없습니다."));

        RoomTypes roomType = room.getRoomType();

        return BookingRoomInfo.builder()
                .hotelIdx(hotel.getIdx())
                .hotelImage(hotel.getHotelImage())
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
                .couponIdx(dto.getCouponIdx())
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

    /**
     * 1. 회원 예약내역 리스트 조회 (예약확정, 이용완료, 예약취소)
     * 2. Entity 리스트 -> BookingListInfo 리스트 변환
     */
    @Override
    public List<BookingListInfo> getBookingsByStatus(Long memberIdx, String status) {
        List<BookingEntity> bookings = bookingRepository.findByMemberIdxAndStatusWithPayment(memberIdx, status);

        return bookings.stream()
                .map(booking -> {
                    Rooms room = roomsRepository.findById(booking.getRoomIdx())
                            .orElseThrow(() -> new IllegalArgumentException("객실 정보를 찾을 수 없습니다."));
                    HotelEntity hotel = Optional.ofNullable(room.getHotel())
                            .orElseThrow(() -> new IllegalArgumentException("호텔 정보를 찾을 수 없습니다."));

                    return BookingListInfo.builder()
                            .bookingIdx(booking.getIdx())
                            .merchantUid(booking.getMerchantUid())
                            .status(booking.getStatus())
                            .hotelName(hotel.getHotelName())
                            .hotelImage(hotel.getHotelImage())
                            .roomName(room.getRoomType().getTypeName())
                            .roomNumber(room.getRoomNumber())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 예약 상세 페이지 조회
     */
    @Override
    public BookingDetailInfo getBookingDetailInfo(String merchantUid) {
        // 예약 조회
        BookingEntity booking = bookingRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 결제 조회
        PaymentEntity payment = paymentRepository.findByBookingIdx(booking.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 없습니다."));

        // 객실 조회
        Rooms room = roomsRepository.findById(booking.getRoomIdx())
                .orElseThrow(() -> new IllegalArgumentException("객실 정보를 찾을 수 없습니다."));

        // 호텔 조회
        HotelEntity hotel = Optional.ofNullable(room.getHotel())
                .orElseThrow(() -> new IllegalArgumentException("호텔 정보를 찾을 수 없습니다."));

        // 체크인/체크아웃 요일 계산
        String checkinDay = getKoreanShortDayOfWeek(booking.getCheckin());
        String checkoutDay = getKoreanShortDayOfWeek(booking.getCheckout());

        // 비회원 여부
        boolean isMember = booking.getMemberIdx() != null;

        // 예약 취소 가능 여부 (체크인 하루 전까지)
        boolean canCancel = LocalDate.now().isBefore(booking.getCheckin());

        // DTO 생성
        return BookingDetailInfo.builder()
                // 비회원 여부 설정
                .isMember(isMember)

                // 예약 취소 가능 여부 설정
                .canCancel(canCancel)

                // 예약 정보
                .merchantUid(booking.getMerchantUid())
                .couponIdx(booking.getCouponIdx())
                .checkin(booking.getCheckin())
                .checkout(booking.getCheckout())
                .checkinDay(checkinDay)
                .checkoutDay(checkoutDay)
                .status(booking.getStatus())

                // 예약자 정보
                .buyerName(payment.getBuyerName())
                .buyerTel(payment.getBuyerTel())

                // 호텔 정보
                .hotelName(hotel.getHotelName())
                .hotelImage(hotel.getHotelImage())
                .hotelAddress(hotel.getHotelAddress())

                // 객실 정보
                .roomName(room.getRoomType().getTypeName())
                .roomNumber(room.getRoomNumber())
                .roomPrice(room.getRoomType().getPrice())
                .capacity(room.getRoomType().getCapacity())
                .description(room.getRoomType().getDescription())

                // 결제 정보
                .payMethod(payment.getPayMethod())
                .paidAmount(payment.getPaidAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}