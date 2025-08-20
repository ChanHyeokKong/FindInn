package com.inn.service;

import com.inn.data.coupon.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    @Override
    public boolean isUsableNow(Coupon c, LocalDateTime issuedAt, LocalDateTime now) {
        if (c == null) return false;
        if (now == null) now = LocalDateTime.now();

        // 절대 기간
        if (c.getValidFrom() != null && now.isBefore(c.getValidFrom())) return false;
        if (c.getValidTo()   != null && now.isAfter(c.getValidTo()))   return false;

        // 하드 컷오프(날짜 단위)
        if (c.getHardEndDate() != null && now.toLocalDate().isAfter(c.getHardEndDate())) return false;

        // 발급+N일(상대 만료)
        if (c.getValidDaysFromIssue() != null && issuedAt != null) {
            LocalDateTime relativeEnd = issuedAt.plusDays(c.getValidDaysFromIssue());
            if (now.isAfter(relativeEnd)) return false;
            // 하드컷 보강 (중복이지만 명시적으로)
            if (c.getHardEndDate() != null && now.toLocalDate().isAfter(c.getHardEndDate())) return false;
        }
        return true;
    }

    @Override
    public boolean isApplicableToHotel(Coupon c, Long hotelId) {
        if (c == null) return false;
        if (Boolean.TRUE.equals(c.getAppliesAllHotels())) return true;
        return hotelId != null
                && c.getAllowedHotelIds() != null
                && c.getAllowedHotelIds().contains(hotelId);
    }

    @Override
    public boolean isApplicableToPrice(Coupon c, int price) {
        if (c == null) return false;
        Integer min = c.getMinOrderAmount();
        return (min == null || price >= min);
    }

    @Override
    public boolean matchTraitIfRequired(Coupon c, String userTrait) {
        if (c == null) return false;
        if (c.getApplicableTrait() == null) return true; // 제한 없음
        return Objects.equals(c.getApplicableTrait(), userTrait);
    }

    @Override
    public int calcDiscount(Coupon c, int originalPrice) {
        if (c == null || originalPrice <= 0) return 0;

        int discount;
        switch (c.getType()) {
            case PERCENT -> {
                // long 중간 연산으로 안전 계산
                long raw = ((long) originalPrice) * c.getDiscountValue() / 100L;
                Integer cap = c.getMaxDiscountAmount();
                long capped = (cap != null) ? Math.min(raw, cap) : raw;
                // 0 ~ originalPrice 범위로 고정
                if (capped < 0) capped = 0;
                if (capped > originalPrice) capped = originalPrice;
                discount = (int) capped;
            }
            case AMOUNT -> {
                int amt = Math.max(0, c.getDiscountValue());
                discount = Math.min(amt, originalPrice);
            }
            default -> discount = 0;
        }
        return discount;
    }

    @Override
    public Preview previewFinalPrice(int originalPrice, Coupon main, Coupon stack) {
        // 1) 메인 쿠폰
        int mainDiscount = (main != null) ? calcDiscount(main, originalPrice) : 0;
        if (mainDiscount < 0) mainDiscount = 0;
        if (mainDiscount > originalPrice) mainDiscount = originalPrice;

        int base = Math.max(0, originalPrice - mainDiscount);

        // 2) 스택(중복 가능 + 배타그룹 충돌 X)
        int stackDiscount = 0;
        if (stack != null) {
            boolean canStack = Boolean.TRUE.equals(stack.getStackable());
            boolean clash = isExclusiveClash(main, stack);
            if (canStack && !clash) {
                stackDiscount = calcDiscount(stack, base);
                if (stackDiscount < 0) stackDiscount = 0;
                if (stackDiscount > base) stackDiscount = base;
            } else {
                // 정책에 따라 예외를 던지고 싶다면 아래 주석 해제
                // if (clash) throw new IllegalArgumentException("동일 배타 그룹 쿠폰은 함께 사용할 수 없습니다.");
                stackDiscount = 0;
            }
        }

        // Preview 생성자가 최종 금액을 clamp하긴 하지만 여기서도 안전하게 계산
        return new Preview(originalPrice, mainDiscount, stackDiscount);
    }

    @Override
    public boolean isExclusiveClash(Coupon a, Coupon b) {
        if (a == null || b == null) return false;
        String ga = a.getExclusiveGroup();
        String gb = b.getExclusiveGroup();
        return ga != null && gb != null && Objects.equals(ga, gb);
    }
}