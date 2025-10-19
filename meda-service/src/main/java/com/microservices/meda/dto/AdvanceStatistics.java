package com.microservices.meda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvanceStatistics {
    private Long totalAdvances;
    private BigDecimal totalAdvancesAmount;
    private BigDecimal averageAdvanceAmount;
    private Long totalDons;
    private BigDecimal totalDonsAmount;
    private Long totalPrets;
    private BigDecimal totalPretsAmount;
    private Long advancesPrisEnCharge;
    private Long advancesComptabilises;
    private List<CurrencyStatistic> byCurrency;
    private List<MonthlyStatistic> monthlyTrend;
}