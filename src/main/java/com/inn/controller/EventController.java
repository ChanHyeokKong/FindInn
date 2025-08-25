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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    // 캠페인 키
    private static final String CAMPAIGN_TEST = "event-test";
    private static final String CAMPAIGN_AUG  = "august-pack";

    // ✅ 프론트 템플릿과 동일한 쿠폰 코드로 통일 (AUG_5K -> SUMMER_5K)
    private static final List<String> PACK_CODES = List.of("AUG_7P", "AUG_10P", "AUG_5K");

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

    /* ===================== 페이지 라우팅 ===================== */

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
            List<String> raw = userCouponService.getIssuedCodes(me, CAMPAIGN_AUG);
            if (raw == null) raw = Collections.emptyList();

            // 코드 정규화: 공백/개행 제거 + 대문자
            issuedCodes = raw.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.replaceAll("\\s+", ""))
                    .map(String::toUpperCase)
                    .map(s -> s.replace("\r", "").replace("\n", ""))
                    .collect(Collectors.toList());

            issuedCount = issuedCodes.size();
        }

        // 템플릿에서 쓰기 쉬운 플래그
        boolean has7  = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_7P"));
        boolean has10 = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_10P"));
        boolean has5k = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_5K"));

        model.addAttribute("issuedCount", issuedCount);
        model.addAttribute("issuedCodes", issuedCodes); // 하위호환
        model.addAttribute("has7", has7);
        model.addAttribute("has10", has10);
        model.addAttribute("has5k", has5k);
        model.addAttribute("packSize", PACK_CODES.size());

        // 데모 호텔 정보
        model.addAttribute("hotelName", "호텔 코지 중산 가오슝");
        model.addAttribute("hotelCity", "가오슝");
        model.addAttribute("hotelId", 123L);

        return "event/august-pack";
    }

    /* ===================== API ===================== */

    /** 팩 일괄 발급 (PRG) */
    @PostMapping("/coupon/issue-pack")
    @PreAuthorize("isAuthenticated()")
    public String issuePack(RedirectAttributes ra) {
        MemberDto me = currentUserOrThrow();
        int ok = 0, already = 0, fail = 0;

        for (String code : PACK_CODES) {
            try {
                userCouponService.issueByCode(me, code, CAMPAIGN_AUG);
                ok++;
            } catch (IllegalStateException ex) {
                if ("ALREADY_ISSUED".equals(ex.getMessage())) already++;
                else fail++;
            } catch (Exception ex) {
                log.error("issuePack error for {}", code, ex);
                fail++;
            }
        }
        String msg = "쿠폰팩 처리: 신규 " + ok + "개, 보유 " + already + "개" + (fail > 0 ? (", 실패 " + fail + "개") : "") + " 🎉";
        ra.addFlashAttribute("toast", msg);
        return "redirect:/event/august-pack";
    }

    /** (AJAX) 해당 이벤트/캠페인에서 이미 발급한 게 있는지 조회 */
    @GetMapping(value = "/coupon/issued", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Map<String, Object> checkIssued(@RequestParam("eventCode") String eventCode) {
        MemberDto me = currentUserOrThrow();
        List<String> codes = userCouponService.getIssuedCodes(me, eventCode);
        boolean issued = codes != null && !codes.isEmpty();
        return Map.of("issued", issued);
    }

    /** (AJAX) 심리테스트 결과 맞춤 쿠폰 발급 */
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

    /** (AJAX) 전 호텔 5% 발급 예시 */
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

    /** 코드로 발급 — PRG (8월팩) */
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

    /** 코드로 발급 — AJAX 버전 (선택) */
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

    @GetMapping("/eventlist")
    public String eventList() {
        return "/event/eventList";
    }
}
