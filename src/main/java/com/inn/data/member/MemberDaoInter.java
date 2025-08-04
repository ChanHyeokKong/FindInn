package com.inn.data.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberDaoInter extends JpaRepository<MemberDto, Long> {
    @Query("SELECT m FROM MemberDto m WHERE m.m_email = :email")
    MemberDto findByEmail(@Param("email") String email);

    
}
