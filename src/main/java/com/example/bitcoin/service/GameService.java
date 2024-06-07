package com.example.bitcoin.service;

import com.example.bitcoin.dto.GameDTO;
import com.example.bitcoin.entity.Game;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.GameRepository;
import com.example.bitcoin.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    public Game saveGame(GameDTO gameDTO, HttpSession session) {
        String nickname = (String) session.getAttribute("nickname");
        User user = null;

        if(nickname != null) {
            user = userRepository.findByChangeNickname(nickname);
        }

        if (user == null) {
            user = new User();
            user.setChangeNickname("비로그인 유저");
        }

        Game game = new Game();
        game.setGameName(gameDTO.getGameName());
        game.setKakaoId(user.getKakaoId());
        game.setChangeNickname(user.getChangeNickname());
        game.setScore(gameDTO.getScore());

        return gameRepository.save(game);

    }

    public List<Game> getTopScoresByGameName(String gameName) {
        return gameRepository.findTop5ByGameNameOrderByScoreDescCreatedDateAsc(gameName);
    }

    public List<Game> getTopScoresForDefenseGame() {
        return getTopScoresByGameName("메이플 랜덤 타워 디펜스");
    }

    public List<Game> getTopScoresForAvoidingGame() {
        return getTopScoresByGameName("총알 피하기");
    }
}