package com.inn.controller;

import com.inn.data.coupon.UserCoupon;
import com.inn.data.coupon.UserCouponService;
import com.inn.data.coupon.UserCouponService.PreviewCoupon;
import com.inn.data.member.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-coupons") // JSON 전용 API
public class UserCouponController {

    private final UserCouponService userCouponService;

    /** 현재 로그인 사용자 (없으면 예외) */
    private MemberDto currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Object p = auth.getPrincipal();
        if (p instanceof MemberDto m) return m;
        if (p instanceof com.inn.config.CustomUserDetails cud) return cud.getMember();
        throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
    }

    // ───────────────────────────── 조회 ─────────────────────────────

    /** 사용 가능한 쿠폰 목록 (미리보기용) */
    @GetMapping("/usable")
    public ResponseEntity<List<PreviewCoupon>> listUsable(
            @RequestParam(name = "hotelId", required = false) Long hotelId,
            @RequestParam(name = "price") long price,
            @RequestParam(name = "userTrait", required = false) String userTrait
    ) {
        MemberDto me = currentUser();
        List<PreviewCoupon> list = userCouponService.listUsableCoupons(me, hotelId, price, userTrait);
        return ResponseEntity.ok(list);
    }

    /** 특정 보유 쿠폰의 할인액 계산 */
    @GetMapping("/discount")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> calculateDiscount(
            @RequestParam Long userCouponId,
            @RequestParam(required = false) Long hotelId,
            @RequestParam long price,
            @RequestParam(required = false) String userTrait
    ) {
        MemberDto me = currentUser();
        long discount = userCouponService.calculateDiscount(me, userCouponId, hotelId, price, userTrait);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "userCouponId", userCouponId,
                "discount", discount
        ));
    }

    /** 최종가 미리보기 (메인 + 스택 조합) */
    @PostMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> previewPrice(
            @RequestParam(required = false) Long hotelId,
            @RequestParam long originalPrice,
            @RequestParam(required = false) Long mainUserCouponId,
            @RequestParam(required = false) Long stackUserCouponId,
            @RequestParam(required = false) String userTrait
    ) {
        MemberDto me = currentUser();
        com.inn.service.CouponService.Preview preview =
                userCouponService.previewPrice(me, hotelId, originalPrice, mainUserCouponId, stackUserCouponId, userTrait);
        return ResponseEntity.ok(preview);
    }

    // ───────────────────────────── 상태 변경 ─────────────────────────────

    /** 쿠폰 사용 확정 (결제 성공 시) */
    @PostMapping("/use/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> confirmUse(
            @PathVariable("id") Long userCouponId,
            @RequestParam(required = false) String bookingId
    ) {
        MemberDto me = currentUser();
        boolean ok = userCouponService.confirmUse(userCouponId, me, bookingId);
        return ok
                ? ResponseEntity.ok(Map.of("ok", true))
                : ResponseEntity.badRequest().body(Map.of("ok", false, "message", "이미 사용되었거나 사용할 수 없습니다."));
    }

    /** 쿠폰 사용 되돌리기 (결제 취소/전액 환불 시) */
    @PostMapping("/revert/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> revertUse(
            @PathVariable("id") Long userCouponId,
            @RequestParam(required = false) String bookingId
    ) {
        MemberDto me = currentUser();
        boolean ok = userCouponService.revertUse(userCouponId, me, bookingId);
        return ok
                ? ResponseEntity.ok(Map.of("ok", true))
                : ResponseEntity.badRequest().body(Map.of("ok", false, "message", "쿠폰 반환에 실패했습니다."));
    }

    // ───────────────────────────── 기타/레거시 호환 ─────────────────────────────

    /** (선택) 기존 화면에서 엔티티 리스트가 필요할 때 */
    @GetMapping("/entities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserCoupon>> getMyUsableEntities(
            @RequestParam(required = false) Long hotelId,
            @RequestParam long price,
            @RequestParam(required = false) String userTrait
    ) {
        MemberDto me = currentUser();
        List<UserCoupon> list = userCouponService.getMyUsableCoupons(me, hotelId, price, userTrait);
        return ResponseEntity.ok(list);
    }

    /** (선택) 특정 이벤트로 발급된 코드 목록 */
    @GetMapping("/issued-codes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getIssuedCodes(
            @RequestParam String eventCode
    ) {
        MemberDto me = currentUser();
        return ResponseEntity.ok(userCouponService.getIssuedCodes(me, eventCode));
    }
}