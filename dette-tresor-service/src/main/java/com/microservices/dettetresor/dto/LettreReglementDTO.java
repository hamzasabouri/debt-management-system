package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.LettreReglement;
import jakarta.validation.constraints.*;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LettreReglementDTO {
    
    private Long id;
    
    @NotNull(message = "Loan ID is required")
    private Long pretId;
    
    // Changed to not required since it can be null in some cases
    private Long ordrePaiementId;
    
    @NotBlank(message = "Letter number is required")
    @Size(max = 50, message = "Letter number cannot exceed 50 characters")
    private String numeroLettre;
    
    @NotNull(message = "Transmission date is required")
    private LocalDate dateTransmission;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Treasury account is required")
    @Size(max = 50, message = "Treasury account cannot exceed 50 characters")
    private String compteTresor;
    
    @Size(max = 500, message = "PDF file path cannot exceed 500 characters")
    private String cheminFichierPdf;
    
    private LocalDateTime createdAt;
    
    // For detailed view - marked as JsonIgnore to prevent circular reference
    private OrdrePaiementDTO ordrePaiement;
    
    // Getter methods
    public Long getId() {
        return id;
    }
    
    public Long getPretId() {
        return pretId;
    }
    
    public Long getOrdrePaiementId() {
        return ordrePaiementId;
    }
    
    public String getNumeroLettre() {
        return numeroLettre;
    }
    
    public LocalDate getDateTransmission() {
        return dateTransmission;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public String getDevise() {
        return devise;
    }
    
    public String getCompteTresor() {
        return compteTresor;
    }
    
    public String getCheminFichierPdf() {
        return cheminFichierPdf;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public OrdrePaiementDTO getOrdrePaiement() {
        return ordrePaiement;
    }
    
    // Setter methods
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setPretId(Long pretId) {
        this.pretId = pretId;
    }
    
    public void setOrdrePaiementId(Long ordrePaiementId) {
        this.ordrePaiementId = ordrePaiementId;
    }
    
    public void setNumeroLettre(String numeroLettre) {
        this.numeroLettre = numeroLettre;
    }
    
    public void setDateTransmission(LocalDate dateTransmission) {
        this.dateTransmission = dateTransmission;
    }
    
    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
    public void setDevise(String devise) {
        this.devise = devise;
    }
    
    public void setCompteTresor(String compteTresor) {
        this.compteTresor = compteTresor;
    }
    
    public void setCheminFichierPdf(String cheminFichierPdf) {
        this.cheminFichierPdf = cheminFichierPdf;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setOrdrePaiement(OrdrePaiementDTO ordrePaiement) {
        this.ordrePaiement = ordrePaiement;
    }
    
    // Helper method to convert from entity
    public static LettreReglementDTO fromEntity(LettreReglement lettreReglement) {
        return LettreReglementDTO.builder()
                .id(lettreReglement.getId())
                .pretId(lettreReglement.getPret().getId())
                .ordrePaiementId(lettreReglement.getOrdrePaiement() != null ? 
                    lettreReglement.getOrdrePaiement().getId() : null)
                .numeroLettre(lettreReglement.getNumeroLettre())
                .dateTransmission(lettreReglement.getDateTransmission())
                .montant(lettreReglement.getMontant())
                .devise(lettreReglement.getDevise())
                .compteTresor(lettreReglement.getCompteTresor())
                .cheminFichierPdf(lettreReglement.getCheminFichierPdf())
                .createdAt(lettreReglement.getCreatedAt())
                .build();
    }
}