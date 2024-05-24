package com.example.bitcoin.service;

import com.example.bitcoin.entity.User;
import com.example.bitcoin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return userRepository.findByChangeNickname(nickname) != null;
    }

    public void updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setChangeNickname(newNickname);
        userRepository.save(user);
    }
}