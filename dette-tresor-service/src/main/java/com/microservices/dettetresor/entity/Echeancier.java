package com.microservices.dettetresor.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "echeancier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Echeancier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    @NotNull(message = "Loan reference is required")
    @JsonBackReference // This will break the bidirectional relationship cycle
    private Pret pret;
    
    @Column(name = "numero_echeance", nullable = false)
    @NotNull(message = "Payment number is required")
    @Min(value = 1, message = "Payment number must be at least 1")
    private Integer numeroEcheance;
    
    @Column(name = "date_echeance", nullable = false)
    @NotNull(message = "Due date is required")
    private LocalDate dateEcheance;
    
    @Column(name = "capital", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.0", message = "Principal cannot be negative")
    private BigDecimal capital;
    
    @Column(name = "interet", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Interest amount is required")
    @DecimalMin(value = "0.0", message = "Interest cannot be negative")
    private BigDecimal interet;
    
    @Column(name = "commission", precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Commission cannot be negative")
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;
    
    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    @Builder.Default
    private StatutEcheance statut = StatutEcheance.PREVU;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_paiement_id")
    private OrdrePaiement ordrePaiement;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum StatutEcheance {
        PREVU("prévu"),
        PAYE("payé"),
        PARTIELLEMENT_PAYE("partiellement_payé"),
        EN_RETARD("en_retard");
        
        private final String description;
        
        StatutEcheance(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Helper method to calculate total
    @PrePersist
    @PreUpdate
    private void calculateTotal() {
        this.montantTotal = capital.add(interet).add(commission != null ? commission : BigDecimal.ZERO);
    }
}