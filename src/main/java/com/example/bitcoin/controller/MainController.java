package com.example.bitcoin.controller;

import com.example.bitcoin.dto.ChatMessageDTO;
import com.example.bitcoin.dto.LottoDTO;
import com.example.bitcoin.dto.VisitorDTO;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.repository.LottoRepository;
import com.example.bitcoin.service.ChatMessageService;
import com.example.bitcoin.service.UserService;
import com.example.bitcoin.service.VisitorService;
import com.example.bitcoin.service.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@Slf4j
public class MainController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;
    @RequestMapping("/")
    public String main(Model model, HttpSession session,HttpServletRequest request) {
        // 채팅 조회
        List<ChatMessageDTO> messages = chatMessageService.getAllMessages();
        model.addAttribute("messages", messages);

        // 카카오 로그인이 아닐때만 세션 30분 설정
        if (session.isNew()) {
            // 방문자 수 증가
            visitorService.incrementVisitorCount();
        }
        VisitorDTO visitorDTO = visitorService.getVisitorCount();

        //vote_id가 1인것만 하나 조회
        Long voteId = 1L;
        Vote vote = voteService.getVoteById(voteId);
        Long voteCount = voteService.getResultCountByVoteId(voteId);

        //유저id
        Long kakaoId = (Long) session.getAttribute("kakaoId");
        User user = null;
        if(kakaoId != null) {
            user = userService.findById(kakaoId);
        }
        if (user == null) {  // user가 여전히 null이면 새로운 User 객체를 생성
            user = new User();
            user.setChangeNickname("비로그인유저");
        }


        model.addAttribute("user", user);
        model.addAttribute("vote", vote);
        model.addAttribute("voteCount", voteCount);
        model.addAttribute("dailyCount", visitorDTO.getDailyCount());
        model.addAttribute("totalCount", visitorDTO.getTotalCount());
        return "main";
    }
}
