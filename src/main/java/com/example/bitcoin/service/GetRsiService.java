package com.example.bitcoin.service;

import com.example.bitcoin.common.JsonTransfer;
import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.dto.CandleDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetRsiService {
    @Autowired
    private RequestUpbitURL requestUpbitURL;

    public double getRsi(String dttm, String unit, String market) throws IOException, JSONException, ParseException {

        String url = "https://api.upbit.com/v1/candles/"+unit+"?market="+market+"&to="+dttm+"&count=200";
        String data = requestUpbitURL.request(url);
        JSONArray jsonArray = new JSONArray(data);
//        List<Map<String, Object>> list = JsonTransfer.getListMapFromJsonArray(jsonArray);
        List<CandleDTO> list = JsonTransfer.getListObjectFromJSONObject(jsonArray, new TypeReference<CandleDTO>() {
        });

        log.info("가나다라: " + list);

        //rsi 계산
        double rsi = new CalculateRsiService().calculateRsi(list);

        return rsi;

    }
}