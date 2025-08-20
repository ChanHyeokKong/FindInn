package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_coupon",
    uniqueConstraints = @UniqueConstraint(name="uk_usercoupon_member_coupon", columnNames = {"member_id", "coupon_id"}),
    indexes = {
        @Index(name = "idx_usercoupon_member_used", columnList = "member_id, used"),
        @Index(name = "idx_usercoupon_event", columnList = "related_event"),
        @Index(name = "idx_usercoupon_coupon", columnList = "coupon_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString(exclude = {"member", "coupon"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberDto member; // ✅ 지금 구조 유지

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    private LocalDateTime usedAt;

    @Column(name = "related_event", length = 100)
    private String relatedEvent;

    @Version
    private Long version;

    @PrePersist
    private void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (relatedEvent != null) relatedEvent = relatedEvent.trim();
    }

    // 비즈니스 메서드
    public void markUsed(LocalDateTime now) {
        if (!this.used) {
            this.used = true;
            this.usedAt = (now != null ? now : LocalDateTime.now());
        }
    }
    public void revertUse() {
        if (this.used) {
            this.used = false;
            this.usedAt = null;
        }
    }
}