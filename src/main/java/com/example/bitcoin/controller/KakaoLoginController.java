package com.example.bitcoin.controller;

//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
import com.example.bitcoin.entity.User;
import com.example.bitcoin.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class KakaoLoginController {

    private final String clientId = System.getenv("KAKAO_CLIENT_ID");
    private final String clientSecret = System.getenv("KAKAO_CLIENT_SECRET");
    private final String redirectUri = System.getenv("KAKAO_REDIRECT_URI");
//    private final String redirectUri = "https://binary96.store/login/oauth2/code/kakao";

    @Autowired
    UserService userService;

    @GetMapping("/oauth/kakao")
    public RedirectView kakaoLogin(@RequestParam(value = "state", required = false) String state) {
        String kakaoUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;
        if (state != null) {
            kakaoUrl += "&state=" + state;
        }
        return new RedirectView(kakaoUrl);
    }

    @RequestMapping("/login/oauth2/code/kakao")
    public String kakaoCallback(@RequestParam String code, @RequestParam(value = "state", required = false) String state, HttpSession session) {
        log.info("Received code: " + code);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        log.info("Request Params: " + params);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");

            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);

            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );

            if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                Long kakaoId = ((Number) userInfo.get("id")).longValue();
                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                Map<String, String> profile = (Map<String, String>) kakaoAccount.get("profile");
                String nickname = profile.get("nickname");

                // 사용자를 찾거나 새로 생성 후 저장
                User user = userService.findOrCreateUser(kakaoId, nickname);

                // 세션 또는 쿠키에 사용자 정보 저장 후 리디렉션
                session.setAttribute("kakaoId", user.getKakaoId());
                session.setAttribute("nickname", user.getChangeNickname());

                if (state != null) {
                    return "redirect:" + state;
                } else {
                    return "redirect:/";
                }
            } else {
                log.error("Failed to fetch user info from Kakao API: " + userInfoResponse.getStatusCode());
            }
        } else {
            log.error("Failed to fetch access token from Kakao API: " + response.getStatusCode());
        }

        return "redirect:/main?error=true";
    }
}