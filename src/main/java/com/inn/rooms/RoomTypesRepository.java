package com.inn.rooms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypesRepository extends JpaRepository<RoomTypes, Long> {
    @Query("SELECT rt FROM RoomTypes rt WHERE rt.hotel_id = :hotelId")
    List<RoomTypes> findByHotelId(@Param("hotelId") Long hotelId);
}
