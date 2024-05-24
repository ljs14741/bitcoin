package com.example.bitcoin.service;

import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findOrCreateUser(Long kakaoId, String nickname) {
        User user = userRepository.findByKakaoId(kakaoId);
        if (user == null) {
            user = new User(kakaoId, nickname);
            user.setChangeNickname(nickname);
            userRepository.save(user);
        }
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean nicknameExists(String nickname) {
        String cleanedNickname = nickname.replaceAll("\\s", "");
        List<String> cleanedExistingNicknames = userRepository.findAllNicknames()
                .stream()
                .map(existingNickname -> existingNickname.replaceAll("\\s", ""))
                .collect(Collectors.toList());

        return cleanedExistingNicknames.contains(cleanedNickname);
    }

    public void updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setChangeNickname(newNickname);
        userRepository.save(user);
    }
}