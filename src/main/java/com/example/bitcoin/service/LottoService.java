package com.example.bitcoin.service;

import com.example.bitcoin.dto.LottoDTO;
import com.example.bitcoin.entity.Lotto;
import com.example.bitcoin.repository.LottoRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LottoService {

    @Autowired
    private LottoRepository lottoRepository;
    private static final String URL = "https://www.dhlottery.co.kr/gameResult.do?method=byWin";

    private static final String PAST_URL = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=";

    // 로또 번호 매주 추가
    @Transactional
    public void saveLotto() throws IOException {

        // 새로운 회차의 번호 조회
        Lotto latestLotto = lottoRepository.findFirstByOrderByRoundNumberDesc();
        int number = latestLotto.getRoundNumber();
        number = number +1;
        String drwNo = String.valueOf(number);

        // URL 생성
        String url = PAST_URL + drwNo;

        // URL로부터 HTML 문서 가져오기
        Document document = Jsoup.connect(url).ignoreContentType(true).get();

        // JSON 데이터 파싱
        JSONObject jsonData = new JSONObject(document.text());

        // 필요한 정보 추출
        int roundNumber = jsonData.getInt("drwNo");
        List<Integer> winningNumbers = new ArrayList<>();
        winningNumbers.add(jsonData.getInt("drwtNo1"));
        winningNumbers.add(jsonData.getInt("drwtNo2"));
        winningNumbers.add(jsonData.getInt("drwtNo3"));
        winningNumbers.add(jsonData.getInt("drwtNo4"));
        winningNumbers.add(jsonData.getInt("drwtNo5"));
        winningNumbers.add(jsonData.getInt("drwtNo6"));

        // 로또 번호 저장
        Lotto lotto = new Lotto();
        lotto.setRoundNumber(roundNumber);
        lotto.setNumber1(winningNumbers.get(0));
        lotto.setNumber2(winningNumbers.get(1));
        lotto.setNumber3(winningNumbers.get(2));
        lotto.setNumber4(winningNumbers.get(3));
        lotto.setNumber5(winningNumbers.get(4));
        lotto.setNumber6(winningNumbers.get(5));

        lottoRepository.save(lotto);
    }

    public Map<Integer, Long> getNumberFrequencies() {
        List<Object[]> results = lottoRepository.findNumberFrequencies();
        Map<Integer, Long> numberFrequencyMap = new HashMap<>();
        for (Object[] result : results) {
            Integer number = (Integer) result[0];
            Long count = (Long) result[1];
            numberFrequencyMap.put(number, count);
        }
        return numberFrequencyMap;
    }
}
