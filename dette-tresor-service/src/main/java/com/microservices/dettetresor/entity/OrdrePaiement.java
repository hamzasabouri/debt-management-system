package com.microservices.dettetresor.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordre_paiement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdrePaiement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    @NotNull(message = "Loan reference is required")
    @JsonBackReference // This will break the bidirectional relationship cycle
    private Pret pret;
    
    @Column(name = "numero_ordre", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Payment order number is required")
    @Size(max = 50, message = "Payment order number cannot exceed 50 characters")
    private String numeroOrdre;
    
    @Column(name = "date_emission", nullable = false)
    @NotNull(message = "Issue date is required")
    private LocalDate dateEmission;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @Column(name = "echeance", nullable = false)
    @NotNull(message = "Due date is required")
    private LocalDate echeance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    @Builder.Default
    private StatutOrdrePaiement statut = StatutOrdrePaiement.EN_ATTENTE;
    
    @OneToOne(mappedBy = "ordrePaiement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // This will manage the bidirectional relationship
    private LettreReglement lettreReglement;
    
    // Changed from OneToOne to OneToMany
    @OneToMany(mappedBy = "ordrePaiement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // This will manage the bidirectional relationship
    @Builder.Default
    private List<AvisDebit> avisDebits = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Helper method to add AvisDebit
    public void addAvisDebit(AvisDebit avisDebit) {
        avisDebits.add(avisDebit);
        avisDebit.setOrdrePaiement(this);
    }
    
    // Helper method to remove AvisDebit
    public void removeAvisDebit(AvisDebit avisDebit) {
        avisDebits.remove(avisDebit);
        avisDebit.setOrdrePaiement(null);
    }
    
    public enum StatutOrdrePaiement {
        EN_ATTENTE("en_attente"),
        PRIS_EN_CHARGE("pris_en_charge"),
        PAYE("payé"),
        ANNULE("annulé");
        
        private final String description;
        
        StatutOrdrePaiement(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}