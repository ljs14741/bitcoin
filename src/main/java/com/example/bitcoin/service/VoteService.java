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
import java.util.Set;
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
                .allowMultipleVotes(voteDTO.getAllowMultipleVotes())
                .maxOptions(voteDTO.getMaxOptions())
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
//        optionRepository.deleteByVoteId(savedVote.getId()); // 여기서 동시성 제어 문제가 발생. 기존 코드: 한 번의 SQL DELETE 문으로 일괄 삭제. 수정된 코드: 여러 번의 SQL DELETE 문으로 개별 삭제. // 삭제 작업을 더 세밀하게 제어 -> 동시성제어
        List<Options> existingOptions = optionRepository.findByVoteId(savedVote.getId());
        for (Options option : existingOptions) {
            optionRepository.delete(option);
        }
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

    // 투표 삭제
    @Transactional
    public void deleteVote(Long id) {
        voteResultRepository.deleteByVoteId(id);

        List<Options> options = optionRepository.findByVoteId(id);
        for (Options option : options) {
            // 옵션 삭제
            optionRepository.delete(option);
        }
        // 투표 삭제
        voteRepository.deleteById(id);
    }

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
        List<VoteResult> existingVotes = voteResultRepository.findByVoteIdAndUserId(voteId, sessionId);
        if (!existingVotes.isEmpty()) {
            throw new IllegalArgumentException("You have already voted.");
        }

        Vote vote = getVoteById(voteId);
        Options option = optionRepository.findByVoteIdAndOptionNumber(voteId, optionNumber).orElseThrow(() -> new RuntimeException("Option not found"));

        VoteResult voteResult = new VoteResult();
        voteResult.setOptionNumber(optionNumber);
        voteResult.setVote(vote);
        voteResult.setUserId(sessionId);

        voteResultRepository.save(voteResult);
    }

    // 중복 투표 처리
    @Transactional
    public void voteMultiple(Long voteId, List<Long> optionNumbers, String sessionId) {
        List<VoteResult> existingVotes = voteResultRepository.findByVoteIdAndUserId(voteId, sessionId);
        if (!existingVotes.isEmpty()) {
            throw new IllegalArgumentException("You have already voted.");
        }

        Vote vote = getVoteById(voteId);

        for (Long optionNumber : optionNumbers) {
            Options option = optionRepository.findByVoteIdAndOptionNumber(voteId, optionNumber)
                    .orElseThrow(() -> new RuntimeException("Option not found"));

            VoteResult voteResult = new VoteResult();
            voteResult.setOptionNumber(optionNumber);
            voteResult.setVote(vote);
            voteResult.setUserId(sessionId);
            voteResultRepository.save(voteResult);
        }
    }

    public Long getUniqueUserCountByVoteId(Long voteId) {
        List<VoteResult> voteResults = voteResultRepository.findByVoteId(voteId);
        Set<String> uniqueUserIds = voteResults.stream()
                .map(VoteResult::getUserId)
                .collect(Collectors.toSet());
        return (long) uniqueUserIds.size();
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

    public boolean checkVotePassword(Long voteId, String password) {
        Vote vote = voteRepository.findById(voteId).orElse(null);
        return vote != null && password.equals(vote.getVotePassword());
    }

    public VoteDTO convertToDTO(Vote vote) {
        return VoteDTO.builder()
                .id(vote.getId())
                .kakaoId(vote.getKakaoId())
                .votePassword(vote.getVotePassword())
                .meetId(vote.getMeet() != null ? vote.getMeet().getId() : null)
                .voteType(vote.getVoteType())
                .endTime(vote.getEndTime())
                .allowMultipleVotes(vote.getAllowMultipleVotes())
                .maxOptions(vote.getMaxOptions())
                .title(vote.getTitle())
                .formattedCreatedDate(vote.getFormattedCreatedDate())
                .updYn(vote.getUpdYn())
                .delYn(vote.getDelYn())
                .createdDate(vote.getCreatedDate())
                .updatedDate(vote.getUpdatedDate())
                .build();
    }

    // Convert VoteDTO to Vote entity
    public Vote convertToEntity(VoteDTO voteDTO) {
        Meet meet = null;
        if (voteDTO.getMeetId() != null) {
            meet = meetRepository.findById(voteDTO.getMeetId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid meet ID: " + voteDTO.getMeetId()));
        }

        return Vote.builder()
                .id(voteDTO.getId())
                .kakaoId(voteDTO.getKakaoId())
                .votePassword(voteDTO.getVotePassword())
                .meet(meet)
                .voteType(voteDTO.getVoteType())
                .endTime(voteDTO.getEndTime())
                .allowMultipleVotes(voteDTO.getAllowMultipleVotes())
                .maxOptions(voteDTO.getMaxOptions())
                .title(voteDTO.getTitle())
                .formattedCreatedDate(voteDTO.getFormattedCreatedDate())
                .updYn("Y")
                .delYn("N")
                .createdDate(voteDTO.getCreatedDate())
                .updatedDate(voteDTO.getUpdatedDate())
                .build();
    }

    // Fetch Vote by ID and return as VoteDTO
    public VoteDTO getVoteDTOById(Long id) {
        Vote vote = voteRepository.findById(id).orElseThrow(() -> new RuntimeException("Vote not found"));
        return convertToDTO(vote);
    }
}