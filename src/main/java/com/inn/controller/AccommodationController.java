package com.inn.controller;

import com.inn.data.detail.AccommodationDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.rooms.RoomTypeAvailDto;
import com.inn.service.HotelService;
import com.inn.service.RoomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class AccommodationController {
    @Autowired
    RoomsService roomsService;

    @Autowired
    HotelService hotelService;

    @Value("${kakao.map.javascript-key}")
    private String kakaoApiKey;

    @GetMapping("/domestic-accommodations")
    public String accommodationDetail(
            @RequestParam("id") Long id,
            @RequestParam(value = "checkIn", required = false) LocalDate checkIn,
            @RequestParam(value = "checkOut", required = false) LocalDate checkOut,
            @RequestParam(value = "personal", required = false) Integer personal,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer) {
        AccommodationDto accommodation = new AccommodationDto();

        int numberOfPeople = (personal != null) ? personal : 1; // Default to 1 person if not specified

        accommodation.setCheckInDate(checkIn);
        accommodation.setCheckOutDate(checkOut); 
        accommodation.setPersonal(numberOfPeople);

        
        // 호텔 db에서 불러오기
        Optional<HotelEntity> hotel = hotelService.getHotelDataById(id);
        if (!hotel.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "The requested hotel does not exist.");
            String redirectUrl = (referer != null) ? referer : "/";
            return "redirect:" + redirectUrl;
        }
        accommodation.setName(hotel.get().getHotelName());
        accommodation.setAddress(hotel.get().getHotelAddress());
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
        model.addAttribute("kakaoApiKey", kakaoApiKey);

        return "detail/accommodation-detail";
    }
}