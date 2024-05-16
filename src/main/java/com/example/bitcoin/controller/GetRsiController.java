package com.example.bitcoin.controller;

import com.example.bitcoin.dto.RsiDTO;
import com.example.bitcoin.dto.RsiDTO;
import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;


@Slf4j
@Controller
public class GetRsiController {

    @Autowired
    GetRsiService getRsiService;

    //RSI 값 insert
//    @RequestMapping("/GetRsiController.getrsiSummary.do")
////    @ResponseBody
//    public String getRsiSummary(Model model) throws IOException, ParseException, NoSuchAlgorithmException {
//        getRsiService.getRsiSummary();
//        return "redirect:/";
//    }

    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void updateRsiValues() {
        try {
            getRsiService.updateRsiValues();
        } catch (IOException | ParseException | NoSuchAlgorithmException e) {
            e.printStackTrace(); // 에러 처리를 원하는 방식으로 변경 가능
        }
    }

    //코인 RSI 정보 조회
    @RequestMapping("/rsiSummary")
    public String showRsiSummary(Model model) {
        List<RsiDTO> rsiList = getRsiService.getRsi();
        model.addAttribute("rsiList", rsiList);
        return "rsiSummary";
    }
}
