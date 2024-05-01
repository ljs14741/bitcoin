package com.example.bitcoin.controller;

import com.example.bitcoin.service.BuyCoinService;
import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;


@Slf4j
@RestController
public class BuyCoinController {

    @Autowired
    GetRsiService getRsiService;

    @Autowired
    BuyCoinService buyCoinService;

    public String dttm = "2023-10-04 09:00:00";
    public String unit = "minutes/240";
    public String market = "KRW-XRP";

    // rsi 값 구하기

    @RequestMapping("/BuyCoinController.buyCoin.do")
    public void buyCoin() throws IOException, ParseException, NoSuchAlgorithmException {
        // 0. 내가 보유하고 있는지 판단 -> 보유하면 매도기능으로 가야할거고, 없으면 매수기능으로 가야할거고

        // 1. RSI 값 구하기
        double rsi = getRsiService.getRsi(dttm, unit, market);
        log.info("rsi: " + rsi);

        // 2. RSI 값으로 매수
        buyCoinService.buyCoin();

        // 3. RSI 값으로 매도
//        buyCoinService.sellCoin();
    }


    // 매수하기

}
