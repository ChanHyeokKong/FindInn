package com.inn.data.test;

import java.time.LocalDateTime;

import com.inn.data.member.MemberDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trait;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "member_id")
    private MemberDto member;
}


