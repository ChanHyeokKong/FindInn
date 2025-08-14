package com.inn.service;


import com.inn.data.hotel.HotelRepository;
import com.inn.data.review.ReviewDto;
import com.inn.data.rooms.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomsService {
    @Autowired
    RoomsRepository roomsRepository;

    @Autowired
    RoomTypesRepository roomTypesRepository;

    @Autowired
    HotelRepository hotelRepository;

    Random rand = new Random();

    public List<RoomTypeAvailDto> getAllHotelRoomTypesWithAvailability(long hotelId, LocalDate checkIn, LocalDate checkOut) {
        return roomTypesRepository.findRoomTypeAvailability(hotelId, checkIn, checkOut);
    }


    public Rooms getAvailableRoom(long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        List<Rooms> rooms = roomsRepository.findAvailableRoomNo(roomTypeId,checkIn,checkOut);
        if (rooms.isEmpty()) {
            return null;
        } else {
            int randomIndex = rand.nextInt(rooms.size());
            Rooms randomRoom = rooms.get(randomIndex);
            return randomRoom;
        }
    }
    public record RoomHotelDetailsDto(String roomTypeName, String hotelName) {
    }

    public RoomHotelDetailsDto getHotelNamebyRoomTypeId(long roomTypeId) {
        RoomTypes roomType = roomTypesRepository.findById(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

        String typeName = roomType.getTypeName();
        String hotelName = roomType.getHotel().getHotelName();

        return new RoomHotelDetailsDto(typeName, hotelName);
    }


}
