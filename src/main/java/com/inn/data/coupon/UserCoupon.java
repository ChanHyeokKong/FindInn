package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_coupon",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "coupon_id"}) // 중복 발급 방지
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유 회원 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private MemberDto member;

    /** 참조 쿠폰(마스터) */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /** 발급 시각 */
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    /** 사용 여부/시각 */
    @Column(nullable = false)
    private boolean used;
    private LocalDateTime usedAt;

    /** 발급 출처(이벤트 코드 등, 선택) */
    @Column(length = 100)
    private String relatedEvent;
}