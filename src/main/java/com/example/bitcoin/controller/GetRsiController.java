package com.example.bitcoin.controller;

import com.example.bitcoin.dto.RsiSummaryDTO;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Controller
public class GetRsiController {

    @Autowired
    GetRsiService getRsiService;

//    @RequestMapping("/GetRsiController.getRsi.do")
    @RequestMapping("/rsiSummary")
    public String getRsiSummary(Model model) throws IOException, ParseException, NoSuchAlgorithmException {
        List<RsiSummaryDTO> rsiSummaryList = getRsiService.getRsiSummary();
        model.addAttribute("rsiSummaryList", rsiSummaryList);
        log.info("아앙?");
        log.info("rsiSummaryList: " + rsiSummaryList.get(0).getMarket());
        log.info("rsiSummaryList: " + rsiSummaryList.get(1).getMarket());
        log.info("rsiSummaryList: " + rsiSummaryList.get(2).getMarket());
        log.info("rsiSummaryList: " + rsiSummaryList.get(2).getRsi60());
        return "rsiSummary";
    }
}
