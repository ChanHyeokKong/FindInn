package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRoomDto;
import com.inn.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

@RestController
public class ChatRestController {

    @Autowired
    ChatService chatService;

    @GetMapping("/chatRoom/reload")
    public List<ChatRoomDto> GetHotelChatRoom(@AuthenticationPrincipal CustomUserDetails currUser ,Long hotelIdx){

        if(hotelIdx == 0){
            currUser.getIdx();
        }

        return chatService.getAllChatRoomForHotel(hotelIdx);
    }

    // New endpoint to get existing chat room ID
    @GetMapping("/chat/room/{hotelIdx}")
    public ResponseEntity<Long> getChatRoomId(@PathVariable Long hotelIdx, @AuthenticationPrincipal CustomUserDetails currUser) {
        if (currUser == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        Optional<ChatRoomDto> chatRoom = chatService.getExistingChatRoom(currUser.getIdx(), hotelIdx);
        return chatRoom.map(room -> ResponseEntity.ok(room.getIdx()))
                       .orElse(ResponseEntity.ok(-1L)); // -1L indicates no existing chat room
    }

    // New endpoint to handle sending the first message and creating a chat room
    @PostMapping("/chat/message")
    public ResponseEntity<Long> sendFirstMessage(@RequestBody ChatDto chatMessage, @AuthenticationPrincipal CustomUserDetails currUser) {
        if (currUser == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        // Assuming ChatDto contains hotelIdx and message content
        ChatRoomDto newChatRoom = chatService.createChatRoomAndSaveFirstMessage(currUser.getIdx(), chatMessage.getHotelIdx(), chatMessage.getMessage());
        return ResponseEntity.ok(newChatRoom.getIdx());
    }

    // New endpoint to get chat history for a specific chat room
    @GetMapping("/chat/history/{chatRoomIdx}")
    public ResponseEntity<List<ChatDto>> getChatHistory(@PathVariable Long chatRoomIdx, @AuthenticationPrincipal CustomUserDetails currUser) {
        if (currUser == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        // Use the new method to get chat history by chat room index
        List<ChatDto> chatHistory = chatService.getChatHistoryByChatRoomIdx(chatRoomIdx);
        return ResponseEntity.ok(chatHistory);
    }
}