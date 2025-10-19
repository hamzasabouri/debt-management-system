package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.Echeancier;
import com.microservices.dettetresor.entity.Echeancier.StatutEcheance;
import com.microservices.dettetresor.validation.CreateValidationGroup;
import com.microservices.dettetresor.validation.UpdateValidationGroup;
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
public class EcheancierDTO {
    
    private Long id;
    
    @NotNull(message = "Loan ID is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private Long pretId;
    
    @NotNull(message = "Payment number is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @Min(value = 1, message = "Payment number must be at least 1", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private Integer numeroEcheance;
    
    @NotNull(message = "Due date is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private LocalDate dateEcheance;
    
    @NotNull(message = "Principal amount is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @DecimalMin(value = "0.0", message = "Principal cannot be negative", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private BigDecimal capital;
    
    @NotNull(message = "Interest amount is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @DecimalMin(value = "0.0", message = "Interest cannot be negative", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private BigDecimal interet;
    
    @DecimalMin(value = "0.0", message = "Commission cannot be negative", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;
    
    @NotNull(message = "Total amount is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private BigDecimal montantTotal;
    
    @NotNull(message = "Status is required", groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    @Builder.Default
    private StatutEcheance statut = StatutEcheance.PREVU;
    
    private Long ordrePaiementId;
    private LocalDateTime createdAt;
    
    // For detailed view - marked as JsonIgnore to prevent circular reference
    @JsonIgnore
    private PretDTO pret;
    
    @JsonIgnore
    private OrdrePaiementDTO ordrePaiement;
    
    // Getter methods
    public Long getId() {
        return id;
    }
    
    public Long getPretId() {
        return pretId;
    }
    
    public Integer getNumeroEcheance() {
        return numeroEcheance;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public BigDecimal getCapital() {
        return capital;
    }
    
    public BigDecimal getInteret() {
        return interet;
    }
    
    public BigDecimal getCommission() {
        return commission;
    }
    
    public BigDecimal getMontantTotal() {
        return montantTotal;
    }
    
    public StatutEcheance getStatut() {
        return statut;
    }
    
    public Long getOrdrePaiementId() {
        return ordrePaiementId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public PretDTO getPret() {
        return pret;
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
    
    public void setNumeroEcheance(Integer numeroEcheance) {
        this.numeroEcheance = numeroEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }
    
    public void setInteret(BigDecimal interet) {
        this.interet = interet;
    }
    
    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
    
    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
    
    public void setStatut(StatutEcheance statut) {
        this.statut = statut;
    }
    
    public void setOrdrePaiementId(Long ordrePaiementId) {
        this.ordrePaiementId = ordrePaiementId;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setPret(PretDTO pret) {
        this.pret = pret;
    }
    
    public void setOrdrePaiement(OrdrePaiementDTO ordrePaiement) {
        this.ordrePaiement = ordrePaiement;
    }
    
    // Helper method to convert from entity
    public static EcheancierDTO fromEntity(Echeancier echeancier) {
        return EcheancierDTO.builder()
                .id(echeancier.getId())
                .pretId(echeancier.getPret().getId())
                .numeroEcheance(echeancier.getNumeroEcheance())
                .dateEcheance(echeancier.getDateEcheance())
                .capital(echeancier.getCapital())
                .interet(echeancier.getInteret())
                .commission(echeancier.getCommission())
                .montantTotal(echeancier.getMontantTotal())
                .statut(echeancier.getStatut())
                .ordrePaiementId(echeancier.getOrdrePaiement() != null ? 
                    echeancier.getOrdrePaiement().getId() : null)
                .createdAt(echeancier.getCreatedAt())
                .build();
    }
    
    // Helper method to convert from entity with Pret (for detailed view)
    public static EcheancierDTO fromEntityWithPret(Echeancier echeancier) {
        EcheancierDTO dto = EcheancierDTO.fromEntity(echeancier);
        // Only populate pretId, not the full PretDTO to avoid circular reference
        return dto;
    }
}