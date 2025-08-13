package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.booking.BookingCompleteInfo;
import com.inn.data.booking.BookingRoomInfo;
import com.inn.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Value("${portone.channel-key}")
    private String channelKey;

    // 예약 및 결제 페이지
    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @RequestParam("roomIdx") Long roomIdx,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {

        if (currentUser != null) {
            model.addAttribute("memberIdx", currentUser.getIdx());
            model.addAttribute("memberEmail", currentUser.getUsername());
            model.addAttribute("isLogined", false);
        } else {
            model.addAttribute("memberIdx", null);
            model.addAttribute("memberEmail", "test@example.com");
            model.addAttribute("isLogined", false);
        }

        // 예약 정보 DTO 조회
        BookingRoomInfo bookingInfo = bookingService.getBookingRoomInfo(roomIdx);

        // 요일 계산
        String checkinDay = bookingService.getKoreanShortDayOfWeek(checkin);
        String checkoutDay = bookingService.getKoreanShortDayOfWeek(checkout);

        // 숙박 일수 계산
        long nights = Duration.between(checkin.atStartOfDay(), checkout.atStartOfDay()).toDays();

        // 총 가격 계산 (객실 가격 * 숙박 일수)
        long totalPrice = (bookingInfo.getRoomPrice() != null ? bookingInfo.getRoomPrice() : 0L) * nights;

        // model 세팅
        model.addAttribute("roomIdx", roomIdx);
        model.addAttribute("hotelImage", bookingInfo.getHotelImage()); // 리스트(첫 이미지 포함)
        model.addAttribute("hotelName", bookingInfo.getHotelName());
        model.addAttribute("roomName", bookingInfo.getRoomName());
        model.addAttribute("roomNameAndNum", bookingInfo.getRoomName() + " " + bookingInfo.getRoomNumber() + "호");
        model.addAttribute("capacity", bookingInfo.getCapacity());
        model.addAttribute("description", bookingInfo.getDescription());
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("checkinDay", checkinDay);
        model.addAttribute("checkoutDay", checkoutDay);
        model.addAttribute("price", bookingInfo.getRoomPrice());
        model.addAttribute("nights", nights);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("channelKey", channelKey);

        return "booking/bookingPage";
    }

    // 예약 성공 확인 페이지
    @GetMapping("/booking/complete")
    public String bookingCompletePage(@RequestParam Long bookingIdx, Model model) {
        BookingCompleteInfo info = bookingService.getBookingCompleteInfo(bookingIdx);

        model.addAttribute("merchantUid", info.getMerchantUid());
        model.addAttribute("checkin", info.getCheckin());
        model.addAttribute("checkout", info.getCheckout());
        model.addAttribute("checkinDay", info.getCheckinDay());
        model.addAttribute("checkoutDay", info.getCheckoutDay());
        model.addAttribute("roomName", info.getRoomName());
        model.addAttribute("roomNumber", info.getRoomNumber());
        model.addAttribute("paidAmount", info.getPaidAmount());

        return "booking/bookingComplete";
    }
}
