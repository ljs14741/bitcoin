package com.example.bitcoin.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@Entity(name="visitor")// class에 지정할 테이블명
@Slf4j
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visitor_id")
    private Long id;

    @Column(name = "daily_count")
    private int dailyCount;

    @Column(name = "total_count")
    private int totalCount;

    @Column(name = "date")
    private LocalDate date;

    public Visitor() {
        this.date = LocalDate.now();
        this.totalCount = 0;
        this.dailyCount = 0;
    }
}
