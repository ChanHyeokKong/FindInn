package com.inn.service;

import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.RoleDaoInter;
import com.inn.data.member.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    MemberDaoInter memberDao;

    @Autowired
    RoleDaoInter roleDao;

    PasswordEncoder pe = new BCryptPasswordEncoder();
    @Autowired
    private MemberDaoInter memberDaoInter;

    public void signup(MemberDto memberDto) {
        memberDto.setM_password(pe.encode(memberDto.getM_password()));

        // 기본 역할 (ROLE_USER)을 데이터베이스에서 조회하여 추가
        Optional<RoleDto> userRoleOptional = roleDao.findById("ROLE_USER");
        if (userRoleOptional.isPresent()) {
            memberDto.getRoles().add(userRoleOptional.get());
        } else {
            // ROLE_USER 역할이 데이터베이스에 없는 경우 처리 (예: 예외 발생 또는 기본 역할 생성)
            // 프로토타입 단계에서는 간단히 로그를 남기거나, 미리 데이터베이스에 ROLE_USER를 추가해야 합니다.
            System.err.println("Error: ROLE_USER not found in database. Please ensure it exists.");
            // 또는 RoleDto를 생성하여 저장하는 로직 추가
            // RoleDto userRole = new RoleDto();
            // userRole.setM_role("ROLE_USER");
            // roleDao.save(userRole);
            // memberDto.getRoles().add(userRole);
        }

        memberDao.save(memberDto);
    }

    public void giveManagerRole(Long memberIdx) {
        Optional<MemberDto> memberOptional = memberDao.findById(memberIdx);

        if (memberOptional.isPresent()) {
            MemberDto memberDto = memberOptional.get();

            Optional<RoleDto> managerRoleOptional = roleDao.findById("ROLE_MANAGER");
            if (managerRoleOptional.isPresent()) {
                memberDto.getRoles().add(managerRoleOptional.get());
                memberDao.save(memberDto);
                System.out.println("Member " + memberIdx + " has been granted ROLE_MANAGER.");
            } else {
                System.err.println("Error: ROLE_MANAGER not found in database. Please ensure it exists.");
            }
        } else {
            System.err.println("Error: Member with ID " + memberIdx + " not found.");
        }
    }

    public List<MemberDto> getAllMembers() {
        List<MemberDto> list = memberDao.findAll();
        return list;
    }

    public List<RoleDto> getAllRoles() {
        List<RoleDto> list = roleDao.findAll();
        return list;
    }


}
