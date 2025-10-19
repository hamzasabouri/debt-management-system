package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.Commission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionDto {
    
    private Long id;
    private Commission.TypeCommission typeCommission;
    private Long ordrePaiementId;
    private Long lettreReglementId;
    private Long avisDebitId;
    private BigDecimal montant;
    private Commission.StatutCommission statut;
    private String numeroReference;
    private String description;
    private String commentaire;
    private LocalDateTime createdAt;
    private LocalDateTime datePaiement;
    
    public static CommissionDto fromEntity(Commission commission) {
        return CommissionDto.builder()
                .id(commission.getId())
                .typeCommission(commission.getTypeCommission())
                .ordrePaiementId(commission.getOrdrePaiementId())
                .lettreReglementId(commission.getLettreReglementId())
                .avisDebitId(commission.getAvisDebitId())
                .montant(commission.getMontant())
                .statut(commission.getStatut())
                .numeroReference(commission.getNumeroReference())
                .description(commission.getDescription())
                .commentaire(commission.getCommentaire())
                .createdAt(commission.getCreatedAt())
                .datePaiement(commission.getDatePaiement())
                .build();
    }
}