package com.example.bitcoin.repository;

import com.example.bitcoin.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {
    Optional<VoteResult> findByOptionId(Long optionId);

    Long countByOptionId(Long optionId);

    Optional<VoteResult> findByVoteIdAndUserId(Long voteId, String userId);
}
