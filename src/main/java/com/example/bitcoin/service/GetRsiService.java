package com.example.bitcoin.service;

import com.example.bitcoin.common.JsonTransfer;
import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.dto.CandleDTO;
import com.example.bitcoin.dto.RsiDTO;
import com.example.bitcoin.entity.CoinKind;
import com.example.bitcoin.entity.Rsi;
import com.example.bitcoin.repository.RsiRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetRsiService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RequestUpbitURL requestUpbitURL;

    @Autowired
    private CoinKindService coinKindService;

    @Autowired
    RsiRepository rsiRepository;

    //RSI 구하기
    public double getRsi(String dttm, String unit, String market) throws IOException, JSONException, ParseException {

        String url = "https://api.upbit.com/v1/candles/"+unit+"?market="+market+"&to="+dttm+"&count=200";
        String data = requestUpbitURL.request(url);
        JSONArray jsonArray = new JSONArray(data);
        List<CandleDTO> list = JsonTransfer.getListObjectFromJSONObject(jsonArray, new TypeReference<CandleDTO>() {
        });

        //rsi 계산
        double rsi = new CalculateRsiService().calculateRsi(list);

        return rsi;

    }

    public void updateRsiValues() throws IOException, ParseException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dttm = now.format(formatter);

        List<CoinKind> coinKinds = coinKindService.getAllCoinKinds();

        for (CoinKind coinKind : coinKinds) {
            String market = coinKind.getMarket();

            Optional<Rsi> optionalRsi = rsiRepository.findByMarket(market);
            if (optionalRsi.isPresent()) {
                Rsi existingRsi = optionalRsi.get();
                double rsi15 = getRsi(dttm, "minutes/15", market);
                rsi15 = Math.round(rsi15 * 100.0) / 100.0;
                existingRsi.setRsi15(rsi15);
                double rsi60 = getRsi(dttm, "minutes/60", market);
                rsi60 = Math.round(rsi60 * 100.0) / 100.0;
                existingRsi.setRsi60(rsi60);
                double rsiDaily = getRsi(dttm, "days", market);
                rsiDaily = Math.round(rsiDaily * 100.0) / 100.0;
                existingRsi.setRsiDaily(rsiDaily);
                double rsiWeekly = getRsi(dttm, "weeks", market);
                rsiWeekly = Math.round(rsiWeekly * 100.0) / 100.0;
                existingRsi.setRsiWeekly(rsiWeekly);
                double rsiMonthly = getRsi(dttm, "months", market);
                rsiMonthly = Math.round(rsiMonthly * 100.0) / 100.0;
                existingRsi.setRsiMonthly(rsiMonthly);
                existingRsi.setUpdatedAt(now);
                rsiRepository.save(existingRsi);
                try {
                    Thread.sleep(300); // 0.3초 딜레이
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                getRsiSummary();
            }
        }
    }

    // 60분봉, 일봉, 주봉, 월봉 RSI 전부 구하기
    public void getRsiSummary() throws IOException, ParseException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dttm = now.format(formatter);

        // 코인 정보 가져오기
        List<CoinKind> coinKinds = coinKindService.getAllCoinKinds();

        for (CoinKind coinKind : coinKinds) {
            log.info("coinKind: " + coinKind);
        }

        // 각 코인별로 RSI 값 계산
        List<RsiDTO> rsiSummaryList = new ArrayList<>();
        for (CoinKind coinKind : coinKinds) {
            String market = coinKind.getMarket();
            String koreanName = coinKind.getKoreanName();

            // 15분봉 RSI 계산
            double rsi15 = getRsi(dttm, "minutes/15", market);
            rsi15 = Math.round(rsi15 * 100.0) / 100.0; // 소수점 두 번째 자리에서 반올림

            // 60분봉 RSI 계산
            double rsi60 = getRsi(dttm, "minutes/60", market);
            rsi60 = Math.round(rsi60 * 100.0) / 100.0; // 소수점 두 번째 자리에서 반올림

            // 일별 RSI 계산
            double rsiDaily = getRsi(dttm, "days", market);
            rsiDaily = Math.round(rsiDaily * 100.0) / 100.0; // 소수점 두 번째 자리에서 반올림

            // 주별 RSI 계산
            double rsiWeekly = getRsi(dttm, "weeks", market);
            rsiWeekly = Math.round(rsiWeekly * 100.0) / 100.0; // 소수점 두 번째 자리에서 반올림

            // 월별 RSI 계산
            double rsiMonthly = getRsi(dttm, "months", market);
            rsiMonthly = Math.round(rsiMonthly * 100.0) / 100.0; // 소수점 두 번째 자리에서 반올림

            if(Double.isNaN(rsi15)) {rsi15 = 0.0;}
            if(Double.isNaN(rsi60)) {rsi60 = 0.0;}
            if(Double.isNaN(rsiDaily)) {rsiDaily = 0.0;}
            if(Double.isNaN(rsiWeekly)) {rsiWeekly = 0.0;}
            if(Double.isNaN(rsiMonthly)) {rsiMonthly = 0.0;}

            // RSI 값을 데이터베이스에 저장
            saveRsi(market, koreanName, rsi15, rsi60, rsiDaily, rsiWeekly, rsiMonthly);
        }
    }

    // RSI 값을 데이터베이스에 저장하는 메서드
    private void saveRsi(String market, String koreanName, double rsi15, double rsi60, double rsiDaily, double rsiWeekly, double rsiMonthly) {
        LocalDateTime now = LocalDateTime.now();

        Rsi rsi = Rsi.builder()
                .market(market)
                .koreanName(koreanName)
                .rsi15(rsi15)
                .rsi60(rsi60)
                .rsiDaily(rsiDaily)
                .rsiWeekly(rsiWeekly)
                .rsiMonthly(rsiMonthly)
                .createdAt(now)
                .updatedAt(now)
                .build();
        rsiRepository.save(rsi);
    }

    public List<RsiDTO> getRsi() {
        List<Rsi> rsiList = rsiRepository.findAllByOrderByRsi15AscRsi60AscRsiDailyAscRsiWeeklyAscRsiMonthlyAsc();
        return rsiList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RsiDTO convertToDto(Rsi rsi) {
        return modelMapper.map(rsi, RsiDTO.class);
    }
}
