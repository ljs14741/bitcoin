package com.example.bitcoin.service;

import com.example.bitcoin.entity.Options;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.entity.VoteResult;
import com.example.bitcoin.repository.VoteResultRepository;
import com.example.bitcoin.repository.VoteRepository;
import com.example.bitcoin.repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private VoteResultRepository voteResultRepository;

    public Vote createVote(Vote vote, List<String> options) {
        LocalDateTime now = LocalDateTime.now();
        vote.setCreatedAt(now);
        Vote savedVote = voteRepository.save(vote);
        long optionId = 1;
        for (String optionText : options) {
            Options option = new Options();
            option.setId(optionId);
            option.setVote(savedVote);
            option.setOptionText(optionText);
            optionRepository.save(option);
            optionId++;
        }
        return savedVote;
    }

    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    public Vote getVoteById(Long id) {
        return voteRepository.findById(id).orElseThrow(() -> new RuntimeException("Vote not found"));
    }

    public void vote(Long voteId, Long optionId, String sessionId) {
        // 중복 투표 확인
        if (voteResultRepository.findByVoteIdAndUserId(voteId, sessionId).isPresent()) {
            throw new IllegalArgumentException("You have already voted.");
        }

        Vote vote = getVoteById(voteId);
        Options option = optionRepository.findByVoteIdAndId(voteId, optionId).orElseThrow(() -> new RuntimeException("Option not found"));

        VoteResult voteResult = new VoteResult();
        voteResult.setOption(option);
        voteResult.setVote(vote);
        voteResult.setUserId(sessionId); // 사용자 세션 ID 설정
        voteResult.setCount(1);

        voteResultRepository.save(voteResult);
    }

    public List<Options> getOptionsByVoteId(Long voteId) {
        return optionRepository.findByVoteIdWithResults(voteId);
    }

    public int countVotesByOptionId(Long optionId) {
        return voteResultRepository.countByOptionId(optionId).intValue();
    }
}