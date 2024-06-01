package com.example.bitcoin.controller;

import com.example.bitcoin.dto.GameDTO;
import com.example.bitcoin.entity.Game;
import com.example.bitcoin.service.GameService;
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

    @GetMapping("/game")
    public String game(Model model) {
        List<Game> games = gameService.getTopScores();
        model.addAttribute("games", games);
        return "game";
    }

    @PostMapping("/save")
    @ResponseBody
    public List<Game> saveGame(@RequestBody GameDTO gameDTO, HttpSession session) {
        gameService.saveGame(gameDTO, session);
        return gameService.getTopScores(); // 최신 순위를 반환
    }

}