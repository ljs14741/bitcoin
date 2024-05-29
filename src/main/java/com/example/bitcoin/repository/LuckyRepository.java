package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Lucky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LuckyRepository extends JpaRepository<Lucky, Long> {
    Optional<Lucky> findTopByKakaoIdOrderByCreatedDateDesc(Long kakaoId);
}