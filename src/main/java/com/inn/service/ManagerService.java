package com.inn.service;

import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MyPageDto;
import com.inn.data.member.manager.HotelRoomTypeSummaryDto;
import com.inn.data.member.manager.HotelWithManagerDto;
import com.inn.data.member.manager.ManageRepository;
import com.inn.data.rooms.RoomTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerService {

    @Autowired
    ManageRepository manageRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    HotelRepository hotelRepository;
    @Autowired
    MemberDaoInter memberDao;

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

    // HotelIdx 들로 호텔에 있는 RoomTypes 객체들 반환
    public List<RoomTypes> getRoomTypesByHotelIds(List<Long> hotelIds) {
        return manageRepository.findRoomTypesByHotelIdIn(hotelIds);
    }

    // MemberIdx(호텔관리자)를 통해 본인 호텔의 모든 예약 불러오기
    public List<MyPageDto> GetAllReservesInMyHotel(Long memberIdx){
        List<Long> memberIdxes = manageRepository.findMyHotelIdxesByMemberIdx(memberIdx);
        return manageRepository.findMyHotelReserves(memberIdxes);
    }

    public List<ChatRoomDto> getChatRoomsAndLastMessageByManager(Long managerIdx) {
        // 1. 매니저가 관리하는 호텔 ID 목록 조회
        List<Long> hotelIds = manageRepository.findMyHotelIdxesByMemberIdx(managerIdx);

        if (hotelIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 호텔 ID 목록으로 모든 채팅방 조회
        List<ChatRoomDto> chatRooms = chatRoomRepository.findAllByHotelIdxIn(hotelIds);
        List<ChatRoomDto> chatRoomInfos = new ArrayList<>();

        for (ChatRoomDto room : chatRooms) {
            // 3. 각 채팅방의 마지막 메시지 조회
            Optional<ChatDto> lastMessageOpt = chatRepository.findTopByChatRoomIdxOrderBySendTimeDesc(room.getIdx());

            // 4. 필요한 정보(호텔이름, 유저이름) 조회
            String hotelName = hotelRepository.findById(room.getHotelIdx()).map(HotelEntity::getHotelName).orElse("알 수 없는 호텔");
            String memberName = memberDao.findById(room.getMemberIdx()).map(m -> m.getMemberName()).orElse("알 수 없는 사용자");

            room.setHotelName(hotelName);
            room.setMemberName(memberName);

            if (lastMessageOpt.isPresent()) {
                ChatDto lastMessage = lastMessageOpt.get();
                room.setLastMessage(lastMessage.getMessage());
                room.setLastMessageTime(lastMessage.getSendTime().toLocalDateTime());
            } else {
                room.setLastMessage("메시지가 없습니다.");
            }
            chatRoomInfos.add(room);
        }

        // 마지막 메시지 시간 순으로 정렬 (최신이 위로)
        chatRoomInfos.sort(Comparator.comparing(ChatRoomDto::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())));

        return chatRoomInfos;
    }

}
