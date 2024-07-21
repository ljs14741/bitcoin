package com.example.bitcoin.service;

import com.example.bitcoin.dto.GameDTO;
import com.example.bitcoin.entity.Game;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.GameRepository;
import com.example.bitcoin.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";



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
        return getTopScoresByGameName("김치 타워 디펜스");
    }

    public List<Game> getTopScoresForAvoidingGame() {
        return getTopScoresByGameName("총알 피하기");
    }

    // 오목 Gemini API 호출
    public Map<String, Object> getGeminiMove(List<List<String>> boardState, boolean firstMove, boolean playerFirst, Map<String, Integer> playerMove) {
        String geminiApiKey = System.getenv("GEMINI_API_KEY");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = createPrompt(firstMove, playerFirst, boardState, playerMove);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt),
                        Map.of("text", boardState.toString())
                ))
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Sending request to Gemini API with body: {}", requestBody);

        ResponseEntity<Map> response = restTemplate.exchange(
                GEMINI_API_URL + geminiApiKey,
                HttpMethod.POST,
                entity,
                Map.class
        );

        log.info("Received response from Gemini API: {}", response.getBody());

        Map<String, Object> responseBody = response.getBody();
        String text = extractTextFromResponse(responseBody);

        // JSON 형식의 문자열을 파싱하여 row와 col 값을 추출
        Map<String, Object> move = parseMoveFromJson(text);

        log.info("눈물나게 아픈날에: " + move);

        return move;
    }

    private String createPrompt(boolean firstMove, boolean playerFirst, List<List<String>> boardState, Map<String, Integer> playerMove) {
        if (firstMove) {
            if (playerFirst) {
                return String.format("안녕? 난 너와 오목을 할거야. 나의 상대가 되어줘. 오목판은 가로, 세로 15*15로 진행할거야. 좌표는 (0,0)부터 (14,14)까지 있어. 내가 먼저 선공을 할게. 나는 black, 너는 white를 해줘. 내가 놓을 자리는 row: %d, col: %d 너는 white 돌을 두고 싶은 자리에 JSON 형식으로 반환해줘. 예시: {\"row\": 8, \"col\": 7}", playerMove.get("row"), playerMove.get("col"));
            } else {
                return "안녕? 난 너와 오목을 할거야. 나의 상대가 되어줘. 오목판은 가로, 세로 15*15로 진행할거야. 좌표는 (0,0)부터 (14,14)까지 있어. 나는 white, 너는 black을 해줘. 너는 black 돌을 두고 싶은 자리에 JSON 형식으로 반환해줘. 예시: {\"row\": 8, \"col\": 7}";
            }
        } else {
            return String.format("내가 놓을 자리는 row: %d, col: %d 너는 white 돌을 두고 싶은 자리에 'white'를 표시해서 알려줘.", playerMove.get("row"), playerMove.get("col"));
        }
    }

    private String extractTextFromResponse(Map<String, Object> responseBody) {
        Map<String, Object> candidate = (Map<String, Object>) ((List<?>) responseBody.get("candidates")).get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        Map<String, Object> part = (Map<String, Object>) ((List<?>) content.get("parts")).get(0);
        return (String) part.get("text");
    }

    private Map<String, Object> parseMoveFromJson(String responseText) {
        // JSON 문자열을 파싱하여 row와 col 값을 추출
        try {
            // JSON 텍스트 내의 불필요한 백틱(```) 제거
            responseText = responseText.replace("```json", "").replace("```", "").trim();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseText, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse move from response text: " + responseText, e);
        }
    }
}