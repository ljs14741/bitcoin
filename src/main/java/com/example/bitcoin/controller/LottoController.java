package com.example.bitcoin.controller;

import com.example.bitcoin.entity.Lotto;
import com.example.bitcoin.repository.LottoRepository;
import com.example.bitcoin.service.LottoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Controller
public class LottoController {

    @Autowired
    private LottoService lottoService;

    @Autowired
    private LottoRepository lottoRepository;

    @GetMapping("/saveLotto")
    public String fetchLotteryData() {
        try {
            lottoService.pastnumbers(); // 현재 1119회 까지 한번에 insert
//            lottoService.fetchAndStoreWinningNumbers(); // 최신 회차 insert
            return "Data fetched and stored successfully.";
        } catch (IOException e) {
            return "Failed to fetch data: " + e.getMessage();
        }
    }

    @RequestMapping("/getLotto")
    public String getLotto(Model model) {
        List<Lotto> lottoList = lottoRepository.findAll();
        model.addAttribute("lottoList", lottoList);
        return "lotto";
    }

}
