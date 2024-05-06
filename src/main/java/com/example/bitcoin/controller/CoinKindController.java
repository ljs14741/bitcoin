package com.example.bitcoin.controller;


import com.example.bitcoin.service.CoinKindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoinKindController {

    @Autowired
    CoinKindService coinKindService;

    @RequestMapping("/CoinKindController.coinKind.do")
    public void coinKind() {
        coinKindService.coinKind();
    }
}
