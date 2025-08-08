package com.inn.data.payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "booking_idx")
    private Long bookingIdx;

    @Column(name = "imp_uid", nullable = false)
    private String impUid;

    @Column(name = "merchant_uid", nullable = false, unique = true)
    private String merchantUid;

    @Column(name = "pay_method", nullable = false)
    private String payMethod;

    @Column(name = "paid_amount", nullable = false)
    private Long paidAmount;

    @Column(name = "buyer_name", nullable = false)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false)
    private String buyerEmail;

    @Column(name = "buyer_tel", nullable = false)
    private String buyerTel;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "PAID";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}