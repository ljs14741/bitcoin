package com.example.bitcoin.service;

import com.example.bitcoin.dto.ChatMessageDTO;
import com.example.bitcoin.entity.ChatMessage;
import com.example.bitcoin.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = ChatMessage.builder()
                .username(chatMessageDTO.getUsername())
                .message(chatMessageDTO.getMessage())
                .imageUrl(chatMessageDTO.getImageUrl())
                .build();
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessageDTO> getAllMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAllByOrderByCreatedDateAsc();
        return messages.stream().map(message -> ChatMessageDTO.builder()
                .username(message.getUsername())
                .message(message.getMessage())
                .imageUrl(message.getImageUrl())
                .createdDate(message.getCreatedDate())
                .build()).collect(Collectors.toList());
    }
}