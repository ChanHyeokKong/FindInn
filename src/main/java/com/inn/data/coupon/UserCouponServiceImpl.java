package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import com.inn.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponService couponService; // 규칙/계산 전용(이미 구현됨)

    /* ===================== 발급 ===================== */

    // 5-1) 코드 발급: 대문자 통일 + 코드 기반 중복 체크
    @Transactional
    @Override
    public UserCoupon issueByCode(MemberDto member, String couponCode, String eventCode) {
        String code = (couponCode == null ? "" : couponCode.trim()).toUpperCase(Locale.ROOT);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다."));

        LocalDateTime now = LocalDateTime.now();
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalArgumentException("현재 발급할 수 없는 쿠폰입니다(기간/하드컷/상대만료).");
        }

        // 코드 기반으로 중복 발급 방지 (엔티티 비교 회피)
        if (userCouponRepository.existsByMemberAndCoupon_Code(member, code)) {
            return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                    .filter(uc -> code.equalsIgnoreCase(uc.getCoupon().getCode()))
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

    // 5-2) 트레이트 발급: 리포지토리 쿼리 직행 + 정규화
    @Transactional
    @Override
    public UserCoupon issueByTrait(MemberDto member, String trait, String eventCode) {
        String key  = normalizeTrait(trait);                // 'healing' 등으로 정규화
        String from = (eventCode == null ? "" : eventCode.trim());

        // DB에서 바로 조회(스트림 필터 제거)
        Coupon coupon = couponRepository
                .findTop1ByIssuedFromAndApplicableTraitIgnoreCaseOrderByValidFromDesc(from, key)
                .orElseThrow(() -> new IllegalArgumentException("해당 성향 전용 쿠폰이 정의되어 있지 않습니다."));

        LocalDateTime now = LocalDateTime.now();
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalArgumentException("현재 발급할 수 없는 쿠폰입니다.");
        }
        if (!couponService.matchTraitIfRequired(coupon, key)) {
            throw new IllegalArgumentException("이 쿠폰은 " + coupon.getApplicableTrait() + " 전용입니다.");
        }

        // 코드 기반 중복 방지
        if (userCouponRepository.existsByMemberAndCoupon_Code(member, coupon.getCode())) {
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
                .relatedEvent(from)
                .build();
        return userCouponRepository.save(uc);
    }

    /* ===================== 조회(결제 컨텍스트) ===================== */

    @Override
    public List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, int price, String userTrait) {
        LocalDateTime now = LocalDateTime.now();
        String t = normalizeTrait(userTrait);

        return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                .filter(uc -> {
                    Coupon c = uc.getCoupon();
                    if (!couponService.isUsableNow(c, uc.getIssuedAt(), now)) return false;
                    if (!couponService.isApplicableToHotel(c, hotelId)) return false;
                    if (!couponService.isApplicableToPrice(c, price)) return false;
                    return couponService.matchTraitIfRequired(c, t);
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
        String t = normalizeTrait(userTrait);

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
            ensureUsableInContext(ucMain, cMain, now, hotelId, originalPrice, t, true);
        }

        // 스택 쿠폰 검증
        if (cStack != null) {
            if (ucStack.isUsed()) throw new IllegalStateException("이미 사용한 스택 쿠폰입니다.");
            if (!Boolean.TRUE.equals(cStack.getStackable())) {
                throw new IllegalArgumentException("이 쿠폰은 다른 쿠폰과 중복 사용할 수 없습니다.");
            }
            ensureUsableInContext(ucStack, cStack, now, hotelId, originalPrice, t, false);
        }

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

    /* ===================== 기타 ===================== */

    @Override
    public List<String> getIssuedCodes(MemberDto member, String issuedFrom) {
        String from = norm(issuedFrom);
        return userCouponRepository.findAllByMember(member).stream()
                .filter(uc -> from.equals(norm(uc.getRelatedEvent())))
                .map(uc -> uc.getCoupon().getCode())
                .toList();
    }

    /* ===================== 내부 헬퍼 ===================== */

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }

    // H01/E01/한글 별칭 등을 서버 키로 정규화
    private static String normalizeTrait(String s) {
        String v = norm(s).toLowerCase(Locale.ROOT);
        if (v.startsWith("heal") || v.contains("힐")) return "healing";
        if (v.startsWith("emo")  || v.contains("감성") || v.contains("토끼")) return "emotion";
        if (v.startsWith("acti") || v.contains("액티") || v.contains("병아리")) return "activity";
        if (v.startsWith("chal") || v.contains("도전") || v.contains("코알라") || v.contains("챌")) return "challenge";
        return v;
    }
}