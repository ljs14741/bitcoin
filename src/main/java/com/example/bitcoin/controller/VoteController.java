package com.example.bitcoin.controller;

import com.example.bitcoin.entity.Options;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.service.VoteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class VoteController {
    @Autowired
    private VoteService voteService;

    @GetMapping("/voteList")
    public String listVotes(Model model) {
        List<Vote> votes = voteService.getAllVotes();
        Map<Long, Long> voteResults = new HashMap<>();

        for (Vote vote : votes) {
            Long voteId = vote.getId();
            Long resultCount = voteService.getResultCountByVoteId(voteId);
            voteResults.put(voteId, resultCount);
        }

        model.addAttribute("votes", votes);
        model.addAttribute("voteResults", voteResults);
        return "voteList";
    }

    @GetMapping("/vote/new")
    public String newVoteForm(Model model) {
        model.addAttribute("vote", new Vote());
        return "newVote";
    }

    @PostMapping("/vote")
    public String createVote(@ModelAttribute Vote vote, @RequestParam List<String> options) {
        voteService.createVote(vote, options);
        return "redirect:/voteList";
    }

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

    @PostMapping("/vote/{id}")
    public String submitVote(@PathVariable Long id, @RequestParam Long optionId, HttpServletRequest request, HttpServletResponse response, Model model) {
        String sessionId = getSessionIdFromCookie(request);

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            setSessionIdCookie(response, sessionId);
        }

        try {
            voteService.vote(id, optionId, sessionId);
        } catch (IllegalArgumentException e) {
            return "redirect:/vote/" + id + "?error=" + e.getMessage();
        }

        return "redirect:/voteList";
    }

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