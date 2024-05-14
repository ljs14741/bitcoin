package com.example.bitcoin.controller;

import com.example.bitcoin.dto.ChatMessageDTO;
import com.example.bitcoin.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class MainController {

    @Autowired
    private ChatMessageService chatMessageService;

    @RequestMapping("/")
    public String main(Model model) {
        List<ChatMessageDTO> messages = chatMessageService.getAllMessages();
        model.addAttribute("messages", messages);
        return "main";
    }

}
