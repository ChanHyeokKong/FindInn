package com.inn.service;

import com.inn.data.payment.PaymentDto;
import com.inn.data.payment.PaymentEntity;

public interface PaymentService {

    PaymentEntity insert(PaymentDto dto);

    boolean cancelPaymentByImpUid(String impUid);
}
