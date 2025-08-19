package com.inn.data.coupon;

import com.inn.data.coupon.UserCoupon;
import com.inn.service.CouponService.Preview;
import com.inn.data.member.MemberDto;

import java.util.List;

public interface UserCouponService {

	 // 발급
    UserCoupon issueByCode(MemberDto member, String couponCode, String eventCode);
    UserCoupon issueByTrait(MemberDto member, String trait, String eventCode);

    // 🔹 이벤트(issuedFrom)별 내가 발급한 쿠폰 코드 목록
    List<String> getIssuedCodes(MemberDto member, String issuedFrom);

    // 결제 전 후보/미리보기/사용 처리
    List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, int price, String userTrait);
    Preview previewPrice(MemberDto member, Long hotelId, int originalPrice,
                         Long mainUserCouponId, Long stackUserCouponId, String userTrait);
    void markUsed(Long userCouponId, MemberDto member);
}