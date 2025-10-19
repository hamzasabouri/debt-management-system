package com.microservices.detteinterieur.dto;

import com.microservices.detteinterieur.entity.AvisBonEquipement;
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
public class AvisBonEquipementDto {
    
    private Long id;
    private Long bonEquipementId;
    private AvisBonEquipement.TypeAvis typeAvis;
    private String numeroAvis;
    private LocalDate dateReception;
    private BigDecimal montant;
    private String emetteur;
    private AvisBonEquipement.StatutAvis statut;
    private String motifRejet;
    private String commentaire;
    private LocalDateTime createdAt;
    private LocalDateTime dateTraitement;
    
    // Explicit getters for Lombok
    public Long getId() {
        return id;
    }
    
    public Long getBonEquipementId() {
        return bonEquipementId;
    }
    
    public AvisBonEquipement.TypeAvis getTypeAvis() {
        return typeAvis;
    }
    
    public String getNumeroAvis() {
        return numeroAvis;
    }
    
    public LocalDate getDateReception() {
        return dateReception;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public String getEmetteur() {
        return emetteur;
    }
    
    public AvisBonEquipement.StatutAvis getStatut() {
        return statut;
    }
    
    public String getMotifRejet() {
        return motifRejet;
    }
    
    public String getCommentaire() {
        return commentaire;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }
    
    public static AvisBonEquipementDto fromEntity(AvisBonEquipement avisBonEquipement) {
        return AvisBonEquipementDto.builder()
                .id(avisBonEquipement.getId())
                .bonEquipementId(avisBonEquipement.getBonEquipement() != null ? avisBonEquipement.getBonEquipement().getId() : null)
                .typeAvis(avisBonEquipement.getTypeAvis())
                .numeroAvis(avisBonEquipement.getNumeroAvis())
                .dateReception(avisBonEquipement.getDateReception())
                .montant(avisBonEquipement.getMontant())
                .emetteur(avisBonEquipement.getEmetteur())
                .statut(avisBonEquipement.getStatut())
                .motifRejet(avisBonEquipement.getMotifRejet())
                .commentaire(avisBonEquipement.getCommentaire())
                .createdAt(avisBonEquipement.getCreatedAt())
                .dateTraitement(avisBonEquipement.getDateTraitement())
                .build();
    }
    
    // Explicit builder method
    public static AvisBonEquipementDtoBuilder builder() {
        return new AvisBonEquipementDtoBuilder();
    }
    
    public static class AvisBonEquipementDtoBuilder {
        private Long id;
        private Long bonEquipementId;
        private AvisBonEquipement.TypeAvis typeAvis;
        private String numeroAvis;
        private LocalDate dateReception;
        private BigDecimal montant;
        private String emetteur;
        private AvisBonEquipement.StatutAvis statut;
        private String motifRejet;
        private String commentaire;
        private LocalDateTime createdAt;
        private LocalDateTime dateTraitement;
        
        public AvisBonEquipementDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder bonEquipementId(Long bonEquipementId) {
            this.bonEquipementId = bonEquipementId;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder typeAvis(AvisBonEquipement.TypeAvis typeAvis) {
            this.typeAvis = typeAvis;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder numeroAvis(String numeroAvis) {
            this.numeroAvis = numeroAvis;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder dateReception(LocalDate dateReception) {
            this.dateReception = dateReception;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder montant(BigDecimal montant) {
            this.montant = montant;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder emetteur(String emetteur) {
            this.emetteur = emetteur;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder statut(AvisBonEquipement.StatutAvis statut) {
            this.statut = statut;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder motifRejet(String motifRejet) {
            this.motifRejet = motifRejet;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder commentaire(String commentaire) {
            this.commentaire = commentaire;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public AvisBonEquipementDtoBuilder dateTraitement(LocalDateTime dateTraitement) {
            this.dateTraitement = dateTraitement;
            return this;
        }
        
        public AvisBonEquipementDto build() {
            return new AvisBonEquipementDto(id, bonEquipementId, typeAvis, numeroAvis, dateReception, montant, emetteur, statut, motifRejet, commentaire, createdAt, dateTraitement);
        }
    }
}