package com.example.bitcoin.service;

import com.example.bitcoin.dto.MeetDTO;
import com.example.bitcoin.entity.Meet;
import com.example.bitcoin.entity.Options;
import com.example.bitcoin.entity.Vote;
import com.example.bitcoin.repository.MeetRepository;
import com.example.bitcoin.repository.OptionRepository;
import com.example.bitcoin.repository.VoteRepository;
import com.example.bitcoin.repository.VoteResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeetService {

    @Autowired
    private MeetRepository meetRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private VoteResultRepository voteResultRepository;

    public MeetDTO createMeet(MeetDTO meetDTO) {
        Meet meet = Meet.builder()
                .meetName(meetDTO.getMeetName())
                .createdPassword(meetDTO.getCreatedPassword())
                .meetPassword(meetDTO.getMeetPassword())
                .endDateTime(meetDTO.getEndDateTime())
                .build();
        meetRepository.save(meet);
        meetDTO.setId(meet.getId());
        meetDTO.setCreatedDate(meet.getCreatedDate());
        meetDTO.setUpdatedDate(meet.getUpdatedDate());
        return meetDTO;
    }

    public List<MeetDTO> getAllMeets() {
        return meetRepository.findAllByOrderByCreatedDateDesc().stream().map(meet -> {
            MeetDTO meetDTO = new MeetDTO();
            meetDTO.setId(meet.getId());
            meetDTO.setMeetName(meet.getMeetName());
            meetDTO.setCreatedPassword(meet.getCreatedPassword());
            meetDTO.setMeetPassword(meet.getMeetPassword());
            meetDTO.setEndDateTime(meet.getEndDateTime());
            meetDTO.setCreatedDate(meet.getCreatedDate());
            meetDTO.setUpdatedDate(meet.getUpdatedDate());
            return meetDTO;
        }).collect(Collectors.toList());
    }

    public Optional<MeetDTO> getMeetById(Long id) {
        return meetRepository.findById(id).map(meet -> {
            MeetDTO meetDTO = new MeetDTO();
            meetDTO.setId(meet.getId());
            meetDTO.setMeetName(meet.getMeetName());
            meetDTO.setCreatedPassword(meet.getCreatedPassword());
            meetDTO.setMeetPassword(meet.getMeetPassword());
            meetDTO.setEndDateTime(meet.getEndDateTime());
            meetDTO.setCreatedDate(meet.getCreatedDate());
            meetDTO.setUpdatedDate(meet.getUpdatedDate());

            System.out.println("Retrieved Meet: " + meet.getMeetName() + ", Created Password: " + meet.getCreatedPassword() + ", Meet Password: " + meet.getMeetPassword());
            return meetDTO;
        });
    }

    public void updateMeet(Long id, MeetDTO meetDTO) {
        meetRepository.findById(id).ifPresent(meet -> {
            meet.setMeetName(meetDTO.getMeetName());
            meet.setCreatedPassword(meetDTO.getCreatedPassword());
            meet.setMeetPassword(meetDTO.getMeetPassword());
            meet.setEndDateTime(meetDTO.getEndDateTime());
            meetRepository.save(meet);
        });
    }

    @Transactional
    public void deleteMeet(Long id) {

        // 1. 해당 모임과 관련된 모든 투표 가져오기
        List<Vote> votes = voteRepository.findByMeetId(id);

        for (Vote vote : votes) {
            // 2. 각 투표의 투표 결과 삭제
            voteResultRepository.deleteByVoteId(vote.getId());

            // 3. 각 투표와 관련된 모든 옵션 삭제
            List<Options> options = optionRepository.findByVoteId(vote.getId());
            for (Options option : options) {
                optionRepository.delete(option);
            }

            // 4. 투표 삭제
            voteRepository.delete(vote);
        }

        // 5. 마지막으로 모임 삭제
        meetRepository.deleteById(id);
    }

    @Transactional
    public void deleteExpiredMeets() {
        LocalDateTime now = LocalDateTime.now();
        List<Meet> expiredMeets = meetRepository.findByEndDateTimeBefore(now);
        for (Meet meet : expiredMeets) {
            deleteMeet(meet.getId());
        }
    }
}