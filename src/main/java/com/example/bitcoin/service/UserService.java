package com.example.bitcoin.service;

import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 유니코드 공백 문자 패턴 정의
    private static final Pattern UNICODE_WHITESPACE_PATTERN = Pattern.compile(
            "[\\p{Zs}\\u0009\\u000A\\u000B\\u000C\\u000D\\u0085\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\u3164]"
    );

    public User findOrCreateUser(Long kakaoId, String nickname) {
        User user = userRepository.findByKakaoId(kakaoId);
        if (user == null) {
            user = new User(kakaoId, nickname);
            user.setChangeNickname(nickname);
            userRepository.save(user);
        }
        return user;
    }

    public User findById(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId);
    }


    // 공백 문자 제거
    private String removeUnicodeWhitespaces(String input) {
        return UNICODE_WHITESPACE_PATTERN.matcher(input).replaceAll("");
    }

    public boolean nicknameExists(String nickname) {
        String cleanedNickname = removeUnicodeWhitespaces(nickname);
        log.info("안되자나: " + cleanedNickname);
        List<String> cleanedExistingNicknames = userRepository.findAllNicknames()
                .stream()
                .map(this::removeUnicodeWhitespaces)
                .collect(Collectors.toList());

        return cleanedExistingNicknames.contains(cleanedNickname);
    }

    public void updateNickname(Long kakaoId, String newNickname) {
        User user = userRepository.findByKakaoId(kakaoId);
        String cleanedNickname = removeUnicodeWhitespaces(newNickname);
        user.setChangeNickname(cleanedNickname);
        userRepository.save(user);
    }
}