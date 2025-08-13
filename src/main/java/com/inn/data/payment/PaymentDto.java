package com.inn.data.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {

    private Long idx;
    private Long bookingIdx;
    private String impUid;
    private String merchantUid;
    private String payMethod;
    private Long paidAmount;
    private String buyerName;
    private String buyerEmail;
    private String buyerTel;
    private String status;

}
