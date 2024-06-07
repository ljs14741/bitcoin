package com.example.bitcoin.controller;

import com.example.bitcoin.entity.Lucky;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.UserRepository;
import com.example.bitcoin.service.LuckyService;
import com.example.bitcoin.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Controller
@Slf4j
public class LuckyController {

    @Autowired
    private LuckyService luckyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/lucky")
    public String lucky(Model model, HttpSession session) {
        //유저id
        Long kakaoId = (Long) session.getAttribute("kakaoId");
        User user = null;
        if(kakaoId != null) {
            user = userService.findById(kakaoId);
        }
        if (user == null) {
            user = new User();
        }

        model.addAttribute("user", user);
        return "lucky";
    }

    // 사용자 정보 저장 및 운세 저장
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute User user, HttpSession session) {
        Long kakaoId = (Long) session.getAttribute("kakaoId");

        if (kakaoId != null) {
            Optional<User> existingUserOptional = Optional.ofNullable(userRepository.findByKakaoId(kakaoId));

            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                existingUser.setGender(user.getGender());
                existingUser.setSolarLunar(user.getSolarLunar());
                existingUser.setBirthTime(user.getBirthTime());
                existingUser.setBirthDate(user.getBirthDate());

                userRepository.save(existingUser);
                luckyService.getLucky(existingUser);
            }

        }

        return "redirect:/lucky";
    }

    // 오늘의 운세 조회
    @GetMapping("/getLucky")
    public String getLucky(HttpSession session, Model model) {
        Long kakaoId = (Long) session.getAttribute("kakaoId");

        if (kakaoId != null) {
            Optional<User> userOptional = Optional.ofNullable(userRepository.findByKakaoId(kakaoId));

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);

                Optional<Lucky> latestLuckyOptional = luckyService.getLatestLucky(kakaoId);

                if (latestLuckyOptional.isPresent()) {
                    Lucky luckyResult = latestLuckyOptional.get();
                    model.addAttribute("luckyResult",luckyResult);

                    LocalDate today = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                    String formattedDate = today.format(formatter);

                    model.addAttribute("luckytime", formattedDate);

                } else {
                    model.addAttribute("luckyResult", "운세 정보를 찾을 수 없습니다.");
                }
            } else {
                model.addAttribute("luckyResult", "유저 정보를 찾을 수 없습니다.");
            }
        } else {
            model.addAttribute("luckyResult", "로그인 정보가 없습니다.");
        }

        return "lucky";
    }

    // 매일 오전 0시 1분 사용자 운세 저장
    @Scheduled(cron = "0 1 0 * * ?")
    @ResponseBody
    public void scheduledLucky() {
        luckyService.scheduledLucky();
    }
}
