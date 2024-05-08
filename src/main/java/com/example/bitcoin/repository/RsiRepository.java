package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Rsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RsiRepository extends JpaRepository<Rsi, Long> {
    List<Rsi> findAllByOrderByRsi60AscRsiDailyAscRsiWeeklyAscRsiMonthlyAsc();
}