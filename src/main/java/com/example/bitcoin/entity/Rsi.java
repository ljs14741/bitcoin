package com.example.bitcoin.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@Entity
public class Rsi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rsi_id")
    private Long id;

    @Column(name = "market")
    private String market;

    @Column(name = "korean_name")
    private String koreanName;

    @Column(name = "rsi15")
    private double rsi15;

    @Column(name = "rsi60")
    private double rsi60;

    @Column(name = "rsi_daily")
    private double rsiDaily;

    @Column(name = "rsi_weekly")
    private double rsiWeekly;

    @Column(name = "rsi_monthly")
    private double rsiMonthly;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}