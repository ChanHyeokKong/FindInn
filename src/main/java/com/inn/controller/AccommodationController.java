package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.detail.AccommodationDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.review.RatingDto;
import com.inn.data.review.ReviewDto;
import com.inn.data.rooms.RoomTypeAvailDto;
import com.inn.service.HotelService;
import com.inn.service.ReviewService;
import com.inn.service.RoomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Autowired
    ChatRepository chatRepository;
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/domestic-accommodations")
    public String accommodationDetail(
            @RequestParam("id") Long id,
            @RequestParam(value = "checkIn", required = false) LocalDate checkIn,
            @RequestParam(value = "checkOut", required = false) LocalDate checkOut,
            @RequestParam(value = "personal", required = false) Integer personal,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page) {
        
        AccommodationDto accommodation = new AccommodationDto();
        int numberOfPeople = (personal != null) ? personal : 1;
        // 파라메터에서 정보 가져오기
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
        else{
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
        model.addAttribute("accommodation", accommodation);
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        System.out.println(accommodation.getImageGalleries());
        model.addAttribute("currentUser", currentUser);
        RatingDto rating = reviewService.getRatings(id);
        model.addAttribute("rating", rating);

        int size = 5; //한번에 리뷰 5개 로드
        Page<ReviewDto> reviewPage = reviewService.getReviewsByHotels(id, page, size);
        model.addAttribute("hotelId", id);
        model.addAttribute("reviewPage", reviewPage);

        // Add current user's memberIdx to the model
        if (currentUser != null) {
            model.addAttribute("currentMemberIdx", currentUser.getIdx());
        } else {
            model.addAttribute("currentMemberIdx", null); // Or a default value if not logged in
        }

        return "detail/accommodation-detail";
    }


    @GetMapping("/reviewLists")
    public String reviewDetail(@RequestParam("id") Long id,@RequestParam(defaultValue = "0") int page, Model model) {
        int size = 5; //한번에 리뷰 5개 로드
        Page<ReviewDto> reviewPage = reviewService.getReviewsByHotels(id, page, size);


        System.out.println("Review Page Content: " + reviewPage.getContent());
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("hotelId", id);
        return "detail/accommodation-detail :: review-fragment";
    }
}