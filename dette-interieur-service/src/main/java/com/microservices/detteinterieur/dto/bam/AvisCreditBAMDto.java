package com.microservices.detteinterieur.dto.bam;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for BAM Credit Notice communication
 * Used for sending credit notices to Bank Al-Maghrib
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisCreditBAMDto {
    
    @JsonProperty("numero_avis")
    @NotBlank(message = "Credit notice number is required")
    @Size(max = 50, message = "Notice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @JsonProperty("type_operation")
    @NotBlank(message = "Operation type is required")
    private String typeOperation; // ADJUDICATION, COMMISSION, BON_EQUIPEMENT, INTERET_DEPOT
    
    @JsonProperty("reference_operation")
    @NotBlank(message = "Operation reference is required")
    private String referenceOperation;
    
    @JsonProperty("date_operation")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Operation date is required")
    private LocalDate dateOperation;
    
    @JsonProperty("montant")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @JsonProperty("devise")
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    private String devise;
    
    @JsonProperty("compte_beneficiaire")
    @NotBlank(message = "Beneficiary account is required")
    private String compteBeneficiaire;
    
    @JsonProperty("nom_beneficiaire")
    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 150, message = "Beneficiary name cannot exceed 150 characters")
    private String nomBeneficiaire;
    
    @JsonProperty("motif_credit")
    @NotBlank(message = "Credit reason is required")
    @Size(max = 500, message = "Credit reason cannot exceed 500 characters")
    private String motifCredit;
    
    @JsonProperty("date_valeur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Value date is required")
    private LocalDate dateValeur;
    
    @JsonProperty("statut_traitement")
    private String statutTraitement; // EN_ATTENTE, TRAITE, REJETE
    
    @JsonProperty("code_erreur")
    private String codeErreur;
    
    @JsonProperty("message_erreur")
    private String messageErreur;
    
    @JsonProperty("date_creation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonProperty("date_traitement")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTraitement;
    
    // Helper methods for validation
    public boolean isValid() {
        return numeroAvis != null && !numeroAvis.trim().isEmpty() &&
               typeOperation != null && !typeOperation.trim().isEmpty() &&
               referenceOperation != null && !referenceOperation.trim().isEmpty() &&
               montant != null && montant.compareTo(BigDecimal.ZERO) > 0 &&
               devise != null && !devise.trim().isEmpty() &&
               compteBeneficiaire != null && !compteBeneficiaire.trim().isEmpty() &&
               nomBeneficiaire != null && !nomBeneficiaire.trim().isEmpty();
    }
    
    public boolean isTraite() {
        return "TRAITE".equals(statutTraitement);
    }
    
    public boolean isRejete() {
        return "REJETE".equals(statutTraitement);
    }
    
    public boolean hasError() {
        return codeErreur != null && !codeErreur.trim().isEmpty();
    }
}