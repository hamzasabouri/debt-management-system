package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.Adjudication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjudicationDto {
    
    private Long id;
    private String numeroAdjud;
    private LocalDate dateAdjud;
    private BigDecimal montantTotal;
    private Adjudication.StatutAdjudication statut;
    private LocalDateTime createdAt;
    private List<AvisAdjudicationDto> avisAdjudications;
    
    public static AdjudicationDto fromEntity(Adjudication adjudication) {
        return AdjudicationDto.builder()
                .id(adjudication.getId())
                .numeroAdjud(adjudication.getNumeroAdjud())
                .dateAdjud(adjudication.getDateAdjud())
                .montantTotal(adjudication.getMontantTotal())
                .statut(adjudication.getStatut())
                .createdAt(adjudication.getCreatedAt())
                .avisAdjudications(adjudication.getAvisAdjudications().stream()
                        .map(AvisAdjudicationDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}