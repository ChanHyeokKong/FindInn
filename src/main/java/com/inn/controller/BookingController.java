package com.inn.controller;

import com.inn.config.CustomUserDetails;
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

    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {

        if (currentUser != null) {
//            model.addAttribute("memberIdx", currentUser.getIdx());
//            model.addAttribute("memberEmail", currentUser.getUsername());
            model.addAttribute("memberIdx", null);
            model.addAttribute("memberEmail", "test@example.com");
            model.addAttribute("isLogined", false);
        } else {
            model.addAttribute("isLogined", true);
        }

        // 요일
        String checkinDay = bookingService.getKoreanShortDayOfWeek(checkin);
        String checkoutDay = bookingService.getKoreanShortDayOfWeek(checkout);

        // 가격
        long price = 150L;
        long nights = Duration.between(checkin.atStartOfDay(), checkout.atStartOfDay()).toDays();
        long totalPrice = price * nights;

        model.addAttribute("roomIdx", 101);
        model.addAttribute("h_name", "서울 프리미엄 호텔");
        model.addAttribute("r_name", "디럭스 더블룸 101호");
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("checkinDay", checkinDay);
        model.addAttribute("checkoutDay", checkoutDay);
        model.addAttribute("price", price);
        model.addAttribute("nights", nights);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("channelKey", channelKey);

        return "booking/bookingPage";
    }

    @GetMapping("/booking/complete")
    public String bookingPage() {
        return "booking/bookingComplete";
    }
}
