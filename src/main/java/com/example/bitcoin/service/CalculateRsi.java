package com.example.bitcoin.service;

import com.example.bitcoin.dto.CandleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CalculateRsi {

    public Double calculateRsi(List<CandleDTO> list){

        //------------- rsi 계산 //   https://herojoon-dev.tistory.com/156
        Collections.reverse(list);
        double zero = 0;
        List<Double> upList = new ArrayList<>();
        List<Double> downList = new ArrayList<>();

        List<String> dou2_dou1List = new ArrayList<String>();
        List<String> upupList = new ArrayList<String>();
        List<String> downdownList = new ArrayList<String>();

        for(int i=0; i < list.size()-1; i++) {
            double dou2 = list.get(i+1).getTradePrice();
            double dou1 = list.get(i).getTradePrice();
            double gapByTradePrice = dou2-dou1;

            /////
            dou2_dou1List.add(String.valueOf(gapByTradePrice));

            /////
            if(gapByTradePrice > 0) {
                upList.add(gapByTradePrice);
                downList.add(zero);
            } else if(gapByTradePrice < 0) {
                downList.add(gapByTradePrice * -1);
                upList.add(zero);
            } else {
                upList.add(zero);
                downList.add(zero);
            }
        }


        double day = 14;
        double a = (double) 1 / (1 + (day-1));
        //double a = 2.0 / (day + 1);

        //AU
        double upEma = 0;
        if(!CollectionUtils.isEmpty(upList)) {
            upEma = upList.get(0).doubleValue();
            if(upList.size() > 1) {
                for(int i=1; i<upList.size(); i++) {
                    upEma = (upList.get(i).doubleValue() * a) + (upEma * (1-a));

                    upupList.add(String.valueOf(upEma));
                }
            }
        }

        // AD
        double downEma = 0;  // 하락 값의 지수이동평균
        if(!CollectionUtils.isEmpty(downList)) {
            downEma = downList.get(0).doubleValue();
            if(downList.size() > 1) {
                for(int i=1; i<downList.size(); i++) {
                    downEma = (downList.get(i).doubleValue() * a) + (downEma * (1 - a));

                    downdownList.add(String.valueOf(downEma));
                }
            }
        }

        // rsi 계산
        double au = upEma;
        double ad = downEma;
        double rs = au / ad;
        double rsi = 100 - (100 / (1 + rs));

        return rsi;

    }
}
