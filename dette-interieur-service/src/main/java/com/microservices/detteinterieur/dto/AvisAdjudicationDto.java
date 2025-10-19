package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.AvisAdjudication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisAdjudicationDto {
    
    private Long id;
    private Long adjudicationId;
    private AvisAdjudication.TypeAvis typeAvis;
    private String numeroAvis;
    private LocalDate dateReception;
    private BigDecimal montant;
    private String emetteur;
    private AvisAdjudication.StatutAvis statut;
    private String commentaire;
    private LocalDateTime createdAt;
    
    public static AvisAdjudicationDto fromEntity(AvisAdjudication avisAdjudication) {
        return AvisAdjudicationDto.builder()
                .id(avisAdjudication.getId())
                .adjudicationId(avisAdjudication.getAdjudication() != null ? avisAdjudication.getAdjudication().getId() : null)
                .typeAvis(avisAdjudication.getTypeAvis())
                .numeroAvis(avisAdjudication.getNumeroAvis())
                .dateReception(avisAdjudication.getDateReception())
                .montant(avisAdjudication.getMontant())
                .emetteur(avisAdjudication.getEmetteur())
                .statut(avisAdjudication.getStatut())
                .commentaire(avisAdjudication.getCommentaire())
                .createdAt(avisAdjudication.getCreatedAt())
                .build();
    }
}