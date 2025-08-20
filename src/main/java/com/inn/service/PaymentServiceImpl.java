package com.inn.service;

import com.inn.data.payment.PaymentDto;
import com.inn.data.payment.PaymentEntity;
import com.inn.data.payment.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;

    /**
     * 결제 저장
     */
    @Override
    public PaymentEntity insert(PaymentDto dto) {
        PaymentEntity payment = PaymentEntity.builder()
                .bookingIdx(dto.getBookingIdx())
                .impUid(dto.getImpUid())
                .merchantUid(dto.getMerchantUid())
                .payMethod(dto.getPayMethod())
                .paidAmount(dto.getPaidAmount())
                .buyerName(dto.getBuyerName())
                .buyerEmail(dto.getBuyerEmail())
                .buyerTel(dto.getBuyerTel())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * impUid 기준 결제 취소
     */
    @Override
    public boolean cancelPaymentByImpUid(String impUid) {
        try {
            CancelData cancelData = new CancelData(impUid, true); // 전액 취소
            IamportResponse<Payment> response = iamportClient.cancelPaymentByImpUid(cancelData);

            return response.getResponse() != null &&
                    "cancelled".equalsIgnoreCase(response.getResponse().getStatus());  // 상태 비교는 대소문자 무시
        } catch (IamportResponseException | IOException e) {
            System.err.println("❌ 결제 취소 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * merchantUid 기준 결제 취소 및 DB 업데이트
     */
    public boolean cancelPaymentByMerchantUid(String merchantUid) {
        // 1. merchantUid로 결제 정보 조회
        PaymentEntity payment = paymentRepository.findByMerchantUid(merchantUid)
                .orElse(null);

        if (payment == null) {
            System.err.println("❌ 결제 정보가 없습니다. merchantUid: " + merchantUid);
            return false;
        }

        String impUid = payment.getImpUid();

        // 2. 결제 취소
        try {
            CancelData cancelData = new CancelData(impUid, true); // 전액 취소
            IamportResponse<Payment> response = iamportClient.cancelPaymentByImpUid(cancelData);

            if (response.getResponse() != null &&
                    "cancelled".equalsIgnoreCase(response.getResponse().getStatus())) {

                // 3. DB 상태 업데이트
                payment.setStatus("CANCELED");
                paymentRepository.save(payment);

                return true;
            } else {
                System.err.println("❌ 결제 취소 실패: 상태 불일치");
                return false;
            }

        } catch (IamportResponseException | IOException e) {
            System.err.println("❌ 결제 취소 실패: " + e.getMessage());
            return false;
        }
    }
}
