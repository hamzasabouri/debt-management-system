package com.microservices.meda.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJustificativeComptabiliteDTO {
    
    // DTO for Comptabilite integration - receiving supporting documents
    @NotBlank(message = "Document number is required")
    private String numeroPiece;
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotNull(message = "Document type is required")
    @Pattern(regexp = "FACTURE|BON_PAIEMENT|RECU|AUTRE", message = "Document type must be FACTURE, BON_PAIEMENT, RECU, or AUTRE")
    private String typePiece;
    
    @NotNull(message = "Execution date is required")
    private LocalDate dateExecution;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    private String devise;
    
    @NotBlank(message = "Issuer (accountant) is required")
    private String emetteur;
    
    // Comptabilite specific fields
    private String referenceComptable;
    private String compteDebite;
    private String centreBeneficiaire;
}