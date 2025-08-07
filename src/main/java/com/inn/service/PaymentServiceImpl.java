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

    @Override
    public PaymentEntity insert(PaymentDto dto) {
        PaymentEntity payment = PaymentEntity.builder()
                .bookingId(dto.getBookingId())
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
}
