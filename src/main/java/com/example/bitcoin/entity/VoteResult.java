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
@Entity(name="vote_result")// class에 지정할 테이블명
public class VoteResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_result_id")
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "option_id")
//    @Column(name = "option_id")
//    private Options option;

    @ManyToOne
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @Column(name = "option_number")
    private Long optionNumber;

    @Column(name = "count")
    private int count=0;

    @Column(name = "user_id")
    private String userId;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
