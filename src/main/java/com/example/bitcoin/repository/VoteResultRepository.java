package com.example.bitcoin.repository;

import com.example.bitcoin.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {Optional<VoteResult> findByOptionNumberAndVoteId(Long optionNumber, Long voteId);

    Long countByOptionNumberAndVoteId(Long optionNumber, Long voteId);

    List<VoteResult> findByVoteIdAndUserId(Long voteId, String userId);

    Long countByVoteId(Long voteId);

    List<VoteResult> findByVoteId(Long voteId);

    Long countByVoteIdAndOptionNumber(Long voteId, Long optionNumber);

    void deleteByOptionNumber(Long optionNumber);

    @Modifying
    @Query("DELETE FROM vote_result vr WHERE vr.vote.id = :voteId")
    void deleteByVoteId(@Param("voteId") Long voteId);
}