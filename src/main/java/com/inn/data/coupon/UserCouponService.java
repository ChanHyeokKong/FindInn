package com.inn.data.coupon;

import com.inn.data.coupon.UserCoupon;
import com.inn.service.CouponService.Preview;
import com.inn.data.member.MemberDto;

import java.util.List;

public interface UserCouponService {

    // 발급
    UserCoupon issueByCode(MemberDto member, String couponCode, String eventCode);
    UserCoupon issueByTrait(MemberDto member, String trait, String eventCode);

    // 결제 전: 내 쿠폰 후보 조회(지금 결제 컨텍스트에 실제 사용 가능한 것만)
    List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, int price, String userTrait);

    // 미리보기: 메인(배타 그룹 1장) + 스택(중복 허용 1장)
    Preview previewPrice(MemberDto member, Long hotelId, int originalPrice,
                         Long mainUserCouponId, Long stackUserCouponId, String userTrait);

    // 결제 성공 시 사용 처리
    void markUsed(Long userCouponId, MemberDto member);
}