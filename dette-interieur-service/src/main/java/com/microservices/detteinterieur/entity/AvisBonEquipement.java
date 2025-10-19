package com.microservices.detteinterieur.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "avis_bon_equipement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisBonEquipement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_equipement_id", nullable = false)
    @NotNull(message = "Equipment bond reference is required")
    @JsonBackReference
    private BonEquipement bonEquipement;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_avis", nullable = false, length = 20)
    @NotNull(message = "Advice type is required")
    private TypeAvis typeAvis;
    
    @Column(name = "numero_avis", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Advice number is required")
    @Size(max = 50, message = "Advice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @Column(name = "date_reception", nullable = false)
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "emetteur", nullable = false, length = 150)
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutAvis statut;
    
    @Column(name = "motif_rejet", length = 500)
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String motifRejet;
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;
    
    // Enums
    public enum TypeAvis {
        CREDIT("credit"),
        DEBIT("debit"),
        REJET("rejet");
        
        private final String description;
        
        TypeAvis(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
   public enum StatutAvis {
    PRIS_EN_CHARGE("pris_en_charge"),
    COMPTABILISE("comptabilise"),
    REJETE("rejete"),
    EN_ATTENTE("en_attente"),
    EN_COURS("en_cours"),   // <-- ajouter
    VALIDEE("comptabilise"); 

        
        private final String description;
        
        StatutAvis(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business methods
    public boolean isCredit() {
        return TypeAvis.CREDIT.equals(this.typeAvis);
    }
    
    public boolean isDebit() {
        return TypeAvis.DEBIT.equals(this.typeAvis);
    }
    
    public boolean isRejet() {
        return TypeAvis.REJET.equals(this.typeAvis);
    }
    
    public boolean isPrisEnCharge() {
        return StatutAvis.PRIS_EN_CHARGE.equals(this.statut);
    }
    
    public boolean isComptabilise() {
        return StatutAvis.COMPTABILISE.equals(this.statut);
    }
    
    public boolean isRejete() {
        return StatutAvis.REJETE.equals(this.statut);
    }
    
    public boolean isEnAttente() {
        return StatutAvis.EN_ATTENTE.equals(this.statut);
    }
    
    public boolean isFromBAM() {
        return emetteur != null && (emetteur.toUpperCase().contains("BAM") || 
                                    emetteur.toUpperCase().contains("BANK AL-MAGHRIB"));
    }
    
    public boolean isFromComptable() {
        return emetteur != null && emetteur.toUpperCase().contains("COMPTABLE");
    }
    
    public boolean requiresRejectionReason() {
        return TypeAvis.REJET.equals(typeAvis) || StatutAvis.REJETE.equals(statut);
    }
    
    // Process advice notice
    public void processer() {
        if (StatutAvis.EN_ATTENTE.equals(statut)) {
            statut = StatutAvis.PRIS_EN_CHARGE;
            dateTraitement = LocalDateTime.now();
        }
    }
    
    public void comptabiliser() {
        if (StatutAvis.PRIS_EN_CHARGE.equals(statut)) {
            statut = StatutAvis.COMPTABILISE;
            dateTraitement = LocalDateTime.now();
        }
    }
    
    public void rejeter(String motif) {
        statut = StatutAvis.REJETE;
        motifRejet = motif;
        dateTraitement = LocalDateTime.now();
    }
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Validate rejection reason for rejection notices
        if (requiresRejectionReason() && 
            (motifRejet == null || motifRejet.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required for rejection notices");
        }
        
        // Validate business logic for rejection notices
        if (TypeAvis.REJET.equals(typeAvis)) {
            // Rejection notices should reverse previous operations
            // This is typically used to cancel a previous credit or debit
        }
        
        // Auto-set processing date
        if (!StatutAvis.EN_ATTENTE.equals(statut) && dateTraitement == null) {
            dateTraitement = LocalDateTime.now();
        }
        
        // Validate amount
        if (montant != null && montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advice amount must be positive");
        }
        
        // Validate that rejected bond can't receive new credit notices
        if (bonEquipement != null && bonEquipement.isRejete() && TypeAvis.CREDIT.equals(typeAvis)) {
            throw new IllegalArgumentException("Cannot add credit notice to rejected equipment bond");
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvisBonEquipement)) return false;
        AvisBonEquipement that = (AvisBonEquipement) o;
        return numeroAvis != null && numeroAvis.equals(that.getNumeroAvis());
    }
    
    @Override
    public int hashCode() {
        return numeroAvis != null ? numeroAvis.hashCode() : 0;
    }
}