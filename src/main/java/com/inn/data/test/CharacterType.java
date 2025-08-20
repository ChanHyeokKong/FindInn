package com.inn.data.test;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterType {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long idx;

	    private String code; // ex) E01, E02

	    private String name; // ex) 조용한 힐러
	    private String description; // 결과 페이지 설명용

	    /** 핵심 키워드: healing, activity, emotion, challenge */
	    @Column(nullable = false)
	    private String trait;

	    // 점수는 int/Integer가 더 자연스러움
	    private Integer activityLevel;
	    private Integer healingLevel;
	    private Integer challengeLevel;
	    private Integer emotionLevel;

	    private String imageUrl;

	    @ElementCollection
	    private List<String> recommendedAccommodations; // 숙소 추천 텍스트
	}