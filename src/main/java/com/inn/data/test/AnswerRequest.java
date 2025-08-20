package com.inn.data.test;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AnswerRequest {
    private String q1;
    private String q2;
    private String q3;
    private String q4;
    private String q5;
    private String q6;

    /** null/공백 제거 + 소문자 통일 + 허용 키만 통과 */
    public List<String> getAllAnswers() {
        Set<String> allowed = Set.of("activity", "healing", "emotion", "challenge");
        return Stream.of(q1, q2, q3, q4, q5, q6)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(allowed::contains)   // 오타/이상값 방지
                .toList();
    }

    /** trait별 카운트 편의 메서드 */
    public Map<String, Integer> countByTrait() {
        int a = 0, h = 0, e = 0, c = 0;
        for (String t : getAllAnswers()) {
            switch (t) {
                case "activity"  -> a++;
                case "healing"   -> h++;
                case "emotion"   -> e++;
                case "challenge" -> c++;
            }
        }
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("activity", a);
        m.put("healing", h);
        m.put("emotion", e);
        m.put("challenge", c);
        return m;
    }
}