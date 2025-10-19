package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.InteretDepot;
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
public class InteretDepotDto {
    
    private Long id;
    private InteretDepot.TypeFonds typeFonds;
    private String numeroCompte;
    private BigDecimal montant;
    private String devise;
    private LocalDate dateCalcul;
    private Long avisCreditId;
    private InteretDepot.StatutInteret statut;
    private BigDecimal tauxInteret;
    private BigDecimal montantPrincipal;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private String nomTitulaire;
    private String organisme;
    private String commentaire;
    private LocalDateTime createdAt;
    private LocalDateTime dateTraitement;
    
    public static InteretDepotDto fromEntity(InteretDepot interetDepot) {
        return InteretDepotDto.builder()
                .id(interetDepot.getId())
                .typeFonds(interetDepot.getTypeFonds())
                .numeroCompte(interetDepot.getNumeroCompte())
                .montant(interetDepot.getMontant())
                .devise(interetDepot.getDevise())
                .dateCalcul(interetDepot.getDateCalcul())
                .avisCreditId(interetDepot.getAvisCreditId())
                .statut(interetDepot.getStatut())
                .tauxInteret(interetDepot.getTauxInteret())
                .montantPrincipal(interetDepot.getMontantPrincipal())
                .periodeDebut(interetDepot.getPeriodeDebut())
                .periodeFin(interetDepot.getPeriodeFin())
                .nomTitulaire(interetDepot.getNomTitulaire())
                .organisme(interetDepot.getOrganisme())
                .commentaire(interetDepot.getCommentaire())
                .createdAt(interetDepot.getCreatedAt())
                .dateTraitement(interetDepot.getDateTraitement())
                .build();
    }
    
    public InteretDepot toEntity() {
        InteretDepot interetDepot = new InteretDepot();
        interetDepot.setId(this.id);
        interetDepot.setTypeFonds(this.typeFonds);
        interetDepot.setNumeroCompte(this.numeroCompte);
        interetDepot.setMontant(this.montant);
        interetDepot.setDevise(this.devise);
        interetDepot.setDateCalcul(this.dateCalcul);
        interetDepot.setAvisCreditId(this.avisCreditId);
        interetDepot.setStatut(this.statut);
        interetDepot.setTauxInteret(this.tauxInteret);
        interetDepot.setMontantPrincipal(this.montantPrincipal);
        interetDepot.setPeriodeDebut(this.periodeDebut);
        interetDepot.setPeriodeFin(this.periodeFin);
        interetDepot.setNomTitulaire(this.nomTitulaire);
        interetDepot.setOrganisme(this.organisme);
        interetDepot.setCommentaire(this.commentaire);
        interetDepot.setCreatedAt(this.createdAt);
        interetDepot.setDateTraitement(this.dateTraitement);
        return interetDepot;
    }
}