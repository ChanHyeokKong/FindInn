package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.member.MemberDto;
import com.inn.data.coupon.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class CouponIssueController {

    private final UserCouponService userCouponService;

    private MemberDto me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof MemberDto) {
            return (MemberDto) principal;
        }
        throw new IllegalStateException("로그인이 필요합니다.");
    }

    /** 심리테스트 결과 전용 — trait 기반 발급 */
    @PostMapping("/test/issue-coupon")
    @PreAuthorize("isAuthenticated()")
    public String issueTrait(@RequestParam("trait") String trait,
                             RedirectAttributes ra) {
        userCouponService.issueByTrait(me(), trait, "event-test");
        ra.addFlashAttribute("toast", "부캐 전용 쿠폰이 발급되었습니다 🎁");
        return "redirect:/test/questions"; // 또는 결과 페이지로
    }

    /** 전 호텔 5% 발급(예: GLOBAL_5P) */
    @PostMapping("/coupon/issue-global")
    @PreAuthorize("isAuthenticated()")
    public String issueGlobal(RedirectAttributes ra) {
        userCouponService.issueByCode(me(), "GLOBAL_5P", "event-test");
        ra.addFlashAttribute("toast", "전 호텔 5% 쿠폰이 발급되었습니다 🎁");
        return "redirect:/test/questions"; // 원하는 곳으로 리다이렉트
    }
}
