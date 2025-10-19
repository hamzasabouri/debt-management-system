package com.microservices.meda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeStatistic {
    private String type;
    private Long count;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
}