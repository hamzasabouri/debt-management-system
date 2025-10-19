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
public class FinancialSummary {
    private BigDecimal totalAvailableFunds;
    private BigDecimal totalAllocatedFunds;
    private BigDecimal totalSpentFunds;
    private BigDecimal totalRemainingFunds;
    private Double utilizationRate;
    private List<ProjectFinancialSummary> projectSummaries;
}