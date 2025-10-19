package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.AvisCredit;
import com.microservices.dettetresor.entity.AvisCredit.TypeAvisCredit;
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
public class AvisCreditDTO {
    
    private Long id;
    
    @NotNull(message = "Loan ID is required")
    private Long pretId;
    
    @NotBlank(message = "Credit advice number is required")
    @Size(max = 50, message = "Credit advice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Credit advice type is required")
    private TypeAvisCredit typeAvis;
    
    private LocalDateTime createdAt;
    
    // For detailed view - marked as JsonIgnore to prevent circular reference
    @JsonIgnore
    private PretDTO pret;
    
    // Getter methods
    public Long getId() {
        return id;
    }
    
    public Long getPretId() {
        return pretId;
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
    
    public String getEmetteur() {
        return emetteur;
    }
    
    public TypeAvisCredit getTypeAvis() {
        return typeAvis;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public PretDTO getPret() {
        return pret;
    }
    
    // Setter methods
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setPretId(Long pretId) {
        this.pretId = pretId;
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
    
    public void setEmetteur(String emetteur) {
        this.emetteur = emetteur;
    }
    
    public void setTypeAvis(TypeAvisCredit typeAvis) {
        this.typeAvis = typeAvis;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setPret(PretDTO pret) {
        this.pret = pret;
    }
    
    // Helper method to convert from entity
    public static AvisCreditDTO fromEntity(AvisCredit avisCredit) {
        return AvisCreditDTO.builder()
                .id(avisCredit.getId())
                .pretId(avisCredit.getPret().getId())
                .numeroAvis(avisCredit.getNumeroAvis())
                .dateReception(avisCredit.getDateReception())
                .montant(avisCredit.getMontant())
                .devise(avisCredit.getDevise())
                .emetteur(avisCredit.getEmetteur())
                .typeAvis(avisCredit.getTypeAvis())
                .createdAt(avisCredit.getCreatedAt())
                .build();
    }
}