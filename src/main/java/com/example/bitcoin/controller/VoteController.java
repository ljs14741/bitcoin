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
import org.springframework.transaction.annotation.Transactional;
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
        List<Vote> votes = voteService.getAllPublicVotes();
        Map<Long, Long> voteResults = new HashMap<>();

        for (Vote vote : votes) {
            Long voteId = vote.getId();
            Long uniqueUserCount = voteService.getUniqueUserCountByVoteId(voteId);
            log.info("아아: " + uniqueUserCount);
            voteResults.put(voteId, uniqueUserCount);
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
        VoteDTO vote = new VoteDTO();
        vote.setMeetId(meetId);

        model.addAttribute("vote", vote);
        model.addAttribute("voteType", voteType);
        return "newVote";
    }


    // 투표 수정 화면 조회
    @GetMapping("/vote/edit/{id}")
    public String editVoteForm(@PathVariable Long id, Model model) {
        VoteDTO voteDTO = voteService.getVoteDTOById(id);
        log.info("테스트: " + voteDTO.getMaxOptions());
        log.info("가가가: " + voteDTO.getMeetId());

        List<Options> options = voteService.getOptionsByVoteId(id);


        model.addAttribute("vote", voteDTO);
        model.addAttribute("options", options);
        return "editVote";
    }

    // 투표 생성하기
    @PostMapping("/vote")
    public String createVote(@ModelAttribute VoteDTO voteDTO, @RequestParam List<String> options, @RequestParam(required = false) String voteType, Model model, Long meetId) {
        log.info("가나다라: " + voteDTO.getAllowMultipleVotes());

        if (voteDTO.getAllowMultipleVotes() == null) {
            voteDTO.setAllowMultipleVotes(false);
        }

        if ("PRIVATE".equals(voteType)) {
            voteDTO.setVoteType(Vote.VoteType.PRIVATE);
            voteService.createVote(voteDTO, options);
            voteDTO.setMeetId(meetId);
//            return meetController.getMeetById(voteDTO.getMeetId(), model);
            return "redirect:/meet/" + meetId;
        } else {
            voteDTO.setVoteType(Vote.VoteType.PUBLIC);
            voteService.createVote(voteDTO, options);
            return "redirect:/voteList";
        }
    }

    // 투표 수정
    @PostMapping("/vote/update/{id}")
    @Transactional
    public String updateVote(@PathVariable Long id, @ModelAttribute VoteDTO voteDTO, @RequestParam List<String> options, HttpSession session, @RequestParam(required = false) Long meetId) {
        log.info("나나나: " + voteDTO.getMeetId());

        voteDTO.setMeetId(meetId);
        Vote vote = voteService.convertToEntity(voteDTO);
        vote.setId(id);
        log.info("나나다: " + vote.getVoteType());
        voteService.updateVote(vote, options);

        if (vote.getVoteType() == Vote.VoteType.PUBLIC) {
            return "redirect:/voteList";
        } else {
            return "redirect:/meet/" + meetId;
        }
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

    // 투표 삭제
    @PostMapping("/vote/delete/{id}")
    public String deleteVote(@PathVariable Long id) {
        Vote vote = voteService.getVoteById(id);
        Long meetId = vote.getMeet() != null ? vote.getMeet().getId() : null;

        voteService.deleteVote(id);

        if (vote.getVoteType() == Vote.VoteType.PUBLIC) {
            return "redirect:/voteList";
        } else {
            return "redirect:/meet/" + meetId;
        }
    }

    // 투표하기
    @PostMapping("/vote/{id}")
    public String submitVote(@PathVariable Long id, @RequestParam List<Long> optionNumbers, HttpServletRequest request, HttpServletResponse response, Model model) {
        String sessionId = getSessionIdFromCookie(request);

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            setSessionIdCookie(response, sessionId);
            log.debug("Generated new session ID: " + sessionId);
        } else {
            log.debug("Using existing session ID: " + sessionId);
        }

        Vote vote = voteService.getVoteById(id);
        Long meetId = vote.getMeet() != null ? vote.getMeet().getId() : null;

        try {
            if (vote.getAllowMultipleVotes()) {
                log.debug("Multiple votes allowed. Voting with session ID: " + sessionId);
                voteService.voteMultiple(id, optionNumbers, sessionId);
            } else {
                if (optionNumbers.size() > 1) {
                    throw new IllegalArgumentException("중복 투표가 허용되지 않습니다.");
                }
                log.debug("Single vote allowed. Voting with session ID: " + sessionId);
                voteService.vote(id, optionNumbers.get(0), sessionId);
            }
        } catch (IllegalArgumentException e) {
            log.error("Voting error: " + e.getMessage());
            return "redirect:/vote/" + id + "?error=" + e.getMessage();
        }

        if (vote.getVoteType() == Vote.VoteType.PUBLIC) {
            return "redirect:/voteList";
        } else {
            return "redirect:/meet/" + meetId;
        }

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
                    log.debug("Found session ID in cookie: " + cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        log.debug("No session ID found in cookies.");
        return null;
    }

    private void setSessionIdCookie(HttpServletResponse response, String sessionId) {
        Cookie cookie = new Cookie("VOTE_SESSION_ID", sessionId);

        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1년 동안 유효
        response.addCookie(cookie);
        log.debug("Set session ID cookie: " + sessionId);
    }

    @PostMapping("/vote/checkPassword")
    @ResponseBody
    public Map<String, Object> checkVotePassword(@RequestBody Map<String, String> params) {
        Long voteId = Long.parseLong(params.get("voteId"));
        String password = params.get("password");

        log.debug("Received checkPassword request for voteId: {}, password: {}", voteId, password);

        boolean isPasswordCorrect = voteService.checkVotePassword(voteId, password);
        Map<String, Object> response = new HashMap<>();
        response.put("success", isPasswordCorrect);

        return response;
    }
}