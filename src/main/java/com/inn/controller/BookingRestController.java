package com.inn.controller;

import com.inn.data.booking.BookingDto;
import com.inn.data.booking.BookingEntity;
import com.inn.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;

    // 예약번호 생성
    @GetMapping("/merchantUid")
    public String generateMerchantUid() {
        return bookingService.generateMerchantUid();
    }

    // 예약 중복 확인
    @GetMapping("/validate")
    public boolean validateBookingOverlap(
            @RequestParam int roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout
    ) {
        return bookingService.isOverlappingBookingExists(roomId, checkin, checkout);
    }

    // 예약 저장
    @PostMapping("/insert")
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

    // 예약 상태를 CANCELED로 변경하는 API
    @PutMapping("/update/cancel/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable int id) {
        try {
            BookingEntity updated = bookingService.updateStatusToCanceled(id);
            return ResponseEntity.ok(Map.of("result", "success", "status", updated.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }
}
