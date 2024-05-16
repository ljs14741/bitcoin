package com.example.bitcoin.controller;

import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.service.BuySellOrderService;
import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;


@Slf4j
//@RestController
@Component
public class BuySellController {

    @Autowired
    BuySellOrderService buySellOrderService;


    // 매수, 매도 요청
//    @RequestMapping("/BuyCoinController.buySellOrder.do")
//    @Scheduled(fixedDelay = 600000) //일정시간마다 아래 함수 실행하는 스케쥴러 (1000 -> 1초)
    public void buySellOrder() throws IOException, ParseException, NoSuchAlgorithmException {
        buySellOrderService.buySellOrder();
    }
}
