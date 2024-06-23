package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT v FROM vote v ORDER BY v.createdDate DESC")
    List<Vote> findAllOrderByCreatedDateDesc();

    // 공개 투표만 조회
    @Query("SELECT v FROM vote v WHERE v.voteType = 'PUBLIC' ORDER BY v.createdDate DESC")
    List<Vote> findAllPublicVotesOrderByCreatedDateDesc();

    @Query("SELECT v FROM vote v WHERE v.voteType = 'Private' ORDER BY v.createdDate DESC")
    List<Vote> findAllPrivateVotesOrderByCreatedDateDesc();

    // 특정 모임 ID와 비공개 투표를 필터링하여 조회
    @Query("SELECT v FROM vote v WHERE v.voteType = 'PRIVATE' AND v.meet.id = :meetId ORDER BY v.createdDate DESC")
    List<Vote> findPrivateVotesByMeetId(@Param("meetId") Long meetId);

    List<Vote> findByMeetId(Long meetId);
}
