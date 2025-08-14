package com.inn.data.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomDto, Long> {

    List<ChatRoomDto> findAllByHotelIdx(Long hotelIdx);

    Long findChatRoomIdxByMemberIdxAndHotelIdx(Long memberIdx, Long hotelIdx);

    List<ChatRoomDto> findByHotelIdxIn(List<Long> hotelIdxes);

}
