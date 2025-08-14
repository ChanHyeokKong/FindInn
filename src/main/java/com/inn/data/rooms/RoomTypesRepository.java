package com.inn.data.rooms;

import com.inn.data.booking.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomTypesRepository extends JpaRepository<RoomTypes, Long> {
    @Query("SELECT rt FROM RoomTypes rt WHERE rt.hotel.idx = :hotelId")
    @Transactional(readOnly = true)
    List<RoomTypes> findByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
    SELECT DISTINCT rt
    FROM RoomTypes rt
    WHERE EXISTS (
        SELECT 1
        FROM Rooms r
        WHERE r.roomType = rt
        AND rt.hotel.idx = :hotelId
        AND NOT EXISTS (
            SELECT 1
            FROM Reserve res
            WHERE res.room = r
            AND res.checkIn < :checkOutDate
            AND res.checkOut > :checkInDate
        )
    )
""")
    @Transactional(readOnly = true)
    List<RoomTypes> findAvailableRoomType(@Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate,
                                          @Param("hotelId") Long hotelId);


    @Query("""
    SELECT new com.inn.data.rooms.RoomTypeAvailDto(
        rt,
        (COUNT(DISTINCT r.idx) > COUNT(DISTINCT b.roomIdx))
    )
    FROM RoomTypes rt
    LEFT JOIN Rooms r ON r.roomType.idx = rt.idx
    LEFT JOIN BookingEntity b ON b.roomIdx = r.idx
        AND b.checkin < :checkOutDate
        AND b.checkout > :checkInDate
        AND b.status = 'CONFIRMED'
    WHERE rt.hotel.idx = :hotelId
    GROUP BY rt.idx
""")
    List<RoomTypeAvailDto> findRoomTypeAvailability(
            @Param("hotelId") Long hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

}
