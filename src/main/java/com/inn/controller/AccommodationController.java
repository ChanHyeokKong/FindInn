package com.inn.controller;

import com.inn.data.detail.AccommodationDto;
import com.inn.data.rooms.RoomTypeAvailDto;
import com.inn.service.RoomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class AccommodationController {
    @Autowired
    RoomsService roomsService;

    @GetMapping("/domestic-accommodations")
    public String accommodationDetail(
            @RequestParam("id") Long id,
            @RequestParam("checkIn") LocalDate checkIn,
            @RequestParam("checkOut") LocalDate checkOut,
            @RequestParam("personal") int personal,
            Model model) {
        AccommodationDto accommodation = new AccommodationDto();

        accommodation.setCheckInDate(checkIn);
        accommodation.setCheckOutDate(checkOut);
        accommodation.setPersonal(personal);

        
        // 호텔 db에서 불러오기
        accommodation.setName("강릉 더끌림 펜션");
        accommodation.setAddress("강원도 강릉시 해안로 123-45");
        accommodation.setImageGalleries(Arrays.asList(
                "/images/pension_main.jpg",
                "/images/pension_room1.jpg",
                "/images/pension_pool.jpg"
        ));
        accommodation.setCheckInTime("15:00");
        accommodation.setCheckOutTime("11:00");

        List <RoomTypeAvailDto> rooms = roomsService.getAllHotelRoomTypesWithAvailability(id,checkIn,checkOut);
        accommodation.setRoomTypes(rooms);

        model.addAttribute("accommodation", accommodation);

        return "detail/accommodation-detail";
    }
}