package com.example.bitcoin;

import com.example.bitcoin.entity.Lotto;
import com.example.bitcoin.repository.LottoRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class LottoServiceTest {

    @Autowired
    LottoRepository lottoRepository;

    @Test
    public void lottoTest() {
        // 새로운 회차의 번호 조회
        Lotto latestLotto = lottoRepository.findFirstByOrderByRoundNumberDesc();
        int number = latestLotto.getRoundNumber();
        number = number +1;
        String sdrwNo = String.valueOf(number);
        log.info("가나: " + sdrwNo);



        String drwNo = String.valueOf(latestLotto.getRoundNumber());
        log.info("drwNo: " + drwNo);
        log.info("drwNo+1: " + drwNo + 1);

    }
}
