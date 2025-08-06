package com.inn.service;

import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.member.manager.ManageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerService {

    @Autowired
    ManageRepository manageRepository;

    // 방 관리용 방의 관한 모든 정보 조회
    public List<HotelWithManagerDto> getAllMyHotel(Long memberIdx){
        return manageRepository.findHotelRoomsAndTypesByMemberIdx(memberIdx);
    }

    //호텔과 매니저 출력 For Admin
    public List<HotelWithManagerDto> GetAllForAdmin(){
        return manageRepository.findAllWithManagerName();
    }

    // 호텔 관리용 -> Description 기반 분할
    public List<HotelRoomTypeSummaryDto> GetAllRoomGroupByDescription(Long memberIdx){
        return manageRepository.findHotelRoomTypesByMemberIdx(memberIdx);
    }



}
