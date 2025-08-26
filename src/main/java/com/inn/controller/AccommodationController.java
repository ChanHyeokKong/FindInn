package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.detail.AccommodationDto;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.review.RatingDto;
import com.inn.data.review.ReviewDto;
import com.inn.data.rooms.RoomTypeAvailDto;
import com.inn.service.AccommodationService;
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

    @Value("${kakao.map.javascript-key}")
    private String kakaoApiKey;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AccommodationService accommodationService;

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

        AccommodationDto accommodation = accommodationService.getAccommodationDetails(id, checkIn, checkOut, personal);
        // 예외처리
        if (accommodation.getHotel() ==null) {
            redirectAttributes.addFlashAttribute("errorMessage", "호텔이 존재하지 않습니다");
            String redirectUrl = (referer != null) ? referer : "/";
            return "redirect:" + redirectUrl;
        }
        model.addAttribute("accommodation", accommodation);
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("currentUser", currentUser);
        RatingDto rating = reviewService.getRatings(id);
        model.addAttribute("rating", rating);

        int size = 5; //한번에 리뷰 5개 로드
        Page<ReviewDto> reviewPage = reviewService.getReviewsByHotels(id, page, size);
        model.addAttribute("hotelId", id);
        model.addAttribute("reviewPage", reviewPage);

        if (currentUser != null) {
            model.addAttribute("currentMemberIdx", currentUser.getIdx());
        } else {
            model.addAttribute("currentMemberIdx", null);
        }

        return "detail/accommodation-detail";
    }


    @GetMapping("/reviewLists")
    public String reviewDetail(@RequestParam("id") Long id,@RequestParam(defaultValue = "0") int page, Model model) {
        int size = 5; //한번에 리뷰 5개 로드
        Page<ReviewDto> reviewPage = reviewService.getReviewsByHotels(id, page, size);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("hotelId", id);
        return "detail/accommodation-detail :: review-fragment";
    }
}