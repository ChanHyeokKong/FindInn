
package com.inn.controller;



import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;
import com.inn.data.test.TestResult;
import com.inn.data.test.CharacterTypeRepository;
import com.inn.data.test.TestResultRepository;
import com.inn.service.TestService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestService testService;
    private final com.inn.data.test.TestResultRepository testResultRepository;
    private final com.inn.data.test.CharacterTypeRepository characterTypeRepository;

    // ✅ 로그인된 사용자 가져오기
    private MemberDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MemberDto) authentication.getPrincipal(); // MemberDto가 UserDetails를 구현한 경우
    }

    // ✅ 질문 페이지 접근
    @GetMapping("/questions")
    public String showQuestions(Model model) {
        MemberDto member = getCurrentUser();

        Optional<TestResult> resultOpt = testResultRepository.findByMember(member);
        if (resultOpt.isPresent()) {
            String trait = resultOpt.get().getTrait();
            CharacterType character = characterTypeRepository.findByTrait(trait);
            model.addAttribute("character", character);
            return "test/test-result";  // 이미 참여한 유저
        }

        return "test/test-questions";  // 처음 참여하는 유저
    }

 // ✅ 결과 저장 및 표시
    @PostMapping("/result")
    public String showResult(@ModelAttribute AnswerRequest answers, Model model) {
        MemberDto member = getCurrentUser();
        CharacterType result = testService.calculateCharacterType(answers, member);
        model.addAttribute("character", result);
        return "test/test-result";
    }
}