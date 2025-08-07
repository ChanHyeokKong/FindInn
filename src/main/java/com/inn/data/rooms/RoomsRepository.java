package com.inn.data.rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface RoomsRepository extends JpaRepository<Rooms, Long> {


    @Query("""
    SELECT r
    FROM Rooms r
    WHERE r.roomType.idx = :roomTypeId
    AND NOT EXISTS (
        SELECT 1
        FROM Reserve res
        WHERE res.room.idx = r.idx
        AND res.checkIn < :checkOutDate
        AND res.checkOut > :checkInDate
    )
""")
    @Transactional(readOnly = true)
    List<Rooms> findAvailableRoomNo(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
