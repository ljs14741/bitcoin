package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findTop5ByGameNameOrderByScoreDescCreatedDateAsc(String gameName);
}
