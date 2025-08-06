package com.inn.data.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.inn.data.member.MemberDto;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final CharacterTypeRepository characterTypeRepository;
    private final TestResultRepository testResultRepository;

    @Override
    public CharacterType calculateCharacterType(AnswerRequest answers, MemberDto member) {
        // 1. 점수 집계
        Map<String, Integer> scoreMap = new HashMap<>();
        for (String answer : answers.getAllAnswers()) {
            scoreMap.put(answer, scoreMap.getOrDefault(answer, 0) + 1);
        }

        // 2. 최고 점수 trait
        String topTrait = Collections.max(scoreMap.entrySet(), Map.Entry.comparingByValue()).getKey();

        // 3. 저장 (1인 1회 제한)
        testResultRepository.findByMember(member).ifPresentOrElse(
            r -> {}, // 이미 있음 → 저장 안 함
            () -> {
                TestResult result = new TestResult();
                result.setTrait(topTrait);
                result.setCreatedAt(LocalDateTime.now());
                result.setMember(member);
                testResultRepository.save(result);
            }
        );

        // 4. 캐릭터 결과 반환
        return characterTypeRepository.findByTrait(topTrait);
    }
}