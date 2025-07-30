package com.inn.data.member;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="member")
public class MemberDto {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long m_idx;

    @Column(unique = true)
    private String m_email;

    @Column
    private String m_name, m_password;

    @ManyToMany(fetch = FetchType.EAGER) // EAGER 로딩으로 설정 (필요에 따라 LAZY로 변경 가능)
    @JoinTable(
        name = "member_roles", // 중간 테이블 이름
        joinColumns = @JoinColumn(name = "member_idx"), // MemberDto의 PK
        inverseJoinColumns = @JoinColumn(name = "role_name") // RoleDto의 PK (m_role)
    )
    private Set<RoleDto> roles = new HashSet<>(); // 초기화

}

