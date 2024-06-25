package com.example.bitcoin.controller;

import com.example.bitcoin.dto.MeetDTO;
import com.example.bitcoin.entity.Meet;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.repository.MeetRepository;
import com.example.bitcoin.service.MeetService;
import com.example.bitcoin.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/meet")
@Slf4j
public class MeetController {

    @Autowired
    private MeetService meetService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private MeetRepository meetRepository;

    @GetMapping("/new")
    public String createMeetForm(Model model) {
        model.addAttribute("meet", new MeetDTO());
        return "createMeet";
    }

    @PostMapping("/new")
    public String createMeet(@ModelAttribute MeetDTO meetDTO, Model model) {
        MeetDTO savedMeet = meetService.createMeet(meetDTO);
        model.addAttribute("meet", savedMeet);
        return "redirect:/meet/privateVoteList";
    }

    @GetMapping("/list")
    public String getAllMeets(Model model) {
        List<MeetDTO> meets = meetService.getAllMeets();
        model.addAttribute("meets", meets);
        return "meetList";
    }

    @GetMapping("/privateVoteList")
    public String getAllPrivateMeets(Model model) {
        List<MeetDTO> meets = meetService.getAllMeets();
        model.addAttribute("meets", meets);
        return "privateVoteList";
    }

    // 모임 참여
    @GetMapping("/{id}")
    public String getMeetById(@PathVariable Long id, Model model) {
        meetService.getMeetById(id).ifPresent(meet -> model.addAttribute("meet", meet));
        Optional<MeetDTO> optionalMeet = meetService.getMeetById(id);

        MeetDTO meet = optionalMeet.get();
        model.addAttribute("meet", meet);

        List<Vote> votes = voteService.getAllPrivateVotes(id);
        Map<Long, Long> voteResults = new HashMap<>();

        for (Vote vote : votes) {
            Long voteId = vote.getId();
            Long uniqueUserCount = voteService.getUniqueUserCountByVoteId(voteId);
            voteResults.put(voteId, uniqueUserCount);
        }

        model.addAttribute("votes", votes);
        model.addAttribute("voteResults", voteResults);

        return "privateVoteRoom";
    }

    @PostMapping("/join")
    public String joinMeet(@RequestParam Long meetId, @RequestParam String password, Model model) {
        Optional<MeetDTO> optionalMeet = meetService.getMeetById(meetId);
        if (optionalMeet.isPresent()) {
            MeetDTO meet = optionalMeet.get();
            if (meet.getMeetPassword().equals(password)) {
                model.addAttribute("meet", meet);
                List<Vote> votes = voteService.getAllPrivateVotes(meetId);
                Map<Long, Long> voteResults = new HashMap<>();

                for (Vote vote : votes) {
                    Long voteId = vote.getId();
                    Long uniqueUserCount = voteService.getUniqueUserCountByVoteId(voteId);
                    voteResults.put(voteId, uniqueUserCount);
                }

                model.addAttribute("votes", votes);
                model.addAttribute("voteResults", voteResults);
                return "privateVoteRoom";
            } else {
                model.addAttribute("error", "비밀번호가 틀렸습니다.");
                return "redirect:/meet/privateVoteList"; // 비밀번호가 틀렸을 경우 경고창을 표시하고 리디렉트
            }
        }
        return "redirect:/meet/privateVoteList";
    }


    // 모임 수정
    @GetMapping("/edit/{id}")
    public String editMeetForm(@PathVariable Long id, Model model) {
        meetService.getMeetById(id).ifPresent(meet -> model.addAttribute("meet", meet));
//        Optional<MeetDTO> optionalMeet = meetService.getMeetById(id);
//
//        MeetDTO meet = optionalMeet.get();
//        model.addAttribute("meet", meet);

        return "editMeet";
    }

    // 모임 수정
    @PostMapping("/edit/{id}")
    public String editMeet(@PathVariable Long id, @ModelAttribute MeetDTO meetDTO) {
        meetService.updateMeet(id, meetDTO);
        return "redirect:/meet/privateVoteList";
    }

    // 모임 삭제
    @PostMapping("/delete/{id}")
    public String deleteMeet(@PathVariable Long id) {
        meetService.deleteMeet(id);
        return "redirect:/meet/privateVoteList";
    }

    @PostMapping("/checkPassword")
    @ResponseBody
    public Map<String, Object> checkPassword(@RequestBody Map<String, String> params) {
        Long meetId = Long.parseLong(params.get("meetId"));
        String password = params.get("password");

        log.debug("Received checkPassword request for meetId: {}, password: {}", meetId, password);

        Optional<MeetDTO> optionalMeet = meetService.getMeetById(meetId);
        Map<String, Object> response = new HashMap<>();

        if (optionalMeet.isPresent()) {
            MeetDTO meet = optionalMeet.get();
            if (meet.getMeetPassword().equals(password)) {
                response.put("success", true);
            } else {
                response.put("success", false);
            }
        } else {
            response.put("success", false);
        }

        return response;
    }

    @Scheduled(fixedRate = 60000) // 매 분마다 실행
    public void checkAndDeleteExpiredMeetings() {
        meetService.deleteExpiredMeets();
    }
}