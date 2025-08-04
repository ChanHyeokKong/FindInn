package com.inn.data.member;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode; // 이 import 추가

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
    private String m_name, m_password, m_phone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "member_roles",
        joinColumns = @JoinColumn(name = "member_idx"),
        inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    @EqualsAndHashCode.Exclude // 이 어노테이션 추가
    private Set<RoleDto> roles = new HashSet<>();

}

