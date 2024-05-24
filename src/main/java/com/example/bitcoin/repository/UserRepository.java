package com.example.bitcoin.repository;

import com.example.bitcoin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByKakaoId(Long kakaoId);
    User findByChangeNickname(String changeNickname);
}
