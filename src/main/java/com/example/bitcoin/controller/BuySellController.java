package com.example.bitcoin.controller;

import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.service.BuySellOrderService;
import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;


@Slf4j
@RestController
public class BuySellController {

    @Autowired
    GetRsiService getRsiService;

    @Autowired
    BuySellOrderService buyCoinService;

    @Autowired
    RequestUpbitURL requestUpbitURL;

    @Autowired
    BuySellOrderService buySellOrderService;




    // 매수, 매도 요청
    @RequestMapping("/BuyCoinController.buySellOrder.do")
    public void buySellOrder() throws IOException, ParseException, NoSuchAlgorithmException {
        buySellOrderService.buySellOrder();

    }
}
