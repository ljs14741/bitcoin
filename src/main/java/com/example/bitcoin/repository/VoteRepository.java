package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT v FROM vote v ORDER BY v.createdAt DESC")
    List<Vote> findAllOrderByCreatedAtDesc();
}
