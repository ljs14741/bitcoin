package com.example.bitcoin.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RsiSummaryDTO {
    private String market;
    private double rsi60;
    private double rsiDaily;
    private double rsiWeekly;
    private double rsiMonthly;

}
