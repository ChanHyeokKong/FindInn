package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;
import com.inn.data.test.TestResult;
import com.inn.data.test.CharacterTypeRepository;
import com.inn.data.test.TestResultRepository;
import com.inn.service.TestService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestService testService;
    private final TestResultRepository testResultRepository;
    private final CharacterTypeRepository characterTypeRepository;

    /** 로그인 사용자 조회(없으면 null). */
    private MemberDto currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return null;
        Object principal = auth.getPrincipal();
        if (principal == null) return null;
        if (principal instanceof CustomUserDetails cud) return cud.getMember();
        if (principal instanceof MemberDto m) return m;
        return null;
    }

    /** 캐릭터 능력치(level)를 0~100% 맵으로 정규화 */
    private Map<String, Integer> levelsOf(CharacterType ch) {
        int a = ch.getActivityLevel()  == null ? 0 : ch.getActivityLevel().intValue();
        int h = ch.getHealingLevel()   == null ? 0 : ch.getHealingLevel().intValue();
        int c = ch.getChallengeLevel() == null ? 0 : ch.getChallengeLevel().intValue();
        int e = ch.getEmotionLevel()   == null ? 0 : ch.getEmotionLevel().intValue();

        // 스케일 자동 감지(<=5, <=10, <=100) → 0~100으로 정규화
        int max  = Math.max(1, Math.max(Math.max(a, h), Math.max(c, e)));
        int base = (max <= 5) ? 5 : (max <= 10) ? 10 : (max <= 100) ? 100 : max;

        Map<String, Integer> lv = new LinkedHashMap<>();
        lv.put("activity",  (int) Math.round(a * 100.0 / base));
        lv.put("healing",   (int) Math.round(h * 100.0 / base));
        lv.put("challenge", (int) Math.round(c * 100.0 / base));
        lv.put("emotion",   (int) Math.round(e * 100.0 / base));
        return lv;
    }

    /** 질문 페이지: 로그인 유저가 과거 결과가 있으면 결과로 이동(캐릭터 레벨 기반 표시) */
    @GetMapping("/questions")
    public String showQuestions(Model model, HttpSession session) {
        MemberDto member = currentUserOrNull();
        if (member == null) return "redirect:/login";

        Optional<TestResult> resultOpt = testResultRepository.findByMember(member);
        if (resultOpt.isPresent()) {
            TestResult tr = resultOpt.get();
            String trait = tr.getTrait();

            // 캐릭터 조회(대소문자 무시)
            CharacterType character = characterTypeRepository
                    .findFirstByTraitIgnoreCase(trait)
                    .orElseGet(() -> CharacterType.builder().trait(trait).build());

            // 모델/세션 세팅
            session.setAttribute("character", character);
            session.setAttribute("trait", trait);

            model.addAttribute("character", character);
            model.addAttribute("topTrait", trait);

            // ✅ 캐릭터 능력치로 4개 막대 표시
            model.addAttribute("levels", levelsOf(character));

            return "test/test-result";
        }

        // 과거 결과가 없으면 질문 화면
        return "test/test-questions";
    }

    /** 결과 계산: 캐릭터 결정 후 레벨 기반으로 표시 */
    @PostMapping("/result")
    public String showResult(@ModelAttribute AnswerRequest answers, Model model) {
        MemberDto member = currentUserOrNull();
        if (member == null) return "redirect:/login";

        // 1) 캐릭터 결정(서비스에서 결정 및 test_result 업서트 저장)
        Optional<CharacterType> resultOpt = testService.calculateCharacterType(answers, member);
        if (resultOpt.isEmpty()) {
            model.addAttribute("errorMessage", "테스트 결과에 해당하는 캐릭터를 찾을 수 없습니다.");
            return "test/test-result";
        }

        CharacterType character = resultOpt.get();
        model.addAttribute("character", character);
        model.addAttribute("topTrait", character.getTrait());

        // 2) 캐릭터 능력치로 4개 막대 표시
        model.addAttribute("levels", levelsOf(character));

        return "test/test-result";
    }
}