package com.inn.data.test;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.data.member.MemberDto;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByMember(MemberDto member);
    boolean existsByMember(MemberDto member);
}