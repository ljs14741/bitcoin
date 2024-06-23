package com.example.bitcoin.controller;

import com.example.bitcoin.dto.MeetDTO;
import com.example.bitcoin.dto.VoteDTO;
import com.example.bitcoin.entity.Meet;
import com.example.bitcoin.entity.Options;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.service.MeetService;
import com.example.bitcoin.service.VoteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class VoteController {
    @Autowired
    private VoteService voteService;

    @Autowired
    private MeetService meetService;

    @Autowired
    private MeetController meetController;

    // 공개 투표 목록 조회
    @GetMapping("/voteList")
    public String publicListVotes(Model model, HttpSession session) {
//        List<Vote> votes = voteService.getAllVotes();
        List<Vote> votes = voteService.getAllPublicVotes();
        Map<Long, Long> voteResults = new HashMap<>();

        for (Vote vote : votes) {
            Long voteId = vote.getId();
            Long resultCount = voteService.getResultCountByVoteId(voteId);
            voteResults.put(voteId, resultCount);
        }

        List<MeetDTO> meets = meetService.getAllMeets();
        model.addAttribute("meets", meets);

        model.addAttribute("votes", votes);
        model.addAttribute("voteResults", voteResults);

        session.setAttribute("dummy", "dummyValue");
        return "voteList";
    }

    // 투표 생성 화면 조회
    @GetMapping("/vote/new")
    public String newVoteForm(@RequestParam(required = false) String voteType, Model model,@RequestParam(required = false) Long meetId) {
//        MeetDTO meet = new MeetDTO();
//        meet.setId(meet.getId());
        log.info("그래그래: " + meetId);
//
//        model.addAttribute("meet", meet);
        VoteDTO vote = new VoteDTO();
        vote.setMeetId(meetId);

        model.addAttribute("vote", vote);
        model.addAttribute("voteType", voteType);
        return "newVote";
    }


    // 투표 수정 화면 조회
    @GetMapping("/vote/edit/{id}")
    public String editVoteForm(@PathVariable Long id, Model model, HttpSession session) {
//        Long kakaoId = (Long) session.getAttribute("kakaoId");
        Vote vote = voteService.getVoteById(id);
//        if (!vote.getKakaoId().equals(kakaoId)) {
//            return "redirect:/voteList";
//        }
        List<Options> options = voteService.getOptionsByVoteId(id);

        model.addAttribute("vote", vote);
        model.addAttribute("options", options);
        return "editVote";
    }

    // 투표 생성하기
    @PostMapping("/vote")
    public String createVote(@ModelAttribute VoteDTO voteDTO, @RequestParam List<String> options, @RequestParam(required = false) String voteType, HttpSession session, Model model) {
//        log.info("가나다: " + vote.getVoteType());
//        Long kakaoId = (Long) session.getAttribute("kakaoId");
//        vote.setKakaoId(kakaoId);
//        Vote vote = convertToDTO(vote);
        if ("PRIVATE".equals(voteType)) {
            voteDTO.setVoteType(Vote.VoteType.PRIVATE);
            voteService.createVote(voteDTO, options);
            return meetController.getMeetById(voteDTO.getMeetId(), model);
        } else {
            voteDTO.setVoteType(Vote.VoteType.PUBLIC);
            voteService.createVote(voteDTO, options);
            return "redirect:/voteList";
        }
//        voteService.createVote(voteDTO, options);

//        voteService.createVote(vote, options);
//        return "redirect:/voteList";
    }

    // 투표 수정하기
    @PostMapping("/vote/update/{id}")
    public String updateVote(@PathVariable Long id, @ModelAttribute Vote vote, @RequestParam List<String> options, HttpSession session) {
//        Long kakaoId = (Long) session.getAttribute("kakaoId");
//        vote.setId(id);
//        vote.setKakaoId(kakaoId);
        voteService.updateVote(vote, options);
        return "redirect:/voteList";
    }

    // 투표 화면 조회
    @GetMapping("/vote/{id}")
    public String vote(@PathVariable Long id, Model model, @RequestParam(required = false) String error) {
        Vote vote = voteService.getVoteById(id);
        List<Options> options = voteService.getOptionsByVoteId(id);
        model.addAttribute("vote", vote);
        model.addAttribute("options", options);
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "vote";
    }

    // 투표하기
    @PostMapping("/vote/{id}")
    public String submitVote(@PathVariable Long id, @RequestParam Long optionNumber, HttpServletRequest request, HttpServletResponse response, Model model) {
        String sessionId = getSessionIdFromCookie(request);

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            setSessionIdCookie(response, sessionId);
        }

        try {
            voteService.vote(id, optionNumber, sessionId);
        } catch (IllegalArgumentException e) {
            return "redirect:/vote/" + id + "?error=" + e.getMessage();
        }

        return "redirect:/voteList";
    }

    // 투표 결과 화면 조회
    @GetMapping("/vote/results/{id}")
    public String viewVoteResults(@PathVariable Long id, Model model) {
        List<Options> options = voteService.getOptionsByVoteId(id);
        Map<Long, Long> results = voteService.getResultCountByVoteIdGrouped(id);

        model.addAttribute("options", options);
        model.addAttribute("results", results);
        return "voteResults";
    }

    private String getSessionIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("VOTE_SESSION_ID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setSessionIdCookie(HttpServletResponse response, String sessionId) {
        Cookie cookie = new Cookie("VOTE_SESSION_ID", sessionId);

        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1년 동안 유효
        response.addCookie(cookie);
    }
}