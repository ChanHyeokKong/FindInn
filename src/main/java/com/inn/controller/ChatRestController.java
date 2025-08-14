package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatRoomDto;
import com.inn.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
