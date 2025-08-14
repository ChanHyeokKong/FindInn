package com.inn.scheduler;

import com.inn.data.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    // 1️⃣ 체크아웃 완료 처리 (매일 11시)
    @Scheduled(cron = "0 0 11 * * *")
    @Transactional
    public void completeBookingsAfterCheckout() {
        LocalDate today = LocalDate.now();
        int updatedCount = bookingRepository.completeTodayCheckouts(today);
        log.info("체크아웃 완료 처리: {}건", updatedCount);
    }

    // 2️⃣ 결제 미완료 예약 자동 취소 (5분 단위)
    @Scheduled(fixedRate = 300000) // 5분마다
    @Transactional
    public void cancelUnpaidBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        int canceledCount = bookingRepository.cancelUnpaidBookings(cutoff);
        log.info("미결제 자동 취소 처리: {}건", canceledCount);
    }

}
