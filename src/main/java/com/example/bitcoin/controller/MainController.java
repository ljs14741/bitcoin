package com.example.bitcoin.controller;

import com.example.bitcoin.dto.ChatMessageDTO;
import com.example.bitcoin.dto.LottoDTO;
import com.example.bitcoin.dto.VisitorDTO;
import com.example.bitcoin.repository.LottoRepository;
import com.example.bitcoin.service.ChatMessageService;
import com.example.bitcoin.service.VisitorService;
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

    @Autowired
    private VisitorService visitorService;

    @RequestMapping("/")
    public String main(Model model) {
        // 채팅 조회
        List<ChatMessageDTO> messages = chatMessageService.getAllMessages();
        model.addAttribute("messages", messages);

        // 방문자 조회
        visitorService.incrementVisitorCount();
        VisitorDTO visitorDTO = visitorService.getVisitorCount();
        model.addAttribute("dailyCount", visitorDTO.getDailyCount());
        model.addAttribute("totalCount", visitorDTO.getTotalCount());
        return "main";
    }

}
