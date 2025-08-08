package com.inn.data.booking;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "merchant_uid", nullable = false, unique = true)
    private String merchantUid;

    @Column(name = "room_idx", nullable = false)
    private Long roomIdx;

    @Column(name = "member_idx")
    private Long memberIdx;

    @Column(name = "checkin", nullable = false)
    private LocalDate checkin;

    @Column(name = "checkout", nullable = false)
    private LocalDate checkout;

    @Column(name = "price", nullable = false)
    private Long price;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
