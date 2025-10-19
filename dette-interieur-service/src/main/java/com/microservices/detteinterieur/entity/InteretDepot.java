package com.microservices.detteinterieur.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "interet_depot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteretDepot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_fonds", nullable = false, length = 30)
    @NotNull(message = "Fund type is required")
    private TypeFonds typeFonds;
    
    @Column(name = "numero_compte", nullable = false, length = 50)
    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String numeroCompte;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Interest amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Interest amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    @Builder.Default
    private String devise = "MAD";
    
    @Column(name = "date_calcul", nullable = false)
    @NotNull(message = "Calculation date is required")
    private LocalDate dateCalcul;
    
    @Column(name = "avis_credit_id")
    private Long avisCreditId; // Reference to generated credit notice
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutInteret statut;
    
    @Column(name = "taux_interet", precision = 5, scale = 4)
    private BigDecimal tauxInteret;
    
    @Column(name = "montant_principal", precision = 18, scale = 2)
    private BigDecimal montantPrincipal;
    
    @Column(name = "periode_debut")
    private LocalDate periodeDebut;
    
    @Column(name = "periode_fin")
    private LocalDate periodeFin;
    
    @Column(name = "nom_titulaire", length = 150)
    @Size(max = 150, message = "Account holder name cannot exceed 150 characters")
    private String nomTitulaire;
    
    @Column(name = "organisme", length = 150)
    @Size(max = 150, message = "Organization name cannot exceed 150 characters")
    private String organisme;
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;
    
    // Enums
    public enum TypeFonds {
        COLLECTIVITE_LOCALE("collectivite_locale"),
        DEPOT_A_VUE("depot_a_vue"),
        DEPOT_TRESOR("depot_tresor"),
        DEPOT_A_TERME("DEPOT_A_TERME");
        
        private final String description;
        
        TypeFonds(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum StatutInteret {
        CALCULE("calcule"),
        PRIS_EN_CHARGE("pris_en_charge"),
        COMPTABILISE("comptabilise"),
        TRANSMIS("transmis"),
        REJETE("rejete"),
        // Temporary constant to handle existing database records with "VALIDEE" or "VALIDE" values
        VALIDEE("comptabilise"),
        VALIDE("comptabilise"),
        EN_COURS("en_cours");
        
        private final String description;
        
        StatutInteret(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business methods
    public boolean isCollectiviteLocale() {
        return TypeFonds.COLLECTIVITE_LOCALE.equals(this.typeFonds);
    }
    
    public boolean isDepotTresor() {
        return TypeFonds.DEPOT_TRESOR.equals(this.typeFonds);
    }
    
    public boolean isCalcule() {
        return StatutInteret.CALCULE.equals(this.statut);
    }
    
    public boolean isPrisEnCharge() {
        return StatutInteret.PRIS_EN_CHARGE.equals(this.statut);
    }
    
    public boolean isComptabilise() {
        return StatutInteret.COMPTABILISE.equals(this.statut);
    }
    
    public boolean isTransmis() {
        return StatutInteret.TRANSMIS.equals(this.statut);
    }
    
    public boolean isRejete() {
        return StatutInteret.REJETE.equals(this.statut);
    }
    
    public boolean hasAvisCredit() {
        return avisCreditId != null;
    }
    
    // Explicit getters for Lombok
    public Long getId() {
        return id;
    }
    
    public TypeFonds getTypeFonds() {
        return typeFonds;
    }
    
    public String getNumeroCompte() {
        return numeroCompte;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public String getDevise() {
        return devise;
    }
    
    public LocalDate getDateCalcul() {
        return dateCalcul;
    }
    
    public Long getAvisCreditId() {
        return avisCreditId;
    }
    
    public StatutInteret getStatut() {
        return statut;
    }
    
    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }
    
    public BigDecimal getMontantPrincipal() {
        return montantPrincipal;
    }
    
    public LocalDate getPeriodeDebut() {
        return periodeDebut;
    }
    
    public LocalDate getPeriodeFin() {
        return periodeFin;
    }
    
    public String getNomTitulaire() {
        return nomTitulaire;
    }
    
    public String getOrganisme() {
        return organisme;
    }
    
    public String getCommentaire() {
        return commentaire;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }
    
    // Calculate interest based on principal, rate, and period
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, 
                                      LocalDate startDate, LocalDate endDate) {
        if (principal == null || rate == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }
        
        long jours = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (jours <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate simple interest: principal * rate * (days/365)
        return principal.multiply(rate)
                .multiply(BigDecimal.valueOf(jours))
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
    }
    
    // Auto-calculate interest if parameters are available
    public void autoCalculateInterest() {
        if (montantPrincipal != null && tauxInteret != null && 
            periodeDebut != null && periodeFin != null) {
            montant = calculateInterest(montantPrincipal, tauxInteret, periodeDebut, periodeFin);
        }
    }
    
    // Get default interest rate based on fund type
    public BigDecimal getDefaultInterestRate() {
        if (TypeFonds.COLLECTIVITE_LOCALE.equals(typeFonds)) {
            return new BigDecimal("0.03"); // 3%
        } else if (TypeFonds.DEPOT_TRESOR.equals(typeFonds)) {
            return new BigDecimal("0.025"); // 2.5%
        }
        return BigDecimal.ZERO;
    }
    
    // Process interest calculation
    public void processer() {
        if (StatutInteret.CALCULE.equals(statut)) {
            statut = StatutInteret.PRIS_EN_CHARGE;
            dateTraitement = LocalDateTime.now();
        }
    }
    
    public void comptabiliser() {
        if (StatutInteret.PRIS_EN_CHARGE.equals(statut)) {
            statut = StatutInteret.COMPTABILISE;
            dateTraitement = LocalDateTime.now();
        }
    }
    
    public void transmettre() {
        if (StatutInteret.COMPTABILISE.equals(statut)) {
            statut = StatutInteret.TRANSMIS;
            dateTraitement = LocalDateTime.now();
        }
    }
    
    public void rejeter(String motif) {
        statut = StatutInteret.REJETE;
        commentaire = motif;
        dateTraitement = LocalDateTime.now();
    }
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Auto-calculate interest if not set
        if (montant == null || montant.compareTo(BigDecimal.ZERO) == 0) {
            autoCalculateInterest();
        }
        
        // Set default interest rate if not provided
        if (tauxInteret == null) {
            tauxInteret = getDefaultInterestRate();
        }
        
        // Validate period dates
        if (periodeDebut != null && periodeFin != null && periodeDebut.isAfter(periodeFin)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Validate amounts
        if (montant != null && montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest amount must be positive");
        }
        
        if (montantPrincipal != null && montantPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive");
        }
        
        // Auto-set processing date
        if (!StatutInteret.CALCULE.equals(statut) && dateTraitement == null) {
            dateTraitement = LocalDateTime.now();
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InteretDepot)) return false;
        InteretDepot that = (InteretDepot) o;
        return numeroCompte != null && numeroCompte.equals(that.numeroCompte) &&
               dateCalcul != null && dateCalcul.equals(that.dateCalcul);
    }
    
    @Override
    public int hashCode() {
        return numeroCompte != null && dateCalcul != null ? 
               (numeroCompte + dateCalcul.toString()).hashCode() : 0;
    }
}