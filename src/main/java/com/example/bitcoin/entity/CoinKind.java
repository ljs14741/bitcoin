package com.example.bitcoin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@Entity(name="coinkind")// class에 지정할 테이블명
@Slf4j
public class CoinKind {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK를 생성 전략 설정 GenerationType.SEQUENCE
    @Column(name = "coin_kind_id")
    @JsonProperty("id")
    private Long id;

    @Column(name = "market")
    @JsonProperty("market")
    private String market;

    @Column(name = "korean_name")
    @JsonProperty("korean_name")
    private String koreanName;

    @Column(name = "english_name")
    @JsonProperty("english_name")
    private String englishName;

    @Column(name = "market_warning")
    @JsonProperty("market_warning")
    private String marketWarning;
}
