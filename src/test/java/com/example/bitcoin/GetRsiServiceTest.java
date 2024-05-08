package com.example.bitcoin;

import com.example.bitcoin.service.GetRsiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertFalse;

//@Component
@SpringBootTest
@Slf4j
public class GetRsiServiceTest {

    @Autowired
    GetRsiService getRsiService;

    @Test
    public void testGetRsi() throws IOException, ParseException {
        // 테스트할 메서드를 호출하고 예상되는 결과를 얻습니다.
        double rsi = getRsiService.getRsi("2024-05-08 12:00:00", "minutes/60", "KRW-ZETA");

        double rsi1 = getRsiService.getRsi("2024-05-08 12:00:00", "days", "KRW-ZETA");

        double rsi2 = getRsiService.getRsi("2024-05-08 12:00:00", "weeks", "KRW-ZETA");

        double rsi3 = getRsiService.getRsi("2024-05-08 12:00:00", "months", "KRW-ZETA");
        log.info("rsi: " + rsi);
        log.info("rsi: " + rsi1);
        log.info("rsi: " + rsi2);
        log.info("rsi: " + rsi3);

        // 예상되는 결과와 실제 결과를 비교하여 테스트를 수행합니다.
        // 예를 들어, 계산된 RSI 값이 NaN이 아닌지 확인합니다.
//        assertFalse(Double.isNaN(rsi));
    }
}

