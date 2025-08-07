
package com.inn.controller;

import com.inn.config.CustomUserDetails;
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
    private final TestResultRepository testResultRepository;
    private final CharacterTypeRepository characterTypeRepository;

    // ✅ 로그인된 사용자 가져오기
    private MemberDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getMember();  // ⬅️ 여기를 수정!
        }

        throw new IllegalStateException("로그인된 사용자 정보를 가져올 수 없습니다.");
    }

    // ✅ 질문 페이지 접근
    @GetMapping("/questions")
    public String showQuestions(Model model) {
        MemberDto member = getCurrentUser();

        Optional<TestResult> resultOpt = testResultRepository.findByMember(member);
        if (resultOpt.isPresent()) {
            String trait = resultOpt.get().getTrait();
            Optional<CharacterType> characterOpt = characterTypeRepository.findByTrait(trait); 
            if (characterOpt.isPresent()) {
                model.addAttribute("character", characterOpt.get());
            }
            return "test/test-result";  // 이미 참여한 유저
        }

        return "test/test-questions";  // 처음 참여하는 유저
    }
 // ✅ 결과 저장 및 표시
    @PostMapping("/result")
    public String showResult(@ModelAttribute AnswerRequest answers, Model model) {
        MemberDto member = getCurrentUser();

        // Optional<CharacterType> 반환값 처리
        Optional<CharacterType> resultOpt = testService.calculateCharacterType(answers, member);
        if (resultOpt.isPresent()) {
            String trait = resultOpt.get().getTrait();
            Optional<CharacterType> characterOpt = characterTypeRepository.findByTrait(trait);
            if (characterOpt.isPresent()) {
            	
            }
        }
        if (resultOpt.isPresent()) {
            CharacterType result = resultOpt.get(); // Optional에서 실제 객체를 추출
            model.addAttribute("character", result);
            return "test/test-result";
        } else {
            // 결과가 없을 경우 (예: 데이터베이스에 해당 트레잇의 캐릭터가 없을 때)
            model.addAttribute("message", "테스트 결과에 해당하는 캐릭터를 찾을 수 없습니다.");
            return "test/error-page"; // 오류를 보여줄 페이지로 리디렉션 또는 포워딩
        }
    }
}
        
