package com.inn.data.coupon;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;                 // 예: AUG_7P, GLOBAL_5P

    @Column(nullable = false, length = 100)
    private String name;                 // 예: 8월팩 7% 할인

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DiscountType type;           // PERCENT, AMOUNT

    @Column(nullable = false)
    private int discountValue;           // % 또는 정액(원)

    private Integer minOrderAmount;      // 최소주문금액(원)
    private Integer maxDiscountAmount;   // 퍼센트 상한(원)

    private LocalDateTime validFrom;     // 절대기간 시작
    private LocalDateTime validTo;       // 절대기간 끝
    private Integer validDaysFromIssue;  // 발급+N일
    private LocalDate hardEndDate;       // 절대 마감일(예: 8/31)

    @Column(nullable = false)
    private Boolean stackable;           // 다른 쿠폰과 중복 가능 여부

    private String exclusiveGroup;       // 배타 그룹(같은 그룹끼리 동시사용 불가)
    private String issuedFrom;           // 발급 출처(로그/통계)
    private String applicableTrait;      // 심리테스트 성향 제한(null=전체)

    @Column(nullable = false)
    private Boolean appliesAllHotels;    // 전 호텔 적용 여부

    @ElementCollection
    @CollectionTable(name = "coupon_allowed_hotels", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "hotel_id")
    private List<Long> allowedHotelIds;  // 지정 호텔 PK 목록(간단버전)

    public enum DiscountType { PERCENT, AMOUNT }
}