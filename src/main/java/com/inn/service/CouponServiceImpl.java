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
            // 하드컷 보강
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
        if (c.getApplicableTrait() == null) return true;      // 제한 없음 → 통과
        return Objects.equals(c.getApplicableTrait(), userTrait);
    }

    @Override
    public int calcDiscount(Coupon c, int originalPrice) {
        if (c == null) return 0;
        return switch (c.getType()) {
            case PERCENT -> {
                int d = originalPrice * c.getDiscountValue() / 100;
                Integer cap = c.getMaxDiscountAmount();
                if (cap != null) d = Math.min(d, cap);
                yield d;
            }
            case AMOUNT  -> c.getDiscountValue();
        };
    }

    @Override
    public Preview previewFinalPrice(int originalPrice, Coupon main, Coupon stack) {
        int price = originalPrice;

        // 메인(배타그룹에서 1장 선택되어 있다고 가정)
        int mainDiscount = (main != null) ? calcDiscount(main, price) : 0;
        price -= mainDiscount;

        // 스택(중복 허용 쿠폰만)
        int stackDiscount = 0;
        if (stack != null && Boolean.TRUE.equals(stack.getStackable())) {
            stackDiscount = calcDiscount(stack, price);
            price -= stackDiscount;
        }

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