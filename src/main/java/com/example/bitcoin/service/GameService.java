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
                        Map.of("text", prompt)
//                        Map.of("text", boardState.toString())
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
        String playerColor = playerFirst ? "black" : "white";
        String aiColor = playerFirst ? "white" : "black";

        String boardStateString = boardState.toString();

        if (firstMove) {
            if (playerFirst) {
                return String.format("안녕? 난 너와 오목을 할거야. 나의 상대가 되어줘. 이해를 돕기 위해 간단한 룰을 알려줄게."
                                + "\n1) 오목판은 가로, 세로 15x15로 진행할 거야. 좌표는 (0,0)부터 (14,14)까지 있어. (row,col)이라고 생각해. 그리고 제일 왼쪽 상단이 (0,0)이지. 여기서 오른쪽으로 한칸 이동하면 (0,1), 아래로 한칸이동하면 (1,0)이야."
                                + "\n2) 자신의 돌을 가로, 세로, 대각선 중 한 방향으로 다섯 개 연속으로 놓으면 승리합니다. 상대방의 돌이 5개 되는 것을 막으면서 너의 돌이 5개가 연속으로 되게 만들어서 나를 이기고 승리해 봐."
                                + "\n3) 나와 상대방 중 돌을 이미 놓은 자리에 중복해서 놓을 수 없다."
                                + "\n4) 금수(쌍삼, 쌍사) 규칙을 적용하여 금수자리에는 돌을 놓을 수 없다."
                                + "\n5) 상대가 3줄을 완성했을 때는 반드시 4줄이 되지 않도록 막아야 합니다. 예를 들어, 상대가 ○ ○ ○ 이렇게 3줄을 만들었다면, 다음 수에서는 반드시 ○ ○ ○ ● 또는 ● ○ ○ ○ 이렇게 한쪽을 막아 4줄이 되지 않도록 해 주세요."
                                + "\n이 전략을 이해하고 게임을 진행해 주세요."
                                + "\n내가 먼저 선공을 할게. 나는 black, 너는 white를 해줘. 내가 놓을 자리는 row: %d, col: %d. 너는 white 돌을 두고 싶은 자리에 JSON 형식으로 반환해줘. 예시: {\"row\": 8, \"col\": 7}. 대답은 내가 보여준 예시로만 대답해. 다른 말은 필요없어."
                                + "\n현재의 보드판 상태야: %s. black과 white의 위치를 먼저 파악해. 그 후 없는 위치 중 가장 좋은 위치를 골라.",
                        playerMove.get("row"), playerMove.get("col"), boardStateString);
            } else {
                return String.format("안녕? 난 너와 오목을 할거야. 나의 상대가 되어줘. 이해를 돕기 위해 간단한 룰을 알려줄게."
                                + "\n1) 오목판은 가로, 세로 15x15로 진행할 거야. 좌표는 (0,0)부터 (14,14)까지 있어. (row,col)이라고 생각해. 그리고 제일 왼쪽 상단이 (0,0)이지. 여기서 오른쪽으로 한칸 이동하면 (0,1), 아래로 한칸이동하면 (1,0)이야."
                                + "\n2) 자신의 돌을 가로, 세로, 대각선 중 한 방향으로 다섯 개 연속으로 놓으면 승리합니다. 상대방의 돌이 5개 되는 것을 막으면서 너의 돌이 5개가 연속으로 되게 만들어서 나를 이기고 승리해 봐."
                                + "\n3) 나와 상대방 중 돌을 이미 놓은 자리에 중복해서 놓을 수 없다."
                                + "\n4) 금수(쌍삼, 쌍사) 규칙을 적용하여 금수자리에는 돌을 놓을 수 없다."
                                + "\n5) 상대가 3줄을 완성했을 때는 반드시 4줄이 되지 않도록 막아야 합니다. 예를 들어, 상대가 ○ ○ ○ 이렇게 3줄을 만들었다면, 다음 수에서는 반드시 ○ ○ ○ ● 또는 ● ○ ○ ○ 이렇게 한쪽을 막아 4줄이 되지 않도록 해 주세요."
                                + "\n이 전략을 이해하고 게임을 진행해 주세요."
                                + "\n너가 먼저 선공을 해. 나는 white, 너는 black을 해줘. 너는 black 돌을 두고 싶은 자리에 JSON 형식으로 반환해줘. 예시: {\"row\": 8, \"col\": 7}. 대답은 내가 보여준 예시로만 대답해. 다른 말은 필요없어."
                                + "\n현재의 보드판 상태야: %s.",
                        boardStateString);
            }
        } else {
            return String.format("안녕? 난 너와 오목을 할거야. 나의 상대가 되어줘. 이해를 돕기 위해 간단한 룰을 알려줄게."
                            + "\n1) 오목판은 가로, 세로 15x15로 진행할 거야. 좌표는 (0,0)부터 (14,14)까지 있어. (row,col)이라고 생각해. 그리고 제일 왼쪽 상단이 (0,0)이지. 여기서 오른쪽으로 한칸 이동하면 (0,1), 아래로 한칸이동하면 (1,0)이야."
                            + "\n2) 자신의 돌을 가로, 세로, 대각선 중 한 방향으로 다섯 개 연속으로 놓으면 승리합니다. 상대방의 돌이 5개 되는 것을 막으면서 너의 돌이 5개가 연속으로 되게 만들어서 나를 이기고 승리해 봐."
                            + "\n3) 나와 상대방 중 돌을 이미 놓은 자리에 중복해서 놓을 수 없다."
                            + "\n4) 금수(쌍삼, 쌍사) 규칙을 적용하여 금수자리에는 돌을 놓을 수 없다."
                            + "\n5) 상대가 3줄을 완성했을 때는 반드시 4줄이 되지 않도록 막아야 합니다. 예를 들어, 상대가 ○ ○ ○ 이렇게 3줄을 만들었다면, 다음 수에서는 반드시 ○ ○ ○ ● 또는 ● ○ ○ ○ 이렇게 한쪽을 막아 4줄이 되지 않도록 해 주세요."
                            + "\n이 전략을 이해하고 게임을 진행해 주세요."
                            + "\n나는 " + playerColor + " 돌이야. 너는 " + aiColor + " 돌이야."
                            + "\n이번 차례에 내가 놓을 자리는 row: %d, col: %d."
                            + "\n현재의 보드판 상태야: %s. black과 white의 위치를 먼저 파악해. 그 후 없는 위치 중 가장 좋은 위치를 골라."
                            + "\n이번엔 너 차례야. 잘 생각하고 너가 돌을 두고 싶은 자리에 JSON 형식으로 반환해줘. 예시: {\"row\": 8, \"col\": 7}. 대답은 내가 보여준 예시로만 대답해. 다른 말은 필요없어.",
                    playerMove.get("row"), playerMove.get("col"), boardStateString);
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