package com.inn.service;

import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    ManagerService managerService;
    @Autowired
    private MemberDaoInter memberDaoInter;

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

}
