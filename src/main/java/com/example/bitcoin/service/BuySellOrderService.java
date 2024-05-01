package com.example.bitcoin.service;

import com.example.bitcoin.common.RequestUpbitURL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class BuySellOrderService {

    @Autowired
    RequestUpbitURL requestUpbitURL;

    @Autowired
    GetRsiService getRsiService;

    public String dttm;
    public String unit = "minutes/60";
    public String market = "KRW-XRP";

    // 현재 시간을 LocalDateTime 객체로 가져오기

    public void buySellOrder() throws IOException, ParseException, NoSuchAlgorithmException {

        LocalDateTime now = LocalDateTime.now();

        // 출력 형식 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // LocalDateTime 객체를 지정된 형식의 문자열로 변환
        dttm = now.format(formatter);

        // 1. 해당 코인을 보유하고 있는지 여부 조회
        boolean existsYn = requestUpbitURL.getAccounts();

        // 2. RSI 값 구하기
        double rsi = getRsiService.getRsi(dttm, unit, market);

        /* 3. 보유하고 있으면 매도, 보유하지 않으면 매수
              rsi 60이상 매도, rsi 25이하 매수 */
        if(existsYn && rsi < 25.0) {
            buyCoin();
        } else if(!existsYn && rsi > 60.0){
            sellCoin();
        } else {
            log.info("rsi: " + rsi + " rsi값이 매수 및 매도기준에 부합하지 않아 매매를 하지 않곘습니다.");
        }
    }

    public void buyCoin() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        requestUpbitURL.executeTrade("KRW-XRP", "bid","1", "5000", "price");
    }

    public void sellCoin() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        requestUpbitURL.executeTrade("KRW-XRP", "ask","1", "5000", "price");
    }
}
