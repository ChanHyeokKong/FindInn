package com.inn.data.test;

import com.inn.data.member.MemberDto;
import com.inn.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final CharacterTypeRepository characterTypeRepository;
    private final TestResultRepository testResultRepository;

    @Override
    public Optional<CharacterType> calculateCharacterType(AnswerRequest answers, MemberDto member) {
        // 1. 점수 초기화 및 응답 분석: Map을 사용하여 점수 계산을 간결하게 처리합니다.
        Map<String, Integer> scores = new HashMap<>();
        for (String answer : answers.getAllAnswers()) {
            scores.merge(answer, 1, Integer::sum);
        }

        Optional<String> topTraitOptional = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey())) // 점수 동일 시 키(trait)로 정렬하여 일관성을 확보합니다.
                .findFirst()
                .map(Map.Entry::getKey);

        if (topTraitOptional.isEmpty()) {
            // 모든 답변이 비어 있거나 점수가 모두 0점인 경우, 빈 Optional을 반환합니다.
            return Optional.empty();
        }

        String topTrait = topTraitOptional.get();
        
        // 3. 캐릭터 조회: findFirstByTraitOrderByIdAsc()는 Optional을 반환합니다.
        Optional<CharacterType> characterOpt = characterTypeRepository.findFirstByTraitOrderByIdAsc(topTrait);

        // 4. 결과 저장 (회원 1회 제한): Optional.ifPresent()를 사용하여 null-safe하게 로직을 실행합니다.
        characterOpt.ifPresent(character -> {
            if (!testResultRepository.existsByMember(member)) {
                TestResult result = new TestResult();
                result.setMember(member);
                result.setTrait(topTrait);
                testResultRepository.save(result);
            }
        });

        // 5. 최종 결과 반환: Optional<CharacterType> 객체를 그대로 반환합니다.
        return characterOpt;
    }
}
