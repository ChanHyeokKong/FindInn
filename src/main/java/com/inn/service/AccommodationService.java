package com.inn.service;

import com.inn.data.detail.AccommodationDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.rooms.RoomTypeAvailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccommodationService {
    @Autowired
    RoomsService roomsService;
    @Autowired
    HotelService hotelService;
    
    // 숙소 정보 불러오기
    public AccommodationDto getAccommodationDetails(Long id, LocalDate checkIn, LocalDate checkOut, Integer personal) {
        AccommodationDto accommodation = new AccommodationDto();
        int numberOfPeople = (personal != null) ? personal : 1;
        accommodation.setCheckInDate(checkIn);
        accommodation.setCheckOutDate(checkOut);
        accommodation.setPersonal(numberOfPeople);

        // 호텔 db에서 불러오기
        Optional<HotelEntity> hotel = hotelService.getHotelDataById(id);
        if (hotel.isPresent()) {
            accommodation.setHotel(hotel.get());
        }
        accommodation.setName(hotel.get().getHotelName());
        accommodation.setAddress(hotel.get().getHotelAddress());
        List<String> images = new ArrayList<>();
        images.add(hotel.get().getHotelImage());
        accommodation.setCheckInTime("15:00");
        accommodation.setCheckOutTime("11:00");

        // 해당 호텔의 방 정보
        List <RoomTypeAvailDto> rooms = roomsService.getAllHotelRoomTypesWithAvailability(id,checkIn,checkOut);
        accommodation.setRoomTypes(rooms);

        for (RoomTypeAvailDto room : rooms) {
            images.add(room.getImageUrl());
        }
        accommodation.setImageGalleries(images);
        return accommodation;
    }

}
