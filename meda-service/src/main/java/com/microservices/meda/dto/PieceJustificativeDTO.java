package com.microservices.meda.dto;

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
public class PieceJustificativeDTO {
    
    private Long id;
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    // Project information for detailed views
    private String nomProjet;
    
    @NotBlank(message = "Document number is required")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String numeroPiece;
    
    @NotNull(message = "Document type is required")
    private String typePiece; // FACTURE, BON_PAIEMENT, RECU, AUTRE
    
    @NotNull(message = "Execution date is required")
    private LocalDate dateExecution;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer (accountant) is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    private String statut; // PRIS_EN_CHARGE, VALIDE, REJETE
    
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    private LocalDateTime createdAt;
    
    // Derived fields
    private String typePieceDescription;
    private String statutDescription;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PieceJustificativeCreateDTO {
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotBlank(message = "Document number is required")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String numeroPiece;
    
    @NotNull(message = "Document type is required")
    @Pattern(regexp = "FACTURE|BON_PAIEMENT|RECU|AUTRE", message = "Document type must be FACTURE, BON_PAIEMENT, RECU, or AUTRE")
    private String typePiece;
    
    @NotNull(message = "Execution date is required")
    private LocalDate dateExecution;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer (accountant) is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "PRIS_EN_CHARGE|VALIDE|REJETE", message = "Status must be PRIS_EN_CHARGE, VALIDE, or REJETE")
    private String statut;
    
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PieceJustificativeUpdateDTO {
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotBlank(message = "Document number is required")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String numeroPiece;
    
    @NotNull(message = "Document type is required")
    @Pattern(regexp = "FACTURE|BON_PAIEMENT|RECU|AUTRE", message = "Document type must be FACTURE, BON_PAIEMENT, RECU, or AUTRE")
    private String typePiece;
    
    @NotNull(message = "Execution date is required")
    private LocalDate dateExecution;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer (accountant) is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "PRIS_EN_CHARGE|VALIDE|REJETE", message = "Status must be PRIS_EN_CHARGE, VALIDE, or REJETE")
    private String statut;
    
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PieceJustificativeValidationDTO {
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "VALIDE|REJETE", message = "Status must be VALIDE or REJETE")
    private String statut;
    
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    private String validatedBy; // User who validated/rejected
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PieceJustificativeSummaryDTO {
    
    private Long id;
    private Long projetId;
    private String nomProjet;
    private String numeroPiece;
    private String typePiece;
    private LocalDate dateExecution;
    private BigDecimal montant;
    private String devise;
    private String emetteur;
    private String statut;
    private String typePieceDescription;
    private String statutDescription;
    private boolean hasComment;
}

