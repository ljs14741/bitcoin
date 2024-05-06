package com.example.bitcoin.controller;

import com.example.bitcoin.entity.CoinKind;
import com.example.bitcoin.repository.CoinKindRepository;
import com.example.bitcoin.service.BuySellOrderService;
import com.example.bitcoin.service.CoinKindService;
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
import java.util.List;


@Slf4j
@RestController
//@Controller
public class GetRsiController {

    @Autowired
    CoinKindService coinKindService;

    @Autowired
    GetRsiService getRsiService;

    @Autowired
    CoinKindRepository coinKindRepository;

    public String dttm;
    public String unit;
    public String market;




    //
    @RequestMapping("/GetRsiController.getRsi.do")
    public void getRsi() throws IOException, ParseException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dttm = now.format(formatter);

        // 1. 코인 정보
        List<CoinKind> coinKinds = coinKindService.getAllCoinKinds();

        // 2. rsi 계산
        for (CoinKind coinKind : coinKinds) {
            market = coinKind.getMarket();

            // 2. 60분봉 rsi
            double rsi60 = getRsiService.getRsi(dttm, "minutes/60", market);
//            log.info("market: " + market + "rsi: " + rsi);

            // 2. 일봉 rsi
            double rsiDaily = getRsiService.getRsi(dttm, "days", market);

            // 2. 주봉 rsi
            double rsiWeekly = getRsiService.getRsi(dttm, "weeks", market);

            // 2. 월봉 rsi
            double rsiMonthly = getRsiService.getRsi(dttm, "months", market);

            log.info("Market: " + market +
                    " | 60분봉 RSI: " + rsi60 +
                    " | 일별 RSI: " + rsiDaily +
                    " | 주별 RSI: " + rsiWeekly +
                    " | 월별 RSI: " + rsiMonthly);
        }

    }
}
