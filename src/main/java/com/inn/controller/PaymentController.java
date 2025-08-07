package com.inn.controller;

import com.inn.data.booking.BookingEntity;
import com.inn.data.booking.BookingRepository;
import com.inn.data.payment.PaymentDto;
import com.inn.data.payment.PaymentEntity;
import com.inn.service.PaymentService;
import com.data.siot.IamportRestClient.IamportClient;
import com.data.siot.IamportRestClient.exception.IamportResponseException;
import com.data.siot.IamportRestClient.request.CancelData;
import com.data.siot.IamportRestClient.response.IamportResponse;
import com.data.siot.IamportRestClient.response.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentService paymentService;

    private final IamportClient iamportClient;

    public PaymentController(
            @Value("${portone.api-key}") String apiKey,
            @Value("${portone.api-secret}") String apiSecret
    ) {
        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    /**
     * 프론트에서 결제 완료 후 imp_uid를 전달받아 결제 정보를 검증합니다.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validatePayment(
            @RequestParam String imp_uid,
            @RequestParam String merchant_uid) {

        try {
            Optional<BookingEntity> optionalBooking = bookingRepository.findByMerchantUid(merchant_uid);
            if (optionalBooking.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("result", "fail", "message", "예약 정보를 찾을 수 없습니다."));
            }

            BookingEntity booking = optionalBooking.get();

            IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(imp_uid);
            Payment payment = paymentResponse.getResponse();

            if (payment == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", "fail", "message", "결제 정보를 불러올 수 없습니다."));
            }

            // 금액 비교 (Booking.price는 Integer, payment.getAmount()는 BigDecimal)
            if (booking.getPrice() != payment.getAmount().intValue()) {
                // 결제 취소 요청 (전액 환불)
                CancelData cancelData = new CancelData(payment.getImpUid(), true);
                iamportClient.cancelPaymentByImpUid(cancelData);

                // 예약 상태 변경 및 저장
                booking.setStatus("CANCELED");
                bookingRepository.save(booking);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", "fail", "message", "결제 금액이 일치하지 않아 결제를 취소합니다."));
            }

            if (!"paid".equals(payment.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", "fail", "message", "결제 상태가 완료되지 않았습니다."));
            }

            // 결제 정상 확인 시 예약 상태 변경
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of("result", "success"));

        } catch (IamportResponseException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }

    // 결제 정보 저장
    @PostMapping("/insert")
    public ResponseEntity<?> insertPayment(@RequestBody PaymentDto dto) {
        try {
            PaymentEntity payment = paymentService.insert(dto);
            return ResponseEntity.ok(Map.of("result", "success", "id", payment.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }

}
