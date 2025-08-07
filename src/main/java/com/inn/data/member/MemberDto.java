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
    private Long idx;

    @Column(unique = true)
    private String memberEmail;

    @Column
    private String memberName, memberPassword, memberPhone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "member_roles",
        joinColumns = @JoinColumn(name = "member_idx", referencedColumnName = "idx"),
        inverseJoinColumns = @JoinColumn(name = "role_idx", referencedColumnName = "idx")
    )
    @EqualsAndHashCode.Exclude // 이 어노테이션 추가
    private Set<RoleDto> roles = new HashSet<>();

}

