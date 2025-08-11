package com.inn.service;

import com.inn.data.coupon.Coupon;

import java.time.LocalDateTime;

public interface CouponService {

    /** 기간(절대/상대/하드컷) 기준 사용 가능 여부 */
    boolean isUsableNow(Coupon c, LocalDateTime issuedAt, LocalDateTime now);

    /** 호텔 대상 가능 여부 (전호텔 or 지정호텔 포함 여부) */
    boolean isApplicableToHotel(Coupon c, Long hotelId);

    /** 최소 주문 금액 충족 여부 */
    boolean isApplicableToPrice(Coupon c, int price);

    /** (옵션) 심리테스트 trait 제한 처리: null이면 전체 허용, 값 있으면 정확히 일치해야 통과 */
    boolean matchTraitIfRequired(Coupon c, String userTrait);

    /** 단일 쿠폰 할인액 계산 (퍼센트 상한 포함) */
    int calcDiscount(Coupon c, int originalPrice);

    /**
     * 최종가 미리보기 (메인 1장 + 스택 1장)
     * - 메인 먼저(퍼센트/정액 모두 허용), 그 다음 stackable=true 쿠폰만 추가
     * - 음수 방지
     */
    Preview previewFinalPrice(int originalPrice, Coupon main, Coupon stackable);

    /** 미리보기 결과 DTO */
    class Preview {
        public final int originalPrice;
        public final int mainDiscount;
        public final int stackDiscount;
        public final int finalPrice;

        public Preview(int originalPrice, int mainDiscount, int stackDiscount) {
            this.originalPrice = originalPrice;
            this.mainDiscount = mainDiscount;
            this.stackDiscount = stackDiscount;
            int tmp = originalPrice - mainDiscount - stackDiscount;
            this.finalPrice = Math.max(0, tmp);
        }
    }

    /** (유틸) 배타 그룹 충돌 여부: 같은 그룹이면 동시 적용 불가 */
    boolean isExclusiveClash(Coupon a, Coupon b);
}