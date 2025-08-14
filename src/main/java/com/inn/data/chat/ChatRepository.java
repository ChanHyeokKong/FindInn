package com.inn.data.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatDto, Long> {

    List<ChatDto> findAllBySenderIdx(Long senderIdx); // Changed from findAllBySender(Long sender)

    List<ChatDto> findAllByChatRoomIdxOrderBySendTime(Long chatRoomIdx);

    Optional<ChatDto> findTopByChatRoomIdxOrderBySendTimeDesc(Long chatRoomIdx);
}
