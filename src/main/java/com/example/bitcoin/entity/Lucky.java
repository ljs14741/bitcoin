package com.example.bitcoin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@EntityListeners(AuditingEntityListener.class)
@Entity(name="lucky")
public class Lucky {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lucky_id")
    private Long luckyId;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "lucky_title", columnDefinition = "TEXT")
    private String luckyTitle;

    @Column(name = "lucky_detail", columnDefinition = "TEXT")
    private String luckyDetail;

    @CreatedDate
    @Column(updatable = false, name = "created_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}