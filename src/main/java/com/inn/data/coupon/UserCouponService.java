package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import com.inn.service.CouponService.Preview;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponService {

    // ── 발급 ────────────────────────────────────────────────
    UserCoupon issueByCode(MemberDto member, String couponCode, @Nullable String eventCode);
    UserCoupon issueByTrait(MemberDto member, String trait, @Nullable String eventCode);

    List<String> getIssuedCodes(MemberDto member, @Nullable String issuedFrom);

    // ── 조회/미리보기 ──────────────────────────────────────
    List<PreviewCoupon> listUsableCoupons(MemberDto member, Long hotelId, long price, @Nullable String userTrait);

    long calculateDiscount(MemberDto member, Long userCouponId, Long hotelId, long price, @Nullable String userTrait);

    Preview previewPrice(MemberDto member,
                         Long hotelId,
                         long originalPrice,                 // long 통일
                         @Nullable Long mainUserCouponId,
                         @Nullable Long stackUserCouponId,
                         @Nullable String userTrait);

    // 오버로드(스택 미사용)
    default Preview previewPrice(MemberDto member, Long hotelId, long originalPrice,
                                 @Nullable Long mainUserCouponId, @Nullable String userTrait) {
        return previewPrice(member, hotelId, originalPrice, mainUserCouponId, null, userTrait);
    }

    // ── 상태 변경 ──────────────────────────────────────────
    /**
     * 결제 성공 시 쿠폰 사용 확정.
     * @return true면 성공(1 row), false면 미적용(이미 사용됨/소유자 불일치 등)
     * @throws IllegalArgumentException (존재하지 않음/권한 없음 등)
     */
    default boolean confirmUse(Long userCouponId, MemberDto member, @Nullable String bookingId) {
        return markUsed(userCouponId, member); // 레거시 경로 재사용 (구현체에서 override 권장)
    }

    /**
     * 전액 환불 등으로 사용 취소.
     * 구현체에서 정책에 맞게 bookingId 검증 포함 권장.
     */
    default boolean revertUse(Long userCouponId, MemberDto member, @Nullable String bookingId) {
        return false; // 기본 no-op (구현체 override 권장)
    }

    // ── 레거시(호환용) ─────────────────────────────────────
    @Deprecated
    List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, long price, @Nullable String userTrait);

    @Deprecated
    boolean markUsed(Long userCouponId, MemberDto member);

    // ── 프론트용 프리뷰 DTO ───────────────────────────────
    record PreviewCoupon(
            Long userCouponId,
            String code,
            String name,
            long discount,
            boolean stackable,
            @Nullable String exclusiveGroup,
            @Nullable LocalDateTime expiresAt,
            long minSpend,
            @Nullable String unusableReason
    ) {}
}
