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
@Table(name = "adjudication")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adjudication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_adjud", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Auction number is required")
    @Size(max = 50, message = "Auction number cannot exceed 50 characters")
    private String numeroAdjud;
    
    @Column(name = "date_adjud", nullable = false)
    @NotNull(message = "Auction date is required")
    private LocalDate dateAdjud;
    
    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutAdjudication statut;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "adjudication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    private List<AvisAdjudication> avisAdjudications = new ArrayList<>();
    
    // Enums
    public enum StatutAdjudication {
        EN_COURS("en_cours"),
        CLOTUREE("cloturee"),
        ANNULEE("annulee"),
        // Temporary constants to handle existing database records
        ANNULE("annulee"),
        VALIDEE("cloturee");
        
        private final String description;
        
        StatutAdjudication(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Helper methods for managing relationships
    public void addAvisAdjudication(AvisAdjudication avis) {
        avisAdjudications.add(avis);
        avis.setAdjudication(this);
    }
    
    public void removeAvisAdjudication(AvisAdjudication avis) {
        avisAdjudications.remove(avis);
        avis.setAdjudication(null);
    }
    
    // Business methods
    public BigDecimal getTotalCredits() {
        return avisAdjudications.stream()
                .filter(avis -> avis.getTypeAvis() == AvisAdjudication.TypeAvis.CREDIT)
                .filter(avis -> avis.getStatut() == AvisAdjudication.StatutAvis.COMPTABILISE)
                .map(AvisAdjudication::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalDebits() {
        return avisAdjudications.stream()
                .filter(avis -> avis.getTypeAvis() == AvisAdjudication.TypeAvis.DEBIT)
                .filter(avis -> avis.getStatut() == AvisAdjudication.StatutAvis.COMPTABILISE)
                .map(AvisAdjudication::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getSoldeNet() {
        return getTotalCredits().subtract(getTotalDebits());
    }
    
    public boolean isActif() {
        return StatutAdjudication.EN_COURS.equals(this.statut);
    }
    
    public boolean isCloture() {
        return StatutAdjudication.CLOTUREE.equals(this.statut);
    }
    
    public boolean isAnnule() {
        return StatutAdjudication.ANNULEE.equals(this.statut);
    }
    
    public boolean isValidée() {
        return false; // No longer used
    }
    
    // Validation method
    @PreUpdate
    private void validateStatusChange() {
        if (StatutAdjudication.ANNULEE.equals(this.statut) && !avisAdjudications.isEmpty()) {
            // Check if all avis are properly handled before cancellation
            boolean hasUnprocessedAvis = avisAdjudications.stream()
                    .anyMatch(avis -> avis.getStatut() == AvisAdjudication.StatutAvis.PRIS_EN_CHARGE);
            
            if (hasUnprocessedAvis) {
                throw new IllegalArgumentException("Cannot cancel auction with unprocessed advice notices");
            }
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Adjudication)) return false;
        Adjudication that = (Adjudication) o;
        return numeroAdjud != null && numeroAdjud.equals(that.getNumeroAdjud());
    }
    
    @Override
    public int hashCode() {
        return numeroAdjud != null ? numeroAdjud.hashCode() : 0;
    }
}