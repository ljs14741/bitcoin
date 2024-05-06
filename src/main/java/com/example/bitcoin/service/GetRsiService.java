package com.example.bitcoin.service;

import com.example.bitcoin.common.JsonTransfer;
import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.dto.CandleDTO;
import com.example.bitcoin.dto.RsiSummaryDTO;
import com.example.bitcoin.entity.CoinKind;
import com.example.bitcoin.repository.CoinKindRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetRsiService {
    @Autowired
    private RequestUpbitURL requestUpbitURL;

    @Autowired
    private CoinKindService coinKindService;

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

    // 60분봉, 일봉, 주봉, 월봉 RSI 전부 구하기
    public List<RsiSummaryDTO> getRsiSummary() throws IOException, ParseException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dttm = now.format(formatter);

        // 코인 정보 가져오기
        List<CoinKind> coinKinds = coinKindService.getAllCoinKinds();

        // 각 코인별로 RSI 값 계산
        List<RsiSummaryDTO> rsiSummaryList = new ArrayList<>();
        for (CoinKind coinKind : coinKinds) {
            String market = coinKind.getMarket();

            // 60분봉 RSI 계산
            double rsi60 = getRsi(dttm, "minutes/60", market);

            // 일별 RSI 계산
            double rsiDaily = getRsi(dttm, "days", market);

            // 주별 RSI 계산
            double rsiWeekly = getRsi(dttm, "weeks", market);

            // 월별 RSI 계산
            double rsiMonthly = getRsi(dttm, "months", market);

            // RSI 값을 객체에 저장
            RsiSummaryDTO rsiSummary = new RsiSummaryDTO(market, rsi60, rsiDaily, rsiWeekly, rsiMonthly);
            rsiSummaryList.add(rsiSummary);
        }

        return rsiSummaryList;
    }
}
