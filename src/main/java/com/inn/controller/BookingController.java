package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.booking.BookingDto;
import com.inn.data.booking.BookingEntity;
import com.inn.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Controller
public class BookingController {

    @Value("${portone.channel-key}")
    private String channelKey;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {

        // 로그인 유저 정보가 있을 경우
        if (currentUser != null) {

            Long currentMemberIdx = currentUser.getMemeberIdx();
            String email = currentUser.getUsername();          // 예시
            model.addAttribute("memberId", currentMemberIdx);
            model.addAttribute("memberEmail", email);

            model.addAttribute("isLogined", false);
        } else {
            model.addAttribute("isLogined", true);
        }

        // 객실가격
        int price = 150;

        // 몇 박인지 계산
        long nights = Duration.between(checkin.atStartOfDay(), checkout.atStartOfDay()).toDays();
        int totalPrice = price * (int)nights;

        // 객실정보
        model.addAttribute("room_id", 101);
        model.addAttribute("h_name", "서울 프리미엄 호텔");
        model.addAttribute("r_name", "디럭스 더블룸 101호");
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);

        // 결제정보
        model.addAttribute("price", price);
        model.addAttribute("nights", nights);
        model.addAttribute("totalPrice", totalPrice);

        // 포트원 채널키 (결제)
        model.addAttribute("channelKey", channelKey);

        return "booking/bookingPage";
    }

    @GetMapping("/booking/merchantUid")
    @ResponseBody
    public String generateMerchantUid() {
        return bookingService.generateMerchantUid(); // "ORD202508051230-AX3FZ1"
    }

    /**
     * 예약 겹침 여부 확인 (true = 예약 가능 / false = 이미 예약 존재)
     */
    @GetMapping("/booking/validate")
    @ResponseBody
    public boolean validateBookingOverlap(
            @RequestParam int roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return bookingService.isOverlappingBookingExists(roomId, checkin, checkout);
    }

    // 예약 저장
    @PostMapping("/booking/insert")
    public ResponseEntity<?> insertBooking(@RequestBody BookingDto dto) {
        try {
            BookingEntity booking = bookingService.insert(dto);
            return ResponseEntity.ok().body(
                    Map.of("result", "success", "id", booking.getId())
            );
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }

    // 결제 완료 페이지
    @GetMapping("/booking/complete")
    public String bookingPage(){
        return "booking/bookingComplete";
    }
}
