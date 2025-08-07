package com.inn.data.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    Optional<BookingEntity> findByMerchantUid(String merchantUid);

    @Query("SELECT b FROM BookingEntity b " +
            "WHERE b.roomId = :roomId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkout > :checkin " +
            "AND b.checkin < :checkout")
    List<BookingEntity> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout);
}
