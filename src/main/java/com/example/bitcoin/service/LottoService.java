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

    // 이거는 최신 로또번호 추가할때 스케줄러 쓰면될듯
    public List<LottoDTO> fetchWinningNumbers() throws IOException {
        List<LottoDTO> lottoList = new ArrayList<>();
        Document document = Jsoup.connect(URL).get();

        Elements numbersElements = document.select("div.num.win p span.ball_645");
        LottoDTO lottoDTO = new LottoDTO();
        List<Integer> winningNumbers = new ArrayList<>();

        for (Element numberElement : numbersElements) {
            log.info("로또번호: " + numberElement.text());
            String numberText = numberElement.text().trim();
            int number = Integer.parseInt(numberText);
            winningNumbers.add(number);
        }

        lottoDTO.setWinningNumbers(winningNumbers);
        lottoList.add(lottoDTO);

        return lottoList;
    }

    @Transactional
    public void fetchAndStoreWinningNumbers() throws IOException {
        List<LottoDTO> winningNumbersList = fetchWinningNumbers();
        for (LottoDTO dto : winningNumbersList) {
            Lotto lotto = new Lotto();
            lotto.setNumber1(dto.getNumber1());
            lotto.setNumber2(dto.getNumber2());
            lotto.setNumber3(dto.getNumber3());
            lotto.setNumber4(dto.getNumber4());
            lotto.setNumber5(dto.getNumber5());
            lotto.setNumber6(dto.getNumber6());
            lottoRepository.save(lotto);
        }
    }


    // 이거는 로또 번호 1119회차까지 api를 제공해줌 이걸로 insert
    @Transactional
    public void pastnumbers() throws IOException {
        // 1회차부터 1119회차까지 앞으로 최신회차만 추가하는 로직으로 수정하면 될듯
        for (int drwNo = 1; drwNo <= 1119; drwNo++) {
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
