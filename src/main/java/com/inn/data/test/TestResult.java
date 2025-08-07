package com.inn.data.test;

import java.time.LocalDateTime;

import com.inn.data.member.MemberDto;

import groovy.transform.builder.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ 꼭 있어야 함
    @JoinColumn(name = "member_idx")   // ✅ member 테이블의 PK와 매핑
    private MemberDto member;

    private String trait;
    private LocalDateTime createdAt;
}

