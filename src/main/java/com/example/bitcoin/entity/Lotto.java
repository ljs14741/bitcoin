package com.example.bitcoin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@Entity(name="lotto")// class에 지정할 테이블명
@Slf4j
public class Lotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK를 생성 전략 설정 GenerationType.SEQUENCE
    @Column(name = "lotto_id")
    private Long id;

    @Column(name = "round_number")
    private int roundNumber;

    @Column(name = "number1")
    private int number1;

    @Column(name = "number2")
    private int number2;

    @Column(name = "number3")
    private int number3;

    @Column(name = "number4")
    private int number4;

    @Column(name = "number5")
    private int number5;

    @Column(name = "number6")
    private int number6;
}