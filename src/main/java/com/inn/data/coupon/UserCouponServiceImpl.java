package com.inn.data.coupon;

import com.inn.data.coupon.Coupon;
import com.inn.data.coupon.CouponRepository;
import com.inn.data.coupon.UserCoupon;
import com.inn.data.coupon.UserCouponRepository;
import com.inn.data.member.MemberDto;
import com.inn.service.CouponService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponService couponService; // 규칙/계산 전용(이미 구현됨)

    /* ===================== 발급 ===================== */

    @Transactional
    @Override
    public UserCoupon issueByCode(MemberDto member, String couponCode, String eventCode) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalStateException("쿠폰 코드가 존재하지 않습니다."));

        LocalDateTime now = LocalDateTime.now();
        // 발급 즉시 사용 가능한 기간인지 체크
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalStateException("현재 발급할 수 없는 쿠폰입니다(기간/하드컷/상대만료).");
        }

        // 중복 발급 방지(이미 보유 → 미사용건 우선 반환)
        if (userCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                    .filter(uc -> uc.getCoupon().getId().equals(coupon.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("이미 보유했고 사용 완료된 쿠폰입니다."));
        }

        UserCoupon uc = UserCoupon.builder()
                .member(member)
                .coupon(coupon)
                .issuedAt(now)
                .used(false)
                .relatedEvent(eventCode)
                .build();
        return userCouponRepository.save(uc);
    }

    @Transactional
    @Override
    public UserCoupon issueByTrait(MemberDto member, String trait, String eventCode) {
        Coupon coupon = couponRepository.findByApplicableTrait(trait)
                .orElseThrow(() -> new IllegalStateException("해당 성향 전용 쿠폰이 정의되어 있지 않습니다."));

        LocalDateTime now = LocalDateTime.now();
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalStateException("현재 발급할 수 없는 쿠폰입니다.");
        }
        // trait 제한
        if (!couponService.matchTraitIfRequired(coupon, trait)) {
            throw new IllegalArgumentException("이 쿠폰은 " + coupon.getApplicableTrait() + " 전용입니다.");
        }

        if (userCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                    .filter(uc -> uc.getCoupon().getId().equals(coupon.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("이미 보유했고 사용 완료된 쿠폰입니다."));
        }

        UserCoupon uc = UserCoupon.builder()
                .member(member)
                .coupon(coupon)
                .issuedAt(now)
                .used(false)
                .relatedEvent(eventCode)
                .build();
        return userCouponRepository.save(uc);
    }

    /* ===================== 조회(결제 컨텍스트) ===================== */

    @Override
    public List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, int price, String userTrait) {
        LocalDateTime now = LocalDateTime.now();
        return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                .filter(uc -> {
                    Coupon c = uc.getCoupon();
                    // 기간/호텔/금액/trait 모두 통과해야 후보
                    if (!couponService.isUsableNow(c, uc.getIssuedAt(), now)) return false;
                    if (!couponService.isApplicableToHotel(c, hotelId)) return false;
                    if (!couponService.isApplicableToPrice(c, price)) return false;
                    if (!couponService.matchTraitIfRequired(c, userTrait)) return false;
                    return true;
                })
                .toList();
    }

    /* ===================== 미리보기(할인 적용) ===================== */

    @Override
    public CouponService.Preview previewPrice(MemberDto member,
                                              Long hotelId,
                                              int originalPrice,
                                              Long mainUserCouponId,
                                              Long stackUserCouponId,
                                              String userTrait) {
        LocalDateTime now = LocalDateTime.now();

        UserCoupon ucMain  = (mainUserCouponId  != null)
                ? userCouponRepository.findByIdAndMember(mainUserCouponId, member)
                    .orElseThrow(() -> new IllegalArgumentException("메인 쿠폰을 찾을 수 없습니다."))
                : null;
        UserCoupon ucStack = (stackUserCouponId != null)
                ? userCouponRepository.findByIdAndMember(stackUserCouponId, member)
                    .orElseThrow(() -> new IllegalArgumentException("스택 쿠폰을 찾을 수 없습니다."))
                : null;

        Coupon cMain  = (ucMain  != null) ? ucMain.getCoupon()  : null;
        Coupon cStack = (ucStack != null) ? ucStack.getCoupon() : null;

        // 배타 그룹 충돌 방지
        if (cMain != null && cStack != null && couponService.isExclusiveClash(cMain, cStack)) {
            throw new IllegalArgumentException("동일 배타 그룹 쿠폰은 함께 사용할 수 없습니다.");
        }

        // 메인 쿠폰 검증
        if (cMain != null) {
            if (ucMain.isUsed()) throw new IllegalStateException("이미 사용한 메인 쿠폰입니다.");
            ensureUsableInContext(ucMain, cMain, now, hotelId, originalPrice, userTrait, true);
        }

        // 스택 쿠폰 검증
        if (cStack != null) {
            if (ucStack.isUsed()) throw new IllegalStateException("이미 사용한 스택 쿠폰입니다.");
            if (!Boolean.TRUE.equals(cStack.getStackable())) {
                throw new IllegalArgumentException("이 쿠폰은 다른 쿠폰과 중복 사용할 수 없습니다.");
            }
            ensureUsableInContext(ucStack, cStack, now, hotelId, originalPrice, userTrait, false);
        }

        // 할인 미리보기(퍼센트/메인 먼저 → 스택)
        return couponService.previewFinalPrice(originalPrice, cMain, cStack);
    }

    private void ensureUsableInContext(UserCoupon uc, Coupon c, LocalDateTime now,
                                       Long hotelId, int price, String trait, boolean main) {
        if (!couponService.isUsableNow(c, uc.getIssuedAt(), now)) {
            throw new IllegalStateException((main ? "메인" : "스택") + " 쿠폰 기간이 유효하지 않습니다.");
        }
        if (!couponService.isApplicableToHotel(c, hotelId)) {
            throw new IllegalArgumentException((main ? "메인" : "스택") + " 쿠폰은 이 숙소에 적용할 수 없습니다.");
        }
        if (!couponService.isApplicableToPrice(c, price)) {
            throw new IllegalArgumentException((main ? "메인" : "스택") + " 쿠폰은 최소 금액 조건을 만족하지 않습니다.");
        }
        if (!couponService.matchTraitIfRequired(c, trait)) {
            throw new IllegalArgumentException((main ? "메인" : "스택") + " 쿠폰은 해당 성향에만 적용 가능합니다.");
        }
    }

    /* ===================== 사용 처리 ===================== */

    @Transactional
    @Override
    public void markUsed(Long userCouponId, MemberDto member) {
        UserCoupon uc = userCouponRepository.findByIdAndMember(userCouponId, member)
                .orElseThrow(() -> new IllegalStateException("쿠폰을 찾을 수 없거나 권한이 없습니다."));
        if (uc.isUsed()) return;
        uc.setUsed(true);
        uc.setUsedAt(LocalDateTime.now());
        userCouponRepository.save(uc);
    }
}