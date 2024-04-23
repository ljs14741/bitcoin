package com.example.bitcoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
@Slf4j
public class CandleDTO {

    @JsonProperty("market")
    private String market;

    @JsonProperty("candle_date_time_utc")
    private String candleDateTimeUtc;

    @JsonProperty("candle_date_time_kst")
    private String candleDateTimeKst;

    @JsonProperty("opening_price")
    private Double openingPrice;

    @JsonProperty("high_price")
    private Double highPrice;

    @JsonProperty("low_price")
    private Double lowPrice;

    @JsonProperty("trade_price")
    private Double tradePrice;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("candle_acc_trade_price")
    private Double candleAccTradePrice;

    @JsonProperty("candle_acc_trade_volume")
    private Double candleAccTradeVolume;

    @JsonProperty("unit")
    private Integer unit;

    @JsonProperty("rsi_value")
    private Double rsiValue;

    @JsonProperty("range")
    private Double range;

    @JsonProperty("target")
    private Double target;

    @JsonProperty("earnings")
    private Double earnings;

    @JsonProperty("buy_day")
    private String buyDay;

    public interface CandleProjection {
        String getMarket();
        String getCandleDateTimeUtc();
        String getCandleDateTimeKst();
        Double getOpeningPrice();
        Double getHighPrice();
        Double getLowPrice();
        Double getTradePrice();
        Long getTimestamp();
        Double getCandleAccTradePrice();
        Double getCandleAccTradeVolume();
        Integer getUnit();
        Double getRsi();
        Double getRange();
        Double getTarget();
        Double getEarnings();



    }

}
