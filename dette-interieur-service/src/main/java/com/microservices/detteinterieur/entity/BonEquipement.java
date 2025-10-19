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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "bon_equipement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonEquipement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_bon", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Equipment bond number is required")
    @Size(max = 50, message = "Bond number cannot exceed 50 characters")
    private String numeroBon;
    
    @Column(name = "date_souscription", nullable = false)
    @NotNull(message = "Subscription date is required")
    private LocalDate dateSouscription;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutBon statut;
    
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
    
    @Column(name = "taux_interet", precision = 5, scale = 4)
    private BigDecimal tauxInteret;
    
    @Column(name = "souscripteur", length = 150)
    @Size(max = 150, message = "Subscriber name cannot exceed 150 characters")
    private String souscripteur;
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "date_remboursement")
    private LocalDate dateRemboursement;
    
    // Relationships
    @OneToMany(mappedBy = "bonEquipement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<AvisBonEquipement> avisBons = new ArrayList<>();
    
    // Enums
    public enum StatutBon {
        SOUSCRIT("souscrit"),
        REMBOURSE("rembourse"),
        REJETE("rejete"),
        EN_COURS("en_cours"),
        EXPIRE("expire"),
        // Temporary constant to handle existing database records with "ANNULE" value
        ANNULE("annule");
        
        private final String description;
        
        StatutBon(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Helper methods for managing relationships
    public void addAvisBonEquipement(AvisBonEquipement avis) {
        avisBons.add(avis);
        avis.setBonEquipement(this);
    }
    
    public void removeAvisBonEquipement(AvisBonEquipement avis) {
        avisBons.remove(avis);
        avis.setBonEquipement(null);
    }
    
    // Business methods
    public boolean isSouscrit() {
        return StatutBon.SOUSCRIT.equals(this.statut);
    }
    
    public boolean isRembourse() {
        return StatutBon.REMBOURSE.equals(this.statut);
    }
    
    public boolean isRejete() {
        return StatutBon.REJETE.equals(this.statut);
    }
    
    public boolean isEnCours() {
        return StatutBon.EN_COURS.equals(this.statut);
    }
    
    public boolean isExpire() {
        return StatutBon.EXPIRE.equals(this.statut);
    }
    
    public boolean isAnnule() {
        return StatutBon.ANNULE.equals(this.statut);
    }
    
    public BigDecimal getTotalCredits() {
        return avisBons.stream()
                .filter(avis -> avis.getTypeAvis() == AvisBonEquipement.TypeAvis.CREDIT)
                .filter(avis -> avis.getStatut() == AvisBonEquipement.StatutAvis.COMPTABILISE)
                .map(AvisBonEquipement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalDebits() {
        return avisBons.stream()
                .filter(avis -> avis.getTypeAvis() == AvisBonEquipement.TypeAvis.DEBIT)
                .filter(avis -> avis.getStatut() == AvisBonEquipement.StatutAvis.COMPTABILISE)
                .map(AvisBonEquipement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalRejets() {
        return avisBons.stream()
                .filter(avis -> avis.getTypeAvis() == AvisBonEquipement.TypeAvis.REJET)
                .map(AvisBonEquipement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getSoldeNet() {
        return getTotalCredits().subtract(getTotalDebits());
    }
    
    public BigDecimal calculateInterets() {
        if (tauxInteret == null || montant == null || dateSouscription == null) {
            return BigDecimal.ZERO;
        }
        
        LocalDate dateCalcul = dateRemboursement != null ? dateRemboursement : LocalDate.now();
        long joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(dateSouscription, dateCalcul);
        
        // Calculate simple interest: montant * taux * (jours/365)
        return montant.multiply(tauxInteret)
                .multiply(BigDecimal.valueOf(joursEcoules))
                .divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP);
    }
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Auto-update status based on dates
        if (dateEcheance != null && dateEcheance.isBefore(LocalDate.now()) && 
            !isRembourse() && !isRejete()) {
            statut = StatutBon.EXPIRE;
        }
        
        // Validate reimbursement
        if (StatutBon.REMBOURSE.equals(statut) && dateRemboursement == null) {
            dateRemboursement = LocalDate.now();
        }
        
        // Validate interest rate
        if (tauxInteret != null && tauxInteret.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BonEquipement)) return false;
        BonEquipement that = (BonEquipement) o;
        return numeroBon != null && numeroBon.equals(that.getNumeroBon());
    }
    
    @Override
    public int hashCode() {
        return numeroBon != null ? numeroBon.hashCode() : 0;
    }
}