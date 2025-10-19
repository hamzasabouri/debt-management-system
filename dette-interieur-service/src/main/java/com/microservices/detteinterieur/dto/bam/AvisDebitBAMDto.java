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
 * DTO for BAM Debit Notice communication
 * Used for receiving debit notices from Bank Al-Maghrib
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisDebitBAMDto {
    
    @JsonProperty("numero_avis")
    @NotBlank(message = "Debit notice number is required")
    @Size(max = 50, message = "Notice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @JsonProperty("type_commission")
    @NotBlank(message = "Commission type is required")
    private String typeCommission; // MAROCLEAR, BAM
    
    @JsonProperty("reference_commission")
    @NotBlank(message = "Commission reference is required")
    private String referenceCommission;
    
    @JsonProperty("date_commission")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Commission date is required")
    private LocalDate dateCommission;
    
    @JsonProperty("montant_commission")
    @NotNull(message = "Commission amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Commission amount must be positive")
    private BigDecimal montantCommission;
    
    @JsonProperty("taux_commission")
    @DecimalMin(value = "0.0", message = "Commission rate cannot be negative")
    private BigDecimal tauxCommission;
    
    @JsonProperty("devise")
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    private String devise;
    
    @JsonProperty("compte_debiteur")
    @NotBlank(message = "Debtor account is required")
    private String compteDebiteur;
    
    @JsonProperty("nom_debiteur")
    @NotBlank(message = "Debtor name is required")
    @Size(max = 150, message = "Debtor name cannot exceed 150 characters")
    private String nomDebiteur;
    
    @JsonProperty("motif_debit")
    @NotBlank(message = "Debit reason is required")
    @Size(max = 500, message = "Debit reason cannot exceed 500 characters")
    private String motifDebit;
    
    @JsonProperty("date_valeur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Value date is required")
    private LocalDate dateValeur;
    
    @JsonProperty("numero_ordre_paiement")
    private String numeroOrdrePaiement;
    
    @JsonProperty("numero_lettre_reglement")
    private String numeroLettreReglement;
    
    @JsonProperty("statut_traitement")
    private String statutTraitement; // RECU, TRAITE, REJETE
    
    @JsonProperty("code_erreur")
    private String codeErreur;
    
    @JsonProperty("message_erreur")
    private String messageErreur;
    
    @JsonProperty("date_reception")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateReception;
    
    @JsonProperty("date_traitement")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTraitement;
    
    // Helper methods for validation and status
    public boolean isValid() {
        return numeroAvis != null && !numeroAvis.trim().isEmpty() &&
               typeCommission != null && !typeCommission.trim().isEmpty() &&
               referenceCommission != null && !referenceCommission.trim().isEmpty() &&
               montantCommission != null && montantCommission.compareTo(BigDecimal.ZERO) > 0 &&
               devise != null && !devise.trim().isEmpty() &&
               compteDebiteur != null && !compteDebiteur.trim().isEmpty() &&
               nomDebiteur != null && !nomDebiteur.trim().isEmpty();
    }
    
    public boolean isRecu() {
        return "RECU".equals(statutTraitement);
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
    
    public boolean isMaroclearCommission() {
        return "MAROCLEAR".equals(typeCommission);
    }
    
    public boolean isBAMCommission() {
        return "BAM".equals(typeCommission);
    }
}