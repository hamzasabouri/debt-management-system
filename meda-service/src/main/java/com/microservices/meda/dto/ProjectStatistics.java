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
public class ProjectStatistics {
    private Long totalProjects;
    private Long activeProjects;
    private Long completedProjects;
    private BigDecimal totalProjectsValue;
    private BigDecimal averageProjectValue;
    private List<CurrencyStatistic> byCurrency;
}