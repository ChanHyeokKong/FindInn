package com.inn.data.test;

import java.time.LocalDateTime;

import com.inn.data.member.MemberDto;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "test_result")                 // 테이블명 고정 권장
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder                                     // ✅ Lombok Builder (Groovy 아님!)
@ToString(exclude = "member")                // 순환참조 방지 (선택)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_idx", referencedColumnName = "idx")   // MemberDto.pk = idx
    private MemberDto member;

    /** 대표 성향: activity / healing / emotion / challenge */
    @Column(nullable = false, length = 20)
    private String trait;

    /** 세부 점수 */
    @Column(nullable = true)
    private Integer activityScore;

    @Column(nullable = true)
    private Integer healingScore;

    @Column(nullable = true)
    private Integer emotionScore;

    @Column(nullable = true)
    private Integer challengeScore;

    private LocalDateTime createdAt;
}