package com.example.bitcoin.repository;

import com.example.bitcoin.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {Optional<VoteResult> findByOptionNumberAndVoteId(Long optionNumber, Long voteId);

    Long countByOptionNumberAndVoteId(Long optionNumber, Long voteId);

    Optional<VoteResult> findByVoteIdAndUserId(Long voteId, String userId);

    Long countByVoteId(Long voteId);

    List<VoteResult> findByVoteId(Long voteId);

    Long countByVoteIdAndOptionNumber(Long voteId, Long optionNumber);
}