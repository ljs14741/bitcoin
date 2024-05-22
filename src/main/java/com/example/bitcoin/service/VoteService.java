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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private VoteResultRepository voteResultRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분");

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
        List<Vote> votes = voteRepository.findAll();
        for (Vote vote : votes) {
            vote.setFormattedCreatedAt(vote.getCreatedAt().format(formatter));
        }
        return votes;
    }

    public Long getResultCountByVoteId(Long voteId) {
        return voteResultRepository.countByVoteId(voteId);
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

    public Map<Long, Long> getResultCountByVoteIdGrouped(Long voteId) {
        return voteResultRepository.findByVoteId(voteId).stream()
                .collect(Collectors.groupingBy(result -> result.getOption().getId(), Collectors.counting()));
    }
}