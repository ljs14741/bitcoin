package com.example.bitcoin.controller;

import com.example.bitcoin.service.BuySellOrderService;
import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestController
//@Controller
public class GetRsiController {

    @Autowired
    GetRsiService getRsiService;

    public String dttm;
    public String unit;
    public String market;




    // 매수, 매도 요청
    @RequestMapping("/GetRsiController.getRsiDay.do")
    public void getRsiDay() throws IOException, ParseException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dttm = now.format(formatter);
        unit = "minutes/60";
        market = "KRW-XRP";

        // 1. 코인 정보

        // 2. 60분봉 rsi
        getRsiService.getRsi(dttm, unit, market);
    }
}
