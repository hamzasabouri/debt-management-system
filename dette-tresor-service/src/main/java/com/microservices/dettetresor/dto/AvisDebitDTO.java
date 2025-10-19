package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.AvisDebit;
import com.microservices.dettetresor.entity.AvisDebit.MotifAvisDebit;
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
public class AvisDebitDTO {
    
    private Long id;
    
    @NotNull(message = "Loan ID is required")
    private Long pretId;
    
    private Long ordrePaiementId; // Nullable for special cases
    
    @NotBlank(message = "Debit advice number is required")
    @Size(max = 50, message = "Debit advice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotNull(message = "Reason is required")
    private MotifAvisDebit motif;
    
    private LocalDateTime createdAt;
    
    // For detailed view - marked as JsonIgnore to prevent circular reference
    @JsonIgnore
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
    
    public String getNumeroAvis() {
        return numeroAvis;
    }
    
    public LocalDate getDateReception() {
        return dateReception;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public String getDevise() {
        return devise;
    }
    
    public MotifAvisDebit getMotif() {
        return motif;
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
    
    public void setNumeroAvis(String numeroAvis) {
        this.numeroAvis = numeroAvis;
    }
    
    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }
    
    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
    public void setDevise(String devise) {
        this.devise = devise;
    }
    
    public void setMotif(MotifAvisDebit motif) {
        this.motif = motif;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setOrdrePaiement(OrdrePaiementDTO ordrePaiement) {
        this.ordrePaiement = ordrePaiement;
    }
    
    // Helper method to convert from entity
    public static AvisDebitDTO fromEntity(AvisDebit avisDebit) {
        return AvisDebitDTO.builder()
                .id(avisDebit.getId())
                .ordrePaiementId(avisDebit.getOrdrePaiement() != null ? 
                    avisDebit.getOrdrePaiement().getId() : null)
                .numeroAvis(avisDebit.getNumeroAvis())
                .dateReception(avisDebit.getDateReception())
                .montant(avisDebit.getMontant())
                .devise(avisDebit.getDevise())
                .motif(avisDebit.getMotif())
                .createdAt(avisDebit.getCreatedAt())
                .build();
    }
}