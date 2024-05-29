package com.example.bitcoin.controller;

import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.UserRepository;
import com.example.bitcoin.service.LuckyService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Controller
    @Slf4j
    public class LuckyController {
        @Autowired
        private LuckyService luckyService;

        @Autowired
        private UserRepository userRepository;

        @GetMapping("/lucky")
        public String Lucky(Model model) {
            model.addAttribute("user", new User());
            return "/lucky";
        }

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
                }
            }

            return "redirect:/lucky";
        }

    @GetMapping("/getLucky")
    public String getLucky(HttpSession session, Model model) {
        Long kakaoId = (Long) session.getAttribute("kakaoId");

        if (kakaoId != null) {
            Optional<User> userOptional = Optional.ofNullable(userRepository.findByKakaoId(kakaoId));

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String fortuneResult = luckyService.getLucky(user);
                model.addAttribute("user", user);  // 사용자 정보를 추가
                model.addAttribute("fortuneResult", fortuneResult);
            } else {
                model.addAttribute("fortuneResult", "유저 정보를 찾을 수 없습니다.");
            }
        } else {
            model.addAttribute("fortuneResult", "로그인 정보가 없습니다.");
        }

        return "/lucky";
    }

    }
