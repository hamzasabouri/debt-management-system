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
public class ProjectFinancialSummary {
    private Long projetId;
    private String nomProjet;
    private BigDecimal montantTotal;
    private BigDecimal totalAvances;
    private BigDecimal totalDepenses;
    private BigDecimal soldeDisponible;
    private Double pourcentageUtilisation;
    private String statut;
}
