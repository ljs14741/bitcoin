package com.example.bitcoin.repository;

import com.example.bitcoin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByKakaoId(Long kakaoId);

    User findByChangeNickname(String changeNickname);

    @Query("SELECT u.changeNickname FROM user u")
    List<String> findAllNicknames();

    List<User> findAllByBirthDateIsNotNull();
}
