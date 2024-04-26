package com.example.bitcoin.controller;

import com.example.bitcoin.service.GetRsiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.text.ParseException;

@Controller
public class BuyCoinController {

    @Autowired
    GetRsiService getRsiService;

    public String dttm = "2023-10-04 09:00:00";
    public String unit = "minutes/240";
    public String market = "KRW-XRP";

    // rsi 값 구하기

    @RequestMapping("/BuyCoinController.getRsi.do")
    public void getRsi() throws IOException, ParseException {
        getRsiService.getRsi(dttm, unit, market);
    }


    // 매수하기

}
