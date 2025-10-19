package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.BonEquipement;
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
public class BonEquipementDto {
    
    private Long id;
    private String numeroBon;
    private LocalDate dateSouscription;
    private BigDecimal montant;
    private BonEquipement.StatutBon statut;
    private LocalDate dateEcheance;
    private BigDecimal tauxInteret;
    private String souscripteur;
    private String commentaire;
    private LocalDateTime createdAt;
    private LocalDate dateRemboursement;
    private List<AvisBonEquipementDto> avisBons;
    
    public static BonEquipementDto fromEntity(BonEquipement bonEquipement) {
        return BonEquipementDto.builder()
                .id(bonEquipement.getId())
                .numeroBon(bonEquipement.getNumeroBon())
                .dateSouscription(bonEquipement.getDateSouscription())
                .montant(bonEquipement.getMontant())
                .statut(bonEquipement.getStatut())
                .dateEcheance(bonEquipement.getDateEcheance())
                .tauxInteret(bonEquipement.getTauxInteret())
                .souscripteur(bonEquipement.getSouscripteur())
                .commentaire(bonEquipement.getCommentaire())
                .createdAt(bonEquipement.getCreatedAt())
                .dateRemboursement(bonEquipement.getDateRemboursement())
                .avisBons(bonEquipement.getAvisBons().stream()
                        .map(AvisBonEquipementDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}