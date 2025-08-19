package com.inn.service;


import com.inn.data.hotel.HotelEntity;
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
    public record RoomHotelDetailsDto(Long roomTypeId, Long hotelId, String hotelImages, String hotelName, String roomName) {
    }

    public RoomHotelDetailsDto getHotelNamebyRoomTypeId(long roomId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("룸 타입이 없습니다. " + roomId));

        Long type = room.getRoomType().getIdx();
        Long hotelId = room.getHotel().getIdx();
        String hotelImages = room.getHotel().getHotelImage();
        String hotelName = room.getHotel().getHotelName();
        String roomName = room.getRoomType().getTypeName();
        return new RoomHotelDetailsDto(type, hotelId, hotelImages, hotelName, roomName);
    }


}
