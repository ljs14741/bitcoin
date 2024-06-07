package com.example.bitcoin.controller;

import com.example.bitcoin.dto.GameDTO;
import com.example.bitcoin.entity.Game;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.service.GameService;
import com.example.bitcoin.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Slf4j
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    UserService userService;

    @GetMapping("/game")
    public String game(Model model, HttpSession session) {
        List<Game> games = gameService.getTopScoresForAvoidingGame();

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
        model.addAttribute("games", games);
        return "game";
    }

    @GetMapping("/defenseGame")
    public String defenseGame(Model model, HttpSession session) {
        List<Game> games = gameService.getTopScoresForDefenseGame();

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
        model.addAttribute("games", games);
        return "defenseGame";
    }

    @PostMapping("/save")
    @ResponseBody
    public List<Game> saveGame(@RequestBody GameDTO gameDTO, HttpSession session) {
        gameService.saveGame(gameDTO, session);
        return gameService.getTopScoresByGameName(gameDTO.getGameName()); // 저장한 게임의 최신 순위를 반환
    }

}