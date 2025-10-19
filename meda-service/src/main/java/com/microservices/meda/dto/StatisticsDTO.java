package com.microservices.meda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsDTO {
    private ProjectStatistics projects;
    private AdvanceStatistics advances;
    private ExpenseStatistics expenses;
    private FinancialSummary financialSummary;
}
