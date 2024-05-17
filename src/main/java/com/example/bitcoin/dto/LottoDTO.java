package com.example.bitcoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성  // 이걸쓰면 밑에 CoinKindEntity Builder로 생성자 생성을 안해도되는듯
@NoArgsConstructor //생성자 자동 완성
public class LottoDTO {
    private int number1;
    private int number2;
    private int number3;
    private int number4;
    private int number5;
    private int number6;
    private List<Integer> numbers;

    public void setWinningNumbers(List<Integer> winningNumbers) {
        if (winningNumbers.size() != 6) {
            throw new IllegalArgumentException("당첨 번호 리스트의 크기는 6이어야 합니다.");
        }

        this.number1 = winningNumbers.get(0);
        this.number2 = winningNumbers.get(1);
        this.number3 = winningNumbers.get(2);
        this.number4 = winningNumbers.get(3);
        this.number5 = winningNumbers.get(4);
        this.number6 = winningNumbers.get(5);
    }
}
