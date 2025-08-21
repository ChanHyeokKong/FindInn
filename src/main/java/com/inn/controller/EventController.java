package com.inn.controller;

import com.inn.data.coupon.UserCouponService;
import com.inn.data.member.MemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/event") // 모든 경로 /event 하위
public class EventController {

    // ✅ 캠페인 키 상수화
    private static final String CAMPAIGN_TEST = "event-test";
    private static final String CAMPAIGN_AUG  = "august-pack";

    private final UserCouponService userCouponService;

    /** 현재 로그인 사용자 (없으면 null) */
    private MemberDto currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof MemberDto m) return m;
        if (p instanceof com.inn.config.CustomUserDetails cud) return cud.getMember();
        return null;
    }

    /** 현재 로그인 사용자 (없으면 예외) — POST에서 사용 */
    private MemberDto currentUserOrThrow() {
        MemberDto m = currentUserOrNull();
        if (m == null) throw new IllegalStateException("로그인이 필요합니다.");
        return m;
    }

    /* ===================== 페이지 ===================== */

    /** 루트/구명칭도 새 경로로 정리 */
    @GetMapping({"", "/", "/coupon-pack"})
    public String redirectToAugustPack() {
        return "redirect:/event/august-pack";
    }

    /** 8월 쿠폰팩 랜딩 */
    @GetMapping("/august-pack")
    public String augustPackLanding(Model model) {
        int issuedCount = 0;
        List<String> issuedCodes = Collections.emptyList();

        MemberDto me = currentUserOrNull();
        if (me != null) {
            issuedCodes = userCouponService.getIssuedCodes(me, CAMPAIGN_AUG);
            if (issuedCodes == null) issuedCodes = Collections.emptyList();
            issuedCount = issuedCodes.size();
        }

        model.addAttribute("issuedCount", issuedCount);
        model.addAttribute("issuedCodes", issuedCodes);
        model.addAttribute("hotelName", "호텔 코지 중산 가오슝");
        model.addAttribute("hotelCity", "가오슝");
        model.addAttribute("hotelId", 123L);
        return "event/august-pack";
    }

    /** 심리테스트 결과 페이지(필요 시) */
    @GetMapping("/test-landing")
    public String testLanding() {
        return "event/test-result"; // 템플릿 파일 존재해야 함
    }

    /* ===================== 동기화 체크 API ===================== */

    /** (프론트 초기 동기화) 이 이벤트에서 이미 쿠폰을 보유했는지 여부 */
    @GetMapping(value = "/coupon/issued", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Map<String, Object> checkIssued(@RequestParam("eventCode") String eventCode) {
        MemberDto me = currentUserOrThrow();
        List<String> codes = userCouponService.getIssuedCodes(me, eventCode);
        boolean issued = codes != null && !codes.isEmpty();
        return Map.of("issued", issued);
    }

    /* ===================== API (발급) ===================== */

    /** 결과(부캐) 맞춤 쿠폰 — JSON (페이지 이동 없음) */
    @PostMapping(value = "/test/issue-coupon", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issueTrait(@RequestParam("trait") String trait) {
        try {
            userCouponService.issueByTrait(currentUserOrThrow(), trait, CAMPAIGN_TEST);
            return ResponseEntity.ok(Map.of("ok", true, "message", "부캐 전용 쿠폰이 발급되었습니다 🎁"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            if ("ALREADY_ISSUED".equals(ex.getMessage())) {
                return ResponseEntity.badRequest().body(
                        Map.of("ok", false, "code", "ALREADY_ISSUED", "message", "이미 쿠폰을 발급받았습니다.")
                );
            }
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("issueTrait error", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "message", "처리 중 오류가 발생했습니다."));
        }
    }

    /** 전 호텔 5% — JSON (페이지 이동 없음) */
    @PostMapping(value = "/coupon/issue-global", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issueGlobalJson() {
        try {
            userCouponService.issueByCode(currentUserOrThrow(), "GLOBAL_5P", CAMPAIGN_TEST);
            return ResponseEntity.ok(Map.of("ok", true, "message", "전 호텔 5% 쿠폰이 발급되었습니다 🎁"));
        } catch (IllegalStateException ex) {
            if ("ALREADY_ISSUED".equals(ex.getMessage())) {
                return ResponseEntity.badRequest().body(
                        Map.of("ok", false, "code", "ALREADY_ISSUED", "message", "이미 쿠폰을 발급받았습니다.")
                );
            }
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("issueGlobalJson error", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "message", "처리 중 오류가 발생했습니다."));
        }
    }

    /** 코드로 발급 — PRG 패턴 유지 (8월팩) */
    @PostMapping("/coupon/issue-by-code")
    @PreAuthorize("isAuthenticated()")
    public String issueByCode(@RequestParam("code") String code, RedirectAttributes ra) {
        try {
            userCouponService.issueByCode(currentUserOrThrow(), code, CAMPAIGN_AUG);
            ra.addFlashAttribute("toast", "쿠폰이 발급되었습니다 🎁");
        } catch (IllegalStateException ex) {
            if ("ALREADY_ISSUED".equals(ex.getMessage())) {
                ra.addFlashAttribute("toast", "이미 쿠폰을 발급받았습니다.");
            } else {
                ra.addFlashAttribute("toast", ex.getMessage());
            }
        } catch (Exception ex) {
            log.error("issueByCode error", ex);
            ra.addFlashAttribute("toast", "처리 중 오류가 발생했습니다.");
        }
        return "redirect:/event/august-pack";
    }

    /** (선택) 8월팩도 AJAX로 쓰고 싶을 때 JSON 버전 제공 */
    @PostMapping(value = "/coupon/issue-by-code.json", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issueByCodeJson(@RequestParam("code") String code) {
        try {
            userCouponService.issueByCode(currentUserOrThrow(), code, CAMPAIGN_AUG);
            return ResponseEntity.ok(Map.of("ok", true, "message", "쿠폰이 발급되었습니다 🎁"));
        } catch (IllegalStateException ex) {
            if ("ALREADY_ISSUED".equals(ex.getMessage())) {
                return ResponseEntity.badRequest().body(
                        Map.of("ok", false, "code", "ALREADY_ISSUED", "message", "이미 쿠폰을 발급받았습니다.")
                );
            }
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("issueByCodeJson error", ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("ok", false, "message", "처리 중 오류가 발생했습니다."));
        }
    }

    /** (레거시) 외부 폼 호환 — PRG 유지 */
    @PostMapping("/coupon/issue")
    @PreAuthorize("isAuthenticated()")
    public String legacyIssueApi(@RequestParam(value = "couponCode", required = false) String couponCode,
                                 @RequestParam(value = "relatedEvent", required = false) String relatedEvent,
                                 RedirectAttributes ra) {
        String code = couponCode == null ? "" : couponCode.trim();
        String event = (relatedEvent == null || relatedEvent.isBlank()) ? CAMPAIGN_AUG : relatedEvent.trim();
        try {
            if (code.isBlank()) throw new IllegalStateException("쿠폰 코드를 입력해주세요.");
            userCouponService.issueByCode(currentUserOrThrow(), code, event);
            ra.addFlashAttribute("toast", "쿠폰이 발급되었습니다 🎁");
        } catch (IllegalStateException ex) {
            if ("ALREADY_ISSUED".equals(ex.getMessage())) {
                ra.addFlashAttribute("toast", "이미 쿠폰을 발급받았습니다.");
            } else {
                ra.addFlashAttribute("toast", ex.getMessage());
            }
        } catch (Exception ex) {
            log.error("legacyIssueApi error", ex);
            ra.addFlashAttribute("toast", "처리 중 오류가 발생했습니다.");
        }
        return "redirect:/event/august-pack";
    }

    @GetMapping("/eventlist")
    public String eventList(){
        return "event/eventList";
    }
}

