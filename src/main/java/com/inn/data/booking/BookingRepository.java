package com.inn.data.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    Optional<BookingEntity> findByMerchantUid(String merchantUid);

    // 예약 중복확인 쿼리
    @Query("SELECT b FROM BookingEntity b " +
            "WHERE b.roomIdx = :roomIdx " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkout > :checkin " +
            "AND b.checkin < :checkout")
    List<BookingEntity> findOverlappingBookings(
            @Param("roomIdx") Long roomIdx,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout);

    // 체크아웃 완료 처리 (스케줄링)
    @Modifying
    @Query("""
        UPDATE BookingEntity b
        SET b.status = 'COMPLETED'
        WHERE b.status = 'CONFIRMED'
          AND b.checkout <= :today
    """)
    int completeTodayCheckouts(@Param("today") LocalDate today);

    // 결제 미완료 예약 자동 취소 (스케줄링)
    @Modifying
    @Query("""
        UPDATE BookingEntity b
        SET b.status = 'CANCELED'
        WHERE b.status = 'PENDING'
          AND b.createdAt <= :cutoff
    """)
    int cancelUnpaidBookings(@Param("cutoff") LocalDateTime cutoff);

}
