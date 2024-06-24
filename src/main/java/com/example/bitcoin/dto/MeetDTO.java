package com.example.bitcoin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@AllArgsConstructor //생성자 자동 완성  // 이걸쓰면 밑에 CoinKindEntity Builder로 생성자 생성을 안해도되는듯
@NoArgsConstructor //생성자 자동 완성
public class MeetDTO {
    private Long id;
    private String meetName;
    private String createdPassword;
    private String meetPassword;
    private LocalDateTime endDateTime;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
