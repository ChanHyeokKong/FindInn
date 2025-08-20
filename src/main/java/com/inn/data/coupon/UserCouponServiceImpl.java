package com.inn.data.coupon;

import com.inn.data.member.MemberDto;
import com.inn.service.CouponService;
import com.inn.service.CouponService.Preview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponService couponService;

    /* ===================== 발급 ===================== */

    @Transactional
    @Override
    public UserCoupon issueByCode(MemberDto member, String couponCode, String eventCode) {
        final String code = (couponCode == null ? "" : couponCode.trim()).toUpperCase(Locale.ROOT);

        final Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다."));

        final LocalDateTime now = LocalDateTime.now();
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalArgumentException("현재 발급할 수 없는 쿠폰입니다.");
        }

        // 중복 발급 방지: 보유 중이면 기존 미사용 쿠폰 반환
        if (userCouponRepository.existsByMemberAndCoupon_Code(member, code)) {
            return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                    .filter(uc -> code.equalsIgnoreCase(uc.getCoupon().getCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("이미 보유했고 사용 완료된 쿠폰입니다."));
        }

        final UserCoupon uc = UserCoupon.builder()
                .member(member)
                .coupon(coupon)
                .issuedAt(now)
                .used(false)
                .relatedEvent(norm(eventCode))
                .build();

        return userCouponRepository.save(uc);
    }

    @Transactional
    @Override
    public UserCoupon issueByTrait(MemberDto member, String trait, String eventCode) {
        final String key  = normalizeTrait(trait);
        final String from = norm(eventCode);

        final Coupon coupon = couponRepository
                .findFirstByApplicableTraitIgnoreCaseAndIssuedFrom(key, from)
                .orElseThrow(() -> new IllegalArgumentException("해당 성향 전용 쿠폰이 정의되어 있지 않습니다."));

        final LocalDateTime now = LocalDateTime.now();
        if (!couponService.isUsableNow(coupon, now, now)) {
            throw new IllegalArgumentException("현재 발급할 수 없는 쿠폰입니다.");
        }
        if (!couponService.matchTraitIfRequired(coupon, key)) {
            throw new IllegalArgumentException("이 쿠폰은 " + coupon.getApplicableTrait() + " 전용입니다.");
        }

        if (userCouponRepository.existsByMemberAndCoupon_Code(member, coupon.getCode())) {
            return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                    .filter(uc -> uc.getCoupon().getId().equals(coupon.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("이미 보유했고 사용 완료된 쿠폰입니다."));
        }

        final UserCoupon uc = UserCoupon.builder()
                .member(member)
                .coupon(coupon)
                .issuedAt(now)
                .used(false)
                .relatedEvent(from)
                .build();

        return userCouponRepository.save(uc);
    }

    /* ===================== 조회/미리보기 ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<PreviewCoupon> listUsableCoupons(MemberDto member, Long hotelId, long price, String userTrait) {
        final LocalDateTime now = LocalDateTime.now();
        final String t = normalizeTrait(userTrait);

        return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                .filter(uc -> {
                    try {
                        final Coupon c = uc.getCoupon();
                        if (!couponService.isUsableNow(c, uc.getIssuedAt(), now)) return false;
                        if (!couponService.isApplicableToHotel(c, hotelId)) return false;
                        if (!couponService.isApplicableToPrice(c, safeToInt(price))) return false;
                        return couponService.matchTraitIfRequired(c, t);
                    } catch (Exception e) {
                        log.debug("쿠폰 제외 id={}, 이유={}", uc.getId(), e.getMessage());
                        return false;
                    }
                })
                .map(uc -> {
                    final Coupon c = uc.getCoupon();
                    final long discount = couponService.calcDiscount(c, safeToInt(price)); // 서비스 로직 재사용
                    return new PreviewCoupon(
                            uc.getId(),
                            c.getCode(),
                            c.getName(),
                            discount,
                            Boolean.TRUE.equals(c.getStackable()),
                            c.getExclusiveGroup(),
                            null,   // expiresAt: 쿠폰에 만료일 필드가 있으면 매핑
                            0L,     // minSpend: 있으면 매핑
                            null    // unusableReason: 필요 시 사유 세팅
                    );
                })
                .sorted(Comparator.comparingLong(PreviewCoupon::discount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long calculateDiscount(MemberDto member, Long userCouponId, Long hotelId, long price, String userTrait) {
        final LocalDateTime now = LocalDateTime.now();
        final String t = normalizeTrait(userTrait);

        final UserCoupon uc = userCouponRepository.findByIdAndMember(userCouponId, member)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없거나 권한이 없습니다."));
        final Coupon c = uc.getCoupon();

        ensureUsableInContext(uc, c, now, hotelId, price, t);
        return couponService.calcDiscount(c, safeToInt(price));
    }

    @Override
    @Transactional(readOnly = true)
    public Preview previewPrice(MemberDto member,
                                Long hotelId,
                                long originalPrice,
                                Long mainUserCouponId,
                                Long stackUserCouponId,
                                String userTrait) {
        final LocalDateTime now = LocalDateTime.now();
        final String t = normalizeTrait(userTrait);

        final UserCoupon ucMain  = (mainUserCouponId  != null)
                ? userCouponRepository.findByIdAndMember(mainUserCouponId, member).orElse(null)
                : null;
        final UserCoupon ucStack = (stackUserCouponId != null)
                ? userCouponRepository.findByIdAndMember(stackUserCouponId, member).orElse(null)
                : null;

        final Coupon cMain  = (ucMain  != null) ? ucMain.getCoupon()  : null;
        final Coupon cStack = (ucStack != null) ? ucStack.getCoupon() : null;

        if (cMain != null && cStack != null && couponService.isExclusiveClash(cMain, cStack)) {
            throw new IllegalArgumentException("동일 배타 그룹 쿠폰은 함께 사용할 수 없습니다.");
        }
        if (cMain != null)  ensureUsableInContext(ucMain,  cMain,  now, hotelId, originalPrice, t);
        if (cStack != null) {
            if (!Boolean.TRUE.equals(cStack.getStackable())) {
                throw new IllegalArgumentException("이 쿠폰은 다른 쿠폰과 중복 사용할 수 없습니다.");
            }
            ensureUsableInContext(ucStack, cStack, now, hotelId, originalPrice, t);
        }

        // CouponService 시그니처(int)로 안전 변환
        return couponService.previewFinalPrice(safeToInt(originalPrice), cMain, cStack);
    }

    /* ===================== 상태 변경 ===================== */

    @Override
    @Transactional
    public boolean confirmUse(Long userCouponId, MemberDto member, String bookingId) {
        userCouponRepository.findByIdAndMember(userCouponId, member)
                .orElseThrow(() -> new IllegalStateException("쿠폰을 찾을 수 없거나 권한이 없습니다."));

        final int updated = userCouponRepository.markUsedIfUnused(userCouponId, member, LocalDateTime.now());
        if (updated == 0) {
            log.warn("쿠폰 사용 실패: userCouponId={}, memberId={}", userCouponId, member.getIdx());
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean revertUse(Long userCouponId, MemberDto member, String bookingId) {
        userCouponRepository.findByIdAndMember(userCouponId, member)
                .orElseThrow(() -> new IllegalStateException("쿠폰을 찾을 수 없거나 권한이 없습니다."));

        final int updated = userCouponRepository.revertUseIfUsed(userCouponId, member);
        if (updated == 0) {
            log.warn("쿠폰 반환 실패(경합/상태불일치): userCouponId={}, memberId={}", userCouponId, member.getIdx());
            return false;
        }
        return true;
    }

    /* ===================== 기타 ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<String> getIssuedCodes(MemberDto member, String issuedFrom) {
        return userCouponRepository.findCodesByMemberAndRelatedEvent(member, norm(issuedFrom));
    }

    /* ===================== 레거시(호환) ===================== */

    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public List<UserCoupon> getMyUsableCoupons(MemberDto member, Long hotelId, long price, String userTrait) {
        final LocalDateTime now = LocalDateTime.now();
        final String t = normalizeTrait(userTrait);

        // 기존 화면에서 엔티티 리스트를 기대하므로, 컨텍스트 조건만 적용해서 반환
        return userCouponRepository.findAllByMemberAndUsedFalse(member).stream()
                .filter(uc -> {
                    final Coupon c = uc.getCoupon();
                    try {
                        return couponService.isUsableNow(c, uc.getIssuedAt(), now)
                                && couponService.isApplicableToHotel(c, hotelId)
                                && couponService.isApplicableToPrice(c, safeToInt(price))
                                && couponService.matchTraitIfRequired(c, t);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    @Transactional
    public boolean markUsed(Long userCouponId, MemberDto member) {
        userCouponRepository.findByIdAndMember(userCouponId, member)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없거나 권한이 없습니다."));
        int updated = userCouponRepository.markUsedIfUnused(userCouponId, member, LocalDateTime.now());
        return updated == 1;
    }

    /* ===================== 내부 헬퍼 ===================== */

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private static String normalizeTrait(String s) {
        String v = norm(s).toLowerCase(Locale.ROOT);
        if (v.startsWith("heal") || v.contains("힐")) return "healing";
        if (v.startsWith("emo")  || v.contains("감성") || v.contains("토끼")) return "emotion";
        if (v.startsWith("acti") || v.contains("액티") || v.contains("병아리")) return "activity";
        if (v.startsWith("chal") || v.contains("도전") || v.contains("코알라") || v.contains("챌")) return "challenge";
        return v;
    }

    private static int safeToInt(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    private void ensureUsableInContext(UserCoupon uc, Coupon c, LocalDateTime now,
                                       Long hotelId, long price, String trait) {
        if (uc.isUsed()) throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        if (!couponService.isUsableNow(c, uc.getIssuedAt(), now)) {
            throw new IllegalStateException("쿠폰 기간이 유효하지 않습니다.");
        }
        if (!couponService.isApplicableToHotel(c, hotelId)) {
            throw new IllegalArgumentException("이 쿠폰은 해당 호텔에 적용할 수 없습니다.");
        }
        if (!couponService.isApplicableToPrice(c, safeToInt(price))) {
            throw new IllegalArgumentException("쿠폰 최소 금액 조건을 만족하지 않습니다.");
        }
        if (!couponService.matchTraitIfRequired(c, trait)) {
            throw new IllegalArgumentException("이 쿠폰은 해당 성향 전용입니다.");
        }
    }
}