package com.inn.service;

import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;
import com.inn.data.test.CharacterTypeRepository;
import com.inn.data.test.TestResult;
import com.inn.data.test.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final CharacterTypeRepository characterTypeRepository;
    private final TestResultRepository testResultRepository;

    /** 동점 시 우선순위 */
    private static final List<String> PRIORITY = List.of("activity", "healing", "emotion", "challenge");

    @Override
    @Transactional
    public Optional<CharacterType> calculateCharacterType(AnswerRequest request, MemberDto member) {

        // 1) 응답 수집
        List<String> answers = Optional.ofNullable(request.getAllAnswers()).orElseGet(ArrayList::new);
        if (answers.isEmpty()) {
            log.info("[TEST] answers empty");
            return Optional.empty();
        }

        // 2) 점수 집계 (허용 키만)
        Map<String, Integer> score = new HashMap<>();
        for (String a : answers) {
            String key = (a == null ? "" : a.trim().toLowerCase());
            if (!PRIORITY.contains(key)) continue;
            score.merge(key, 1, Integer::sum);
        }
        if (score.isEmpty()) {
            log.info("[TEST] no valid traits in answers: {}", answers);
            return Optional.empty();
        }

        // 3) 대표 성향 (동점 시 PRIORITY 순)
        String topTrait = PRIORITY.stream()
                .max(Comparator.comparingInt(t -> score.getOrDefault(t, 0)))
                .orElse(null);
        if (topTrait == null) {
            log.info("[TEST] topTrait is null, score={}", score);
            return Optional.empty();
        }

        // 4) 캐릭터 조회
        CharacterType character = characterTypeRepository
                .findFirstByTraitIgnoreCase(topTrait)
                .orElse(null);
        if (character == null) {
            log.info("[TEST] character not found for trait={}", topTrait);
            return Optional.empty();
        }

        // 5) 카운트 추출
        int a = score.getOrDefault("activity", 0);
        int h = score.getOrDefault("healing", 0);
        int e = score.getOrDefault("emotion", 0);
        int c = score.getOrDefault("challenge", 0);

        // 6) 1인 1행 upsert: 있으면 갱신, 없으면 생성
        if (member != null) {
            TestResult tr = testResultRepository.findByMember(member)
                    .orElseGet(() -> TestResult.builder()
                            .member(member)
                            .createdAt(LocalDateTime.now())
                            .build());

            tr.setTrait(topTrait);       // 대표 성향 갱신
            tr.setActivityScore(a);
            tr.setHealingScore(h);
            tr.setEmotionScore(e);
            tr.setChallengeScore(c);

            if (tr.getCreatedAt() == null) {
                tr.setCreatedAt(LocalDateTime.now());
            }

            testResultRepository.save(tr);
            log.info("[TEST] upsert TestResult: member={}, trait={}, a/h/e/c={}/{}/{}/{}",
                    member.getMemberEmail(), topTrait, a, h, e, c);
        } else {
            log.info("[TEST] skip save: member is null. a/h/e/c={}/{}/{}/{}", a, h, e, c);
        }

        return Optional.of(character);
    }
}