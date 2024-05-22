package com.example.bitcoin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@Entity(name="options")// class에 지정할 테이블명
public class Options {
    @Id
    @Column(name = "option_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vote_id")
//    @Column(name = "vote_id")
    private Vote vote;

    @Column(name = "option_text")
    private String optionText;

    @OneToMany(mappedBy = "option")
    private List<VoteResult> voteResults;

//    @OneToMany(mappedBy = "options", cascade = CascadeType.ALL)
//    private List<Vote> votes = new ArrayList<>();
}
