package com.inn.service;

import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatRepository chatRepository;

    public List<ChatRoomDto> getAllChatRoomForHotel(Long hotelIdx){
        return chatRoomRepository.findAllByHotelIdx(hotelIdx);
    }

    public List<ChatRoomDto> getAllChatRoomForManager(List<Long> hotelIdxes){
        return chatRoomRepository.findAllByHotelIdxIn(hotelIdxes);
    }

    // New method to save a chat message
    public ChatDto saveChatMessage(Long chatRoomIdx, Long senderIdx, String messageContent) {
        ChatDto chatMessage = new ChatDto();
        chatMessage.setChatRoomIdx(chatRoomIdx);
        chatMessage.setSenderIdx(senderIdx);
        chatMessage.setMessage(messageContent);
        chatMessage.setSendTime(new Timestamp(System.currentTimeMillis()));
        return chatRepository.save(chatMessage);
    }

    // Modified method to get an existing chat room
    public Optional<ChatRoomDto> getExistingChatRoom(Long memberIdx, Long hotelIdx) {
        // Use the new method name
        Long chatRoomIdx = chatRoomRepository.findIdxByMemberIdxAndHotelIdx(memberIdx, hotelIdx);
        if (chatRoomIdx != null) {
            return chatRoomRepository.findById(chatRoomIdx);
        }
        return Optional.empty();
    }

    // New method to create a chat room and save the first message
    public ChatRoomDto createChatRoomAndSaveFirstMessage(Long memberIdx, Long hotelIdx, String firstMessageContent) {
        // Create new chat room
        ChatRoomDto newChatRoom = new ChatRoomDto();
        newChatRoom.setMemberIdx(memberIdx);
        newChatRoom.setHotelIdx(hotelIdx);
        ChatRoomDto savedChatRoom = chatRoomRepository.save(newChatRoom);

        // Save the first message
        saveChatMessage(savedChatRoom.getIdx(), memberIdx, firstMessageContent);

        return savedChatRoom;
    }

    // New method to get chat history by chat room index
    public List<ChatDto> getChatHistoryByChatRoomIdx(Long chatRoomIdx) {
        return chatRepository.findAllByChatRoomIdxOrderBySendTime(chatRoomIdx);
    }
}