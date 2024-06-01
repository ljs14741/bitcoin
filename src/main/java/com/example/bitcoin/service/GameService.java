package com.example.bitcoin.service;

import com.example.bitcoin.dto.GameDTO;
import com.example.bitcoin.entity.Game;
import com.example.bitcoin.repository.GameRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    public Game saveGame(GameDTO gameDTO, HttpSession session) {
        String nickname = (String) session.getAttribute("nickname");
        if(nickname == null) {
            Game game = new Game();
            game.setGameName(gameDTO.getGameName());
            game.setKakaoId(gameDTO.getKakaoId());
            game.setChangeNickname("비로그인 유저");
            game.setScore(gameDTO.getScore());
            return gameRepository.save(game);
        } else {
            Game game = new Game();
            game.setGameName(gameDTO.getGameName());
            game.setKakaoId(gameDTO.getKakaoId());
            game.setChangeNickname(nickname);
            game.setScore(gameDTO.getScore());
            return gameRepository.save(game);
        }

    }

    public List<Game> getTopScores() {
        return gameRepository.findTop5ByOrderByScoreDescCreatedDateAsc();
    }
}