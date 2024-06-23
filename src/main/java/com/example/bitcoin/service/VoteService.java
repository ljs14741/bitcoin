package com.example.bitcoin.service;

import com.example.bitcoin.dto.VoteDTO;
import com.example.bitcoin.entity.Meet;
import com.example.bitcoin.entity.Options;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.entity.VoteResult;
import com.example.bitcoin.repository.MeetRepository;
import com.example.bitcoin.repository.VoteResultRepository;
import com.example.bitcoin.repository.VoteRepository;
import com.example.bitcoin.repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private MeetRepository meetRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분");

    public Vote createVote(VoteDTO voteDTO, List<String> options) {
        Meet meet = null;
        if (voteDTO.getMeetId() != null) {
            meet = meetRepository.findById(voteDTO.getMeetId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid meet ID: " + voteDTO.getMeetId()));
        }

        Vote vote = Vote.builder()
                .id(voteDTO.getId())
                .kakaoId(voteDTO.getKakaoId())
                .votePassword(voteDTO.getVotePassword())
                .meet(meet)
                .voteType(voteDTO.getVoteType())
                .endTime(voteDTO.getEndTime())
                .title(voteDTO.getTitle())
                .formattedCreatedDate(voteDTO.getFormattedCreatedDate())
                .updYn("N")
                .delYn("N")
                .createdDate(voteDTO.getCreatedDate())
                .updatedDate(voteDTO.getUpdatedDate())
                .build();

        Vote savedVote = voteRepository.save(vote);
        long optionNumber = 1;
        for (String optionText : options) {
            Options option = new Options();
            option.setOptionNumber(optionNumber); // 수동으로 번호 설정
            option.setVote(savedVote);
            option.setOptionText(optionText);
            optionRepository.save(option);
            optionNumber++;
        }
        return savedVote;
    }

    @Transactional
    public void updateVote(Vote vote, List<String> options) {
        Vote savedVote = voteRepository.save(vote);
        optionRepository.deleteByVoteId(savedVote.getId());
        long optionNumber = 1;
        for (String optionText : options) {
            Options option = new Options();
            option.setOptionNumber(optionNumber); // 수동으로 번호 설정
            option.setVote(savedVote);
            option.setOptionText(optionText);
            optionRepository.save(option);
            optionNumber++;
        }
    }

//    public List<Vote> getAllVotes() {
//        List<Vote> votes = voteRepository.findAllOrderByCreatedDateDesc();
//        for (Vote vote : votes) {
//            vote.setFormattedCreatedDate(vote.getCreatedDate().format(formatter));
//        }
//        return votes;
//    }

    // 공개 투표만 조회
    public List<Vote> getAllPublicVotes() {
        List<Vote> votes = voteRepository.findAllPublicVotesOrderByCreatedDateDesc();
        for (Vote vote : votes) {
            vote.setFormattedCreatedDate(vote.getCreatedDate().format(formatter));
        }
        return votes;
    }

    // 비공개 투표 조회 (선택한 모임)
    public List<Vote> getAllPrivateVotes(Long meetId) {
        List<Vote> votes = voteRepository.findPrivateVotesByMeetId(meetId);
        for (Vote vote : votes) {
            vote.setFormattedCreatedDate(vote.getCreatedDate().format(formatter));
        }
        return votes;
    }

    public Long getResultCountByVoteId(Long voteId) {
        return voteResultRepository.countByVoteId(voteId);
    }

    public Vote getVoteById(Long id) {
        return voteRepository.findById(id).orElseThrow(() -> new RuntimeException("Vote not found"));
    }

    public void vote(Long voteId, Long optionNumber, String sessionId) {
        // 중복 투표 확인
        if (voteResultRepository.findByVoteIdAndUserId(voteId, sessionId).isPresent()) {
            throw new IllegalArgumentException("You have already voted.");
        }

        Vote vote = getVoteById(voteId);
        Options option = optionRepository.findByVoteIdAndOptionNumber(voteId, optionNumber).orElseThrow(() -> new RuntimeException("Option not found"));

        VoteResult voteResult = new VoteResult();
        voteResult.setOptionNumber(optionNumber); // 수동으로 번호 설정
        voteResult.setVote(vote);
        voteResult.setUserId(sessionId); // 사용자 세션 ID 설정

        voteResultRepository.save(voteResult);
    }

    public List<Options> getOptionsByVoteId(Long voteId) {
        return optionRepository.findByVoteIdWithResults(voteId);
    }

    public int countVotesByOptionNumber(Long voteId, Long optionNumber) {
        return voteResultRepository.countByVoteIdAndOptionNumber(voteId, optionNumber).intValue();
    }

    public Map<Long, Long> getResultCountByVoteIdGrouped(Long voteId) {
        return voteResultRepository.findByVoteId(voteId).stream()
                .collect(Collectors.groupingBy(VoteResult::getOptionNumber, Collectors.counting()));
    }
}