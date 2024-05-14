package com.example.bitcoin.controller;

import com.example.bitcoin.dto.ChatMessageDTO;
import com.example.bitcoin.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(ChatMessageDTO chatMessageDTO) {
        chatMessageService.saveMessage(chatMessageDTO);
        return chatMessageDTO;
    }
}
