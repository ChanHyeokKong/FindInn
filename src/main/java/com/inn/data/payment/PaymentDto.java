package com.inn.data.payment;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class PaymentDto {

    private Integer id;
    private Integer bookingId;
    private String impUid;
    private String merchantUid;
    private String payMethod;
    private Integer paidAmount;
    private String buyerName;
    private String buyerEmail;
    private String buyerTel;
    private String status;

}
