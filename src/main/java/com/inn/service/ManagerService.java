package com.inn.service;

import com.inn.data.hotel.HotelEntity;
import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.member.manager.ManageRepository;
import com.inn.data.rooms.RoomTypes;
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

    // 관리자 IDX를 관리할 수 있는 모든 호텔 IDX 추출
    public List<HotelEntity> GetAllMyHotel(Long memberIdx){
        return manageRepository.findHotelByMemberIdx(memberIdx);
    }

    public List<RoomTypes> getRoomTypesByHotelIds(List<Integer> hotelIds) {
        return manageRepository.findRoomTypesByHotelIdIn(hotelIds);
    }

}
