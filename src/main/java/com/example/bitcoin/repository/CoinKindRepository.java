package com.example.bitcoin.repository;

import com.example.bitcoin.entity.CoinKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinKindRepository extends JpaRepository<CoinKind, Long> {
    @Query("SELECT c FROM coinkind c WHERE c.market LIKE %:market%")
    List<CoinKind> findByMarketLike(String market);
}