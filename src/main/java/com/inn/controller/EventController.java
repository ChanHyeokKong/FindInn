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

    private static final String CAMPAIGN_TEST = "event-test";
    private static final String CAMPAIGN_AUG  = "august-pack";

    private static final List<String> PACK_CODES = List.of("AUG_7P", "AUG_10P", "AUG_5K");

    private final UserCouponService userCouponService;

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

    private MemberDto currentUserOrThrow() {
        MemberDto m = currentUserOrNull();
        if (m == null) throw new IllegalStateException("로그인이 필요합니다.");
        return m;
    }

    @GetMapping({"", "/", "/coupon-pack"})
    public String redirectToAugustPack() {
        return "redirect:/event/august-pack";
    }

    @GetMapping("/august-pack")
    public String augustPackLanding(Model model) {
        int issuedCount = 0;
        List<String> issuedCodes = Collections.emptyList();

        MemberDto me = currentUserOrNull();
        if (me != null) {
            List<String> raw = userCouponService.getIssuedCodes(me, CAMPAIGN_AUG);
            if (raw == null) raw = Collections.emptyList();

            // ✅ 코드 정규화: 공백 제거 + 대문자 + 제어문자 제거
            issuedCodes = raw.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.replaceAll("\\s+", ""))   // 모든 공백 제거
                    .map(String::toUpperCase)
                    .map(s -> s.replace("\r","").replace("\n",""))
                    .collect(Collectors.toList());

            issuedCount = issuedCodes.size();
        }

        // ✅ 안전한 매칭(대소문자/공백 무시)용 플래그 계산
        boolean has7   = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_7P"));
        boolean has10  = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_10P"));
        boolean has5k  = issuedCodes.stream().anyMatch(c -> c.equalsIgnoreCase("AUG_5K"));

        model.addAttribute("issuedCount", issuedCount);
        model.addAttribute("issuedCodes", issuedCodes);   // (호환 유지)
        model.addAttribute("has7", has7);
        model.addAttribute("has10", has10);
        model.addAttribute("has5k", has5k);
        model.addAttribute("packSize", PACK_CODES.size());

        model.addAttribute("hotelName", "호텔 코지 중산 가오슝");
        model.addAttribute("hotelCity", "가오슝");
        model.addAttribute("hotelId", 123L);
        return "event/august-pack";
    }

    /** 팩 일괄 발급 */
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

    @GetMapping(value = "/coupon/issued", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Map<String, Object> checkIssued(@RequestParam("eventCode") String eventCode) {
        MemberDto me = currentUserOrThrow();
        List<String> codes = userCouponService.getIssuedCodes(me, eventCode);
        boolean issued = codes != null && !codes.isEmpty();
        return Map.of("issued", issued);
    }

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
    public String eventList(){
        return "/event/eventList";
    }
}
