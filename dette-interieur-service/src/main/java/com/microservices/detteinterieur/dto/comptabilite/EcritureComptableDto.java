package com.microservices.detteinterieur.dto.comptabilite;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Comptabilite integration communication
 * Used for sending accounting entries and supporting documents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcritureComptableDto {
    
    @JsonProperty("numero_ecriture")
    @NotBlank(message = "Accounting entry number is required")
    @Size(max = 50, message = "Entry number cannot exceed 50 characters")
    private String numeroEcriture;
    
    @JsonProperty("type_operation")
    @NotBlank(message = "Operation type is required")
    private String typeOperation; // ADJUDICATION, COMMISSION, BON_EQUIPEMENT, INTERET_DEPOT
    
    @JsonProperty("reference_operation")
    @NotBlank(message = "Operation reference is required")
    private String referenceOperation;
    
    @JsonProperty("date_ecriture")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Entry date is required")
    private LocalDate dateEcriture;
    
    @JsonProperty("date_valeur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Value date is required")
    private LocalDate dateValeur;
    
    @JsonProperty("journal")
    @NotBlank(message = "Journal is required")
    @Size(max = 10, message = "Journal code cannot exceed 10 characters")
    private String journal;
    
    @JsonProperty("libelle_ecriture")
    @NotBlank(message = "Entry description is required")
    @Size(max = 255, message = "Entry description cannot exceed 255 characters")
    private String libelleEcriture;
    
    @JsonProperty("lignes_comptables")
    @NotEmpty(message = "Accounting lines are required")
    @Valid
    private List<LigneComptableDto> lignesComptables;
    
    @JsonProperty("pieces_justificatives")
    @Valid
    private List<PieceJustificativeDto> piecesJustificatives;
    
    @JsonProperty("montant_total")
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @JsonProperty("devise")
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    private String devise;
    
    @JsonProperty("statut_comptabilisation")
    private String statutComptabilisation; // EN_ATTENTE, COMPTABILISE, REJETE
    
    @JsonProperty("numero_lot")
    private String numeroLot;
    
    @JsonProperty("utilisateur_saisie")
    private String utilisateurSaisie;
    
    @JsonProperty("date_saisie")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateSaisie;
    
    @JsonProperty("date_comptabilisation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateComptabilisation;
    
    @JsonProperty("code_erreur")
    private String codeErreur;
    
    @JsonProperty("message_erreur")
    private String messageErreur;
    
    @JsonProperty("commentaire")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    // Nested DTO for accounting lines
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LigneComptableDto {
        
        @JsonProperty("numero_ligne")
        @NotNull(message = "Line number is required")
        @Min(value = 1, message = "Line number must be positive")
        private Integer numeroLigne;
        
        @JsonProperty("compte")
        @NotBlank(message = "Account is required")
        @Size(max = 20, message = "Account code cannot exceed 20 characters")
        private String compte;
        
        @JsonProperty("libelle_compte")
        @NotBlank(message = "Account description is required")
        @Size(max = 150, message = "Account description cannot exceed 150 characters")
        private String libelleCompte;
        
        @JsonProperty("montant_debit")
        @DecimalMin(value = "0.0", message = "Debit amount cannot be negative")
        private BigDecimal montantDebit;
        
        @JsonProperty("montant_credit")
        @DecimalMin(value = "0.0", message = "Credit amount cannot be negative")
        private BigDecimal montantCredit;
        
        @JsonProperty("centre_cout")
        @Size(max = 20, message = "Cost center cannot exceed 20 characters")
        private String centreCout;
        
        @JsonProperty("axe_analytique")
        @Size(max = 20, message = "Analytical axis cannot exceed 20 characters")
        private String axeAnalytique;
        
        @JsonProperty("tiers")
        @Size(max = 50, message = "Third party cannot exceed 50 characters")
        private String tiers;
        
        @JsonProperty("devise_ligne")
        @Size(max = 3, message = "Line currency cannot exceed 3 characters")
        private String deviseLigne;
        
        @JsonProperty("montant_devise")
        private BigDecimal montantDevise;
        
        @JsonProperty("taux_change")
        private BigDecimal tauxChange;
    }
    
    // Nested DTO for supporting documents
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PieceJustificativeDto {
        
        @JsonProperty("numero_piece")
        @NotBlank(message = "Document number is required")
        @Size(max = 50, message = "Document number cannot exceed 50 characters")
        private String numeroPiece;
        
        @JsonProperty("type_piece")
        @NotBlank(message = "Document type is required")
        private String typePiece; // AVIS_CREDIT, AVIS_DEBIT, ORDRE_PAIEMENT, LETTRE_REGLEMENT
        
        @JsonProperty("date_piece")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "Document date is required")
        private LocalDate datePiece;
        
        @JsonProperty("libelle_piece")
        @NotBlank(message = "Document description is required")
        @Size(max = 255, message = "Document description cannot exceed 255 characters")
        private String libellePiece;
        
        @JsonProperty("montant_piece")
        @NotNull(message = "Document amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Document amount must be positive")
        private BigDecimal montantPiece;
        
        @JsonProperty("fichier_joint")
        private String fichierJoint; // Base64 encoded file or file path
        
        @JsonProperty("nom_fichier")
        @Size(max = 255, message = "File name cannot exceed 255 characters")
        private String nomFichier;
        
        @JsonProperty("type_fichier")
        @Size(max = 10, message = "File type cannot exceed 10 characters")
        private String typeFichier; // PDF, JPG, PNG, etc.
        
        @JsonProperty("taille_fichier")
        private Long tailleFichier; // File size in bytes
    }
    
    // Helper methods for validation and status
    public boolean isValid() {
        return numeroEcriture != null && !numeroEcriture.trim().isEmpty() &&
               typeOperation != null && !typeOperation.trim().isEmpty() &&
               referenceOperation != null && !referenceOperation.trim().isEmpty() &&
               dateEcriture != null &&
               lignesComptables != null && !lignesComptables.isEmpty() &&
               montantTotal != null && montantTotal.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isComptabilise() {
        return "COMPTABILISE".equals(statutComptabilisation);
    }
    
    public boolean isRejete() {
        return "REJETE".equals(statutComptabilisation);
    }
    
    public boolean hasError() {
        return codeErreur != null && !codeErreur.trim().isEmpty();
    }
    
    public boolean isEquilibree() {
        if (lignesComptables == null || lignesComptables.isEmpty()) {
            return false;
        }
        
        BigDecimal totalDebit = lignesComptables.stream()
                .filter(ligne -> ligne.getMontantDebit() != null)
                .map(LigneComptableDto::getMontantDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = lignesComptables.stream()
                .filter(ligne -> ligne.getMontantCredit() != null)
                .map(LigneComptableDto::getMontantCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalDebit.compareTo(totalCredit) == 0;
    }
}