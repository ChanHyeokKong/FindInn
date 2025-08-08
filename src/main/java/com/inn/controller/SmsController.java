package com.inn.controller;

import com.inn.data.booking.BookingInfo;
import com.inn.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    // ✅ 인증번호 문자 전송
    @GetMapping("/auth")
    public ResponseEntity<?> sendAuthCode(@RequestParam("guestPhone") String guestPhone) {
        try {
            String code = smsService.sendAuthCode(guestPhone);
            return ResponseEntity.ok(code);  // or ResponseEntity.ok("sent") if 보안 고려
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문자 전송 실패");
        }
    }

    // ✅ 예약 완료 문자 전송
    @PostMapping("/booking-confirm")
    public ResponseEntity<?> sendReservationConfirmation(@RequestBody BookingInfo info) {
        try {
            smsService.sendBookingConfirmation(info);
            return ResponseEntity.ok("문자 전송 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("문자 전송 실패");
        }
    }
}