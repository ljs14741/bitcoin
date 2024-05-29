package com.example.bitcoin.controller;

import com.example.bitcoin.entity.Lotto;
import com.example.bitcoin.repository.LottoRepository;
import com.example.bitcoin.service.LottoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class LottoController {

    @Autowired
    private LottoService lottoService;

    @Autowired
    private LottoRepository lottoRepository;

    // 매주 일요일 오전 6시 로또 번호 insert
    @Scheduled(cron = "0 0 6 * * SUN")
    @ResponseBody
    public String saveLotto() {
        try {
            lottoService.saveLotto();
            return "Data fetched and stored successfully.";
        } catch (IOException e) {
            return "Failed to fetch data: " + e.getMessage();
        }
    }

    // 로또 번호 조회
    @RequestMapping("/getLotto")
    public String getLotto(Model model) {
//        List<Lotto> lottoList = lottoRepository.findAll();
        List<Lotto> lottoList = lottoRepository.findByOrderByRoundNumberDesc();
        model.addAttribute("lottoList", lottoList);

        Map<Integer, Long> numberFrequencies = lottoService.getNumberFrequencies();
        model.addAttribute("numberFrequencies", numberFrequencies);
        return "lotto";
    }
}
