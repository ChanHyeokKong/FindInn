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
    private Integer id;

    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "imp_uid")
    private String impUid;

    @Column(name = "merchant_uid", nullable = false, unique = true)
    private String merchantUid;

    @Column(name = "pay_method")
    private String payMethod;

    @Column(name = "paid_amount")
    private Integer paidAmount;

    @Column(name = "buyer_name")
    private String buyerName;

    @Column(name = "buyer_email")
    private String buyerEmail;

    @Column(name = "buyer_tel")
    private String buyerTel;

    @Builder.Default
    @Column(name = "status")
    private String status = "PAID";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}