package com.inn.rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomTypesRepository extends JpaRepository<RoomTypes, Long> {
    @Query("SELECT rt FROM RoomTypes rt WHERE rt.hotelId = :hotelId")
    List<RoomTypes> findByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
    SELECT DISTINCT rt
    FROM RoomTypes rt
    WHERE rt.capacity >= :peopleCount
    AND EXISTS (
        SELECT 1
        FROM Rooms r
        WHERE r.roomType = rt
        AND r.hotelId = :hotelId
        AND NOT EXISTS (
            SELECT 1
            FROM Reserve res
            WHERE res.room = r
            AND res.checkIn < :checkOutDate
            AND res.checkOut > :checkInDate
        )
    )
""")
    List<RoomTypes> findAvailableRoomType(@Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate,
                                          @Param("peopleCount") int peopleCount,
                                          @Param("hotelId") Long hotelId);


}
