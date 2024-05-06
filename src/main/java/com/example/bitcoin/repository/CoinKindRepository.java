package com.example.bitcoin.repository;

import com.example.bitcoin.entity.CoinKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinKindRepository extends JpaRepository<CoinKind, Long> {

}