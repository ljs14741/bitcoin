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
@Entity(name="vote")// class에 지정할 테이블명
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long id;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "vote_password")
    private String votePassword;

    @ManyToOne
    @JoinColumn(name = "meet_id")
    private Meet meet;

    // 투표 종류 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type")
    private VoteType voteType;

    @Column(name = "allow_multiple_votes")
    private Boolean allowMultipleVotes = false;

    @Column(name = "upd_yn")
    private String updYn = "N";

    @Column(name = "del_yn")
    private String delYn = "N";

    // 투표 종료시간 추가
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @JoinColumn(name = "title")
    private String title;

    @Transient
    @Column(name = "formatted_created_date")
    private String formattedCreatedDate;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public enum VoteType {
        PUBLIC,
        PRIVATE
    }

    public String getFormattedCreatedDate() {
        return formattedCreatedDate;
    }

    public void setFormattedCreatedDate(String formattedCreatedDate) {
        this.formattedCreatedDate = formattedCreatedDate;
    }

}
