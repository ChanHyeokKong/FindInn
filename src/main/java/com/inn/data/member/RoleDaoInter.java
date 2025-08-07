package com.inn.data.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDaoInter extends JpaRepository<RoleDto, Long> {

    RoleDto findByRoleName(String roleName);

}