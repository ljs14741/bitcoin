package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Options;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<Options, Long> {
    List<Options> findByVoteId(Long voteId);

    @Query("SELECT o FROM options o LEFT JOIN FETCH o.voteResults WHERE o.vote.id = :voteId")
    List<Options> findByVoteIdWithResults(@Param("voteId") Long voteId);

    @Query("SELECT o FROM options o WHERE o.vote.id = :voteId AND o.optionNumber = :optionNumber")
    Optional<Options> findByVoteIdAndOptionNumber(@Param("voteId") Long voteId, @Param("optionNumber") Long optionNumber);
}