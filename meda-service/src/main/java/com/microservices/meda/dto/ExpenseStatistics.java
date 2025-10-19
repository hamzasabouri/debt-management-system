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
public class ExpenseStatistics {
    private Long totalDocuments;
    private BigDecimal totalValidatedExpenses;
    private BigDecimal averageExpenseAmount;
    private Long documentsPrisEnCharge;
    private Long documentsValides;
    private Long documentsRejetes;
    private List<TypeStatistic> byType;
    private List<CurrencyStatistic> byCurrency;
    private List<MonthlyStatistic> monthlyTrend;
}