package com.microservices.detteinterieur.dto.dtfe;

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
 * DTO for DTFE (Direction du Trésor et des Finances Extérieures) integration
 * Used for sending treasury operations and debt management information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationTresorDto {
    
    @JsonProperty("numero_operation")
    @NotBlank(message = "Operation number is required")
    @Size(max = 50, message = "Operation number cannot exceed 50 characters")
    private String numeroOperation;
    
    @JsonProperty("type_operation")
    @NotBlank(message = "Operation type is required")
    private String typeOperation; // ADJUDICATION, INTERET_DEPOT, BON_EQUIPEMENT, COMMISSION
    
    @JsonProperty("sous_type_operation")
    private String sousTypeOperation; // COLLECTIVITE_LOCALE, DEPOT_TRESOR, MAROCLEAR, BAM
    
    @JsonProperty("reference_interne")
    @NotBlank(message = "Internal reference is required")
    private String referenceInterne;
    
    @JsonProperty("date_operation")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Operation date is required")
    private LocalDate dateOperation;
    
    @JsonProperty("date_valeur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Value date is required")
    private LocalDate dateValeur;
    
    @JsonProperty("montant_principal")
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Principal amount must be positive")
    private BigDecimal montantPrincipal;
    
    @JsonProperty("montant_interet")
    @DecimalMin(value = "0.0", message = "Interest amount cannot be negative")
    private BigDecimal montantInteret;
    
    @JsonProperty("montant_commission")
    @DecimalMin(value = "0.0", message = "Commission amount cannot be negative")
    private BigDecimal montantCommission;
    
    @JsonProperty("montant_operation")
    @NotNull(message = "Operation amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Operation amount must be positive")
    private BigDecimal montantOperation;
    
    @JsonProperty("sens_operation")
    private String sensOperation; // ENTREE, SORTIE
    
    @JsonProperty("categorie_operation")
    private String categorieOperation; // FINANCEMENT, FRAIS, INTERET
    
    @JsonProperty("description")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @JsonProperty("date_soumission")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateSoumission;
    
    @JsonProperty("date_traitement")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTraitement;
    
    @JsonProperty("code_erreur")
    private String codeErreur;
    
    @JsonProperty("message_erreur")
    private String messageErreur;
    
    @JsonProperty("devise")
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    private String devise;
    
    @JsonProperty("taux_interet")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal tauxInteret;
    
    @JsonProperty("periode_debut")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodeDebut;
    
    @JsonProperty("periode_fin")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodeFin;
    
    @JsonProperty("entite_concerne")
    @NotBlank(message = "Concerned entity is required")
    @Size(max = 150, message = "Entity name cannot exceed 150 characters")
    private String entiteConcerne;
    
    @JsonProperty("compte_tresor")
    @NotBlank(message = "Treasury account is required")
    @Size(max = 50, message = "Treasury account cannot exceed 50 characters")
    private String compteTresor;
    
    @JsonProperty("statut_operation")
    private String statutOperation; // EN_ATTENTE, VALIDEE, COMPTABILISEE, REJETE
    
    @JsonProperty("statut_validation")
    private String statutValidation; // NON_VALIDEE, VALIDEE, REJETE
    
    @JsonProperty("validateur")
    @Size(max = 100, message = "Validator name cannot exceed 100 characters")
    private String validateur;
    
    @JsonProperty("date_validation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateValidation;
    
    @JsonProperty("observations")
    @Size(max = 1000, message = "Observations cannot exceed 1000 characters")
    private String observations;
    
    @JsonProperty("pieces_jointes")
    @Valid
    private List<PieceJointeDto> piecesJointes;
    
    @JsonProperty("informations_complementaires")
    @Valid
    private InformationsComplementairesDto informationsComplementaires;
    
    @JsonProperty("date_creation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonProperty("utilisateur_creation")
    private String utilisateurCreation;
    
    @JsonProperty("date_modification")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;
    
    @JsonProperty("utilisateur_modification")
    private String utilisateurModification;
    
    // Nested DTO for attached documents
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PieceJointeDto {
        
        @JsonProperty("nom_piece")
        @NotBlank(message = "Document name is required")
        @Size(max = 255, message = "Document name cannot exceed 255 characters")
        private String nomPiece;
        
        @JsonProperty("type_piece")
        @NotBlank(message = "Document type is required")
        private String typePiece; // AVIS_ADJUDICATION, LETTRE_REGLEMENT, AVIS_CREDIT, JUSTIFICATIF
        
        @JsonProperty("contenu_base64")
        private String contenuBase64; // Base64 encoded file content
        
        @JsonProperty("type_mime")
        @Size(max = 100, message = "MIME type cannot exceed 100 characters")
        private String typeMime;
        
        @JsonProperty("taille")
        private Long taille; // File size in bytes
        
        @JsonProperty("date_piece")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate datePiece;
        
        @JsonProperty("description")
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
    }
    
    // Nested DTO for additional information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InformationsComplementairesDto {
        
        @JsonProperty("numero_adjudication")
        private String numeroAdjudication;
        
        @JsonProperty("numero_bon_equipement")
        private String numeroBonEquipement;
        
        @JsonProperty("numero_compte_depot")
        private String numeroCompteDepot;
        
        @JsonProperty("type_fonds")
        private String typeFonds; // COLLECTIVITE_LOCALE, DEPOT_TRESOR
        
        @JsonProperty("organisme")
        @Size(max = 150, message = "Organization name cannot exceed 150 characters")
        private String organisme;
        
        @JsonProperty("contact_responsable")
        @Size(max = 150, message = "Contact name cannot exceed 150 characters")
        private String contactResponsable;
        
        @JsonProperty("telephone")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String telephone;
        
        @JsonProperty("email")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        private String email;
        
        @JsonProperty("adresse")
        @Size(max = 500, message = "Address cannot exceed 500 characters")
        private String adresse;
        
        @JsonProperty("code_postal")
        @Size(max = 10, message = "Postal code cannot exceed 10 characters")
        private String codePostal;
        
        @JsonProperty("ville")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        private String ville;
        
        @JsonProperty("pays")
        @Size(max = 100, message = "Country cannot exceed 100 characters")
        private String pays;
    }
    
    // Helper methods for validation and status
    public boolean isValid() {
        return numeroOperation != null && !numeroOperation.trim().isEmpty() &&
               typeOperation != null && !typeOperation.trim().isEmpty() &&
               referenceInterne != null && !referenceInterne.trim().isEmpty() &&
               dateOperation != null &&
               montantPrincipal != null && montantPrincipal.compareTo(BigDecimal.ZERO) > 0 &&
               montantOperation != null && montantOperation.compareTo(BigDecimal.ZERO) > 0 &&
               devise != null && !devise.trim().isEmpty() &&
               entiteConcerne != null && !entiteConcerne.trim().isEmpty() &&
               compteTresor != null && !compteTresor.trim().isEmpty();
    }
    
    public boolean isValidee() {
        return "VALIDEE".equals(statutValidation) || "VALIDEE".equals(statutOperation);
    }
    
    public boolean isComptabilisee() {
        return "COMPTABILISEE".equals(statutOperation);
    }
    
    public boolean isREJETE() {
        return "REJETE".equals(statutValidation) || "REJETE".equals(statutOperation);
    }
    
    public boolean isAdjudication() {
        return "ADJUDICATION".equals(typeOperation);
    }
    
    public boolean isInteretDepot() {
        return "INTERET_DEPOT".equals(typeOperation);
    }
    
    public boolean isBonEquipement() {
        return "BON_EQUIPEMENT".equals(typeOperation);
    }
    
    public boolean isCommission() {
        return "COMMISSION".equals(typeOperation);
    }
    
    public boolean isCollectiviteLocale() {
        return "COLLECTIVITE_LOCALE".equals(sousTypeOperation);
    }
    
    public boolean isDepotTresor() {
        return "DEPOT_TRESOR".equals(sousTypeOperation);
    }
}