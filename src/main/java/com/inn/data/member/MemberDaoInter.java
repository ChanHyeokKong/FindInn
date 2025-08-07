package com.inn.data.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberDaoInter extends JpaRepository<MemberDto, Long> {
//    @Query("SELECT m FROM MemberDto m WHERE m.m_email = :email")
    MemberDto findByMemberEmail(@Param("email") String email);

    @Query("SELECT m FROM MemberDto m JOIN m.roles r WHERE r.roleName = 'ROLE_USER'")
    List<MemberDto> findAllUser();

    @Query("SELECT m FROM MemberDto m JOIN m.roles r WHERE r.roleName = 'ROLE_MANAGER'")
    List<MemberDto> findAllManager();

}
