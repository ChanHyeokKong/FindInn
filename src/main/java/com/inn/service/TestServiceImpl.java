package com.inn.service;



import com.inn.data.member.MemberDto;
import com.inn.data.test.AnswerRequest;
import com.inn.data.test.CharacterType;
import com.inn.data.test.CharacterTypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final CharacterTypeRepository characterTypeRepository;

    @Override
    public CharacterType calculateCharacterType(AnswerRequest request, MemberDto member) {
        // 1. 사용자 응답 가져오기
        String[] responses = {
                request.getQ1(),
                request.getQ2(),
                request.getQ3(),
                request.getQ4(),
                request.getQ5(),
                request.getQ6()
        };

        // 2. 점수 누적 맵 초기화
        Map<String, Integer> scoreMap = new HashMap<>();

        for (String trait : responses) {
            scoreMap.put(trait, scoreMap.getOrDefault(trait, 0) + 1);
        }

        // 3. 가장 높은 점수 trait 추출
        String topTrait = scoreMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();  // ex: "healing"

        // 4. 해당 trait 기반 캐릭터 반환
        return characterTypeRepository.findByTrait(topTrait);
    }
}