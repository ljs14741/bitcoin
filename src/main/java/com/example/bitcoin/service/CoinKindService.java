package com.example.bitcoin.service;

import com.example.bitcoin.common.JsonTransfer;
import com.example.bitcoin.common.RequestUpbitURL;
import com.example.bitcoin.dto.CandleDTO;
import com.example.bitcoin.dto.CoinKindDTO;
import com.example.bitcoin.entity.CoinKind;
import com.example.bitcoin.repository.CoinKindRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CoinKindService {

    @Autowired
    RequestUpbitURL requestUpbitURL;

    @Autowired
    CoinKindRepository coinKindRepository;

    public List<CoinKind> getAllCoinKinds() {
//        Pageable pageable = PageRequest.of(0, 5);
        return coinKindRepository.findByMarketLike("KRW");
    }


    @Transactional
    public void coinKind() {
        try {
            String url = "https://api.upbit.com/v1/market/all?isDetails=false";
            String data = requestUpbitURL.request(url);
            JSONArray jsonArray = new JSONArray(data);
            List<CoinKindDTO> list = JsonTransfer.getListObjectFromJSONObject(jsonArray, new TypeReference<CoinKindDTO>() {
            });

            // CoinKindDTO를 CoinKind 엔티티로 변환하여 저장
            for (CoinKindDTO dto : list) {
                CoinKind coinKind = CoinKind.builder()
                        .market(dto.getMarket())
                        .koreanName(dto.getKoreanName())
                        .englishName(dto.getEnglishName())
                        .marketWarning(dto.getMarketWarning())
                        .build();
                coinKindRepository.save(coinKind);
            }

            log.info("CoinKind Likt: " + list);
        } catch (Exception e) {
            log.error("CoinKind 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("CoinKind 저장 중 오류 발생", e);
        }
    }
}
