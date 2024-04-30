package com.example.bitcoin.controller;

import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;


@Slf4j
@RestController
public class BuyCoinController {

    @Autowired
    GetRsiService getRsiService;

    public String dttm = "2023-10-04 09:00:00";
    public String unit = "minutes/240";
    public String market = "KRW-XRP";

    // rsi 값 구하기

    @RequestMapping("/BuyCoinController.getRsi.do")
    public void getRsi() throws IOException, ParseException {
        double rsi = getRsiService.getRsi(dttm, unit, market);
        log.info("rsi: " + rsi);
    }


    // 매수하기

}
