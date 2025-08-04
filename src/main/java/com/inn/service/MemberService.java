package com.inn.service;

import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.RoleDaoInter;
import com.inn.data.member.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService implements UserDetailsService {

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
            System.out.println("error");
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

    public MemberDto getMemberByEmail(String email) {
        return memberDao.findByEmail(email);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberDto member = memberDao.findByEmail(username);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new User(member.getM_email(), member.getM_password(),
                        member.getRoles().stream()
                              .map(role -> new SimpleGrantedAuthority(role.getM_role()))
                              .collect(Collectors.toList()));
    }
}