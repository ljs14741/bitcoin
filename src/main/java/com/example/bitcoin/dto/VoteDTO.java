package com.example.bitcoin.dto;

import com.example.bitcoin.entity.Meet;
import com.example.bitcoin.entity.Vote;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@AllArgsConstructor //생성자 자동 완성  // 이걸쓰면 밑에 CoinKindEntity Builder로 생성자 생성을 안해도되는듯
@NoArgsConstructor //생성자 자동 완성
public class VoteDTO {

    private Long id;
    private Long meetId;
    private Long kakaoId;
    private String votePassword;
//    private Meet meet;
    private Vote.VoteType voteType;
    private Boolean allowMultipleVotes;
    private Integer maxOptions;
    private LocalDateTime endTime;
    private String title;
    private String formattedCreatedDate;
    private String updYn;
    private String delYn;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}