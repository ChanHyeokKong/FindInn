package com.inn.service;

import com.inn.data.payment.PaymentDto;
import com.inn.data.payment.PaymentEntity;
import com.inn.data.payment.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // insert
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
}