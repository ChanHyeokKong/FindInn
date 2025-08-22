package com.inn.service;

import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.RoleDaoInter;
import com.inn.data.member.RoleDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    ManagerService managerService;
    @Autowired
    private MemberDaoInter memberDaoInter;
    @Autowired
    private RoleDaoInter roleDaoInter;

    // 현재 FindInn에 등록된 모든 호텔 정보 리턴
    public List<HotelWithManagerDto> getHotelList(){
        return managerService.GetAllForAdmin();
    }

    // 모든 멤버 리스트 리턴
    public List<MemberDto> getAllMember(){
        List<MemberDto> list = memberDaoInter.findAllUser();
        return list;
    }

    // 모든 매니저 리턴
    public List<MemberDto> getAllManager(){
        List<MemberDto> list = memberDaoInter.findAllManager();
        return list;
    }

    public List<MemberDto> getApplyList(){
        List<MemberDto> list = memberDaoInter.findAllByStatus(Long.valueOf(1));
        return list;
    }

    // 기존 유저 Role 제거 후 MANAGER 권한 부여
    public void giveManagerRole(Long memberIdx) {
        Optional<MemberDto> memberOptional = memberDaoInter.findById(memberIdx);

        if (memberOptional.isPresent()) {
            MemberDto memberDto = memberOptional.get();

            Optional<RoleDto> managerRoleOptional = Optional.ofNullable(roleDaoInter.findByRoleName("ROLE_MANAGER"));
            if (managerRoleOptional.isPresent()) {
                memberDto.getRoles().clear();
                memberDto.getRoles().add(managerRoleOptional.get());
                memberDto.setStatus(Long.valueOf(0));
                memberDaoInter.save(memberDto);
                System.out.println("Member " + memberIdx + " has been granted ROLE_MANAGER.");
            } else {
                System.err.println("Error: ROLE_MANAGER not found in database. Please ensure it exists.");
            }
        } else {
            System.err.println("Error: Member with ID " + memberIdx + " not found.");
        }
    }

}
