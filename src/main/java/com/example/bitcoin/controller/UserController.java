package com.example.bitcoin.controller;

import com.example.bitcoin.entity.User;
import com.example.bitcoin.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("id");
        if (userId == null) {
            return "redirect:/";
        }

        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/updateNickname")
    public String updateNickname(@RequestParam String newNickname, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("id");
        if (userId == null) {
            return "redirect:/";
        }

        if (userService.nicknameExists(newNickname)) {
            model.addAttribute("error", "동일한 닉네임이 존재합니다. 다른 닉네임을 사용하세요. ^^");
            return showProfile(session, model);
        }

        userService.updateNickname(userId, newNickname);
        session.setAttribute("nickname", newNickname);
        return "redirect:/";
    }
}
