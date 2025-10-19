package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.OrdrePaiement.StatutOrdrePaiement;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdrePaiementDTO {
    
    private Long id;
    
    @NotNull(message = "Loan ID is required")
    private Long pretId;
    
    private Long lettreReglementId;
    
    @NotBlank(message = "Payment order number is required")
    @Size(max = 50, message = "Payment order number cannot exceed 50 characters")
    private String numeroOrdre;
    private LocalDate dateEmission;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotNull(message = "Due date is required")
    private LocalDate echeance;
    
    @NotNull(message = "Status is required")
    @Builder.Default
    private StatutOrdrePaiement statut = StatutOrdrePaiement.EN_ATTENTE;
    
    private LocalDateTime createdAt;
    
    // For detailed view - marked as JsonIgnore to prevent circular reference
    @JsonIgnore
    private PretDTO pret;
    
    @JsonBackReference
    private LettreReglementDTO lettreReglement;
    
    // Changed from single AvisDebitDTO to List<AvisDebitDTO>
    @JsonIgnore
    private List<AvisDebitDTO> avisDebits;
    
    // Getter methods
    public Long getId() {
        return id;
    }
    
    public Long getPretId() {
        return pretId;
    }
    
    public Long getLettreReglementId() {
        return lettreReglementId;
    }
    
    public String getNumeroOrdre() {
        return numeroOrdre;
    }
    
    public LocalDate getDateEmission() {
        return dateEmission;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public String getDevise() {
        return devise;
    }
    
    public LocalDate getEcheance() {
        return echeance;
    }
    
    public StatutOrdrePaiement getStatut() {
        return statut;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public PretDTO getPret() {
        return pret;
    }
    
    public LettreReglementDTO getLettreReglement() {
        return lettreReglement;
    }
    
    // Updated getter for avisDebits
    public List<AvisDebitDTO> getAvisDebits() {
        return avisDebits;
    }
    
    // Setter methods
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setPretId(Long pretId) {
        this.pretId = pretId;
    }
    
    public void setLettreReglementId(Long lettreReglementId) {
        this.lettreReglementId = lettreReglementId;
    }
    
    public void setNumeroOrdre(String numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }
    
    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }
    
    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
    public void setDevise(String devise) {
        this.devise = devise;
    }
    
    public void setEcheance(LocalDate echeance) {
        this.echeance = echeance;
    }
    
    public void setStatut(StatutOrdrePaiement statut) {
        this.statut = statut;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setPret(PretDTO pret) {
        this.pret = pret;
    }
    
    public void setLettreReglement(LettreReglementDTO lettreReglement) {
        this.lettreReglement = lettreReglement;
    }
    
    // Updated setter for avisDebits
    public void setAvisDebits(List<AvisDebitDTO> avisDebits) {
        this.avisDebits = avisDebits;
    }
    
    // Helper method to convert from entity
    public static OrdrePaiementDTO fromEntity(OrdrePaiement ordrePaiement) {
        return OrdrePaiementDTO.builder()
                .id(ordrePaiement.getId())
                .pretId(ordrePaiement.getPret().getId())
                .lettreReglementId(ordrePaiement.getLettreReglement() != null ? 
                    ordrePaiement.getLettreReglement().getId() : null)
                .numeroOrdre(ordrePaiement.getNumeroOrdre())
                .dateEmission(ordrePaiement.getDateEmission())
                .montant(ordrePaiement.getMontant())
                .devise(ordrePaiement.getDevise())
                .echeance(ordrePaiement.getEcheance())
                .statut(ordrePaiement.getStatut())
                .createdAt(ordrePaiement.getCreatedAt())
                .build();
    }
}