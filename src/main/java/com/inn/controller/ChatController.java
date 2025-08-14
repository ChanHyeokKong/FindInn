package com.inn.controller;

import com.inn.config.CustomUserDetails;
import com.inn.data.chat.ChatDto;
import com.inn.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatDto chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Get senderIdx from principal if available, otherwise from chatMessage
        // For now, assuming senderIdx is already set in chatMessage from client
        // Or, if using Spring Security, you can get it from headerAccessor.getUser().getName()
        // For simplicity, let's assume senderIdx is passed in chatMessage for now.

        // Save message to DB
        chatService.saveChatMessage(chatMessage.getChatRoomIdx(), chatMessage.getSenderIdx(), chatMessage.getMessage());

        // Send message to specific chat room topic
        messagingTemplate.convertAndSend("/topic/chatRoom/" + chatMessage.getChatRoomIdx(), chatMessage);
    }
}