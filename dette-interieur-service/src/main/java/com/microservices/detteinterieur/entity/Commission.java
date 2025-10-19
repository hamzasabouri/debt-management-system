package com.microservices.detteinterieur.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_commission", nullable = false, length = 20)
    @NotNull(message = "Commission type is required")
    private TypeCommission typeCommission;
    
    @Column(name = "ordre_paiement_id")
    private Long ordrePaiementId; // Reference to external payment order
    
    @Column(name = "lettre_reglement_id")
    private Long lettreReglementId; // Reference to settlement letter
    
    @Column(name = "avis_debit_id")
    private Long avisDebitId; // Reference to debit notice
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutCommission statut;
    
    @Column(name = "numero_reference", length = 100)
    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String numeroReference;
    
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;
    
    // Enums
    public enum TypeCommission {
        MAROCLEAR("maroclear"),
        BAM("bam"),
        GESTION("gestion"),
        BANCAIRE("bancaire"),
        SERVICE("service");;// <- ajouter cette valeur

        
        private final String description;
        
        TypeCommission(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum StatutCommission {
        EN_ATTENTE("en_attente"),
        EN_COURS_TRAITEMENT("en_cours_traitement"),
        PAYE("paye"),
        REJETE("rejete"),
        // Temporary constant to handle existing database records with "PAYEE" value
        PAYEE("paye");


        private final String description;
        
        StatutCommission(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business methods
    public boolean isMaroclear() {
        return TypeCommission.MAROCLEAR.equals(this.typeCommission);
    }
    
    public boolean isBAM() {
        return TypeCommission.BAM.equals(this.typeCommission);
    }
    
    public boolean isEnAttente() {
        return StatutCommission.EN_ATTENTE.equals(this.statut);
    }
    
    public boolean isEnCoursTraitement() {
        return StatutCommission.EN_COURS_TRAITEMENT.equals(this.statut);
    }
    
    public boolean isPaye() {
        return StatutCommission.PAYE.equals(this.statut);
    }
    
    public boolean isRejete() {
        return StatutCommission.REJETE.equals(this.statut);
    }
    
    public boolean hasOrdrePaiement() {
        return ordrePaiementId != null;
    }
    
    public boolean hasLettreReglement() {
        return lettreReglementId != null;
    }
    
    public boolean hasAvisDebit() {
        return avisDebitId != null;
    }
    
    public boolean isCompleteWorkflow() {
        if (isMaroclear()) {
            // Maroclear workflow: ordre paiement → lettre règlement → avis débit
            return hasOrdrePaiement() && hasLettreReglement() && hasAvisDebit();
        } else if (isBAM()) {
            // BAM workflow: direct avis débit processing
            return hasAvisDebit();
        }
        return false;
    }
    
    // Calculate commission rate based on type and amount
    public BigDecimal calculateCommissionRate() {
        if (TypeCommission.MAROCLEAR.equals(typeCommission)) {
            return new BigDecimal("0.002"); // 0.2%
        } else if (TypeCommission.BAM.equals(typeCommission)) {
            return new BigDecimal("0.001"); // 0.1%
        }
        return BigDecimal.ZERO;
    }
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Validate workflow completion for status changes
        if (StatutCommission.PAYE.equals(statut) && !isCompleteWorkflow()) {
            throw new IllegalArgumentException("Cannot mark commission as paid without complete workflow");
        }
        
        // Validate payment date
        if (StatutCommission.PAYE.equals(statut) && datePaiement == null) {
            datePaiement = LocalDateTime.now();
        }
        
        // Validate commission amount
        if (montant != null && montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Commission amount must be positive");
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commission)) return false;
        Commission that = (Commission) o;
        return numeroReference != null && numeroReference.equals(that.getNumeroReference());
    }
    
    @Override
    public int hashCode() {
        return numeroReference != null ? numeroReference.hashCode() : 0;
    }
}