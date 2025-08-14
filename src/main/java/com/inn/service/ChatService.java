package com.inn.service;

import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatRepository chatRepository;

    public List<ChatDto> getAllChatWithHotel(Long memberIdx, Long hotelIdx){
        Long chatRoomIdx = chatRoomRepository.findChatRoomIdxByMemberIdxAndHotelIdx(memberIdx, hotelIdx);
        return chatRepository.findAllByChatRoomIdxOrderBySendTime(chatRoomIdx);
    }

    public List<ChatRoomDto> getAllChatRoomForHotel(Long hotelIdx){
        return chatRoomRepository.findAllByHotelIdx(hotelIdx);
    }

    public List<ChatRoomDto> getAllChatRoomForManager(List<Long> hotelIdxes){
        return chatRoomRepository.findByHotelIdxIn(hotelIdxes);
    }

}
