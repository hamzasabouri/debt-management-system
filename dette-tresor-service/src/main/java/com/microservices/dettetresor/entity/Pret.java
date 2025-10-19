package com.microservices.dettetresor.entity;

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
@Table(name = "pret")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pret {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_pret", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Loan number is required")
    @Size(max = 50, message = "Loan number cannot exceed 50 characters")
    private String numeroPret;
    
    @Column(name = "date_signature", nullable = false)
    @NotNull(message = "Signature date is required")
    private LocalDate dateSignature;
    
    @Column(name = "organisme_bailleur", nullable = false, length = 150)
    @NotBlank(message = "Lending organization is required")
    @Size(max = 150, message = "Lending organization cannot exceed 150 characters")
    private String organismeBailleur;
    
    @Column(name = "objet", columnDefinition = "TEXT")
    private String objet;
    
    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @Column(name = "solde_courant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.0", message = "Current balance cannot be negative")
    private BigDecimal soldeCourant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @Column(name = "duree", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer duree;
    
    @Column(name = "taux_interet", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal tauxInteret;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "pret", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference // This will manage the bidirectional relationship
    private List<Echeancier> echeanciers = new ArrayList<>();
    
    @OneToMany(mappedBy = "pret", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference // This will manage the bidirectional relationship
    private List<AvisCredit> avisCredits = new ArrayList<>();
    
    @OneToMany(mappedBy = "pret", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference // This will manage the bidirectional relationship
    private List<OrdrePaiement> ordresPaiement = new ArrayList<>();
    
    // Helper methods
    public void addEcheancier(Echeancier echeancier) {
        echeanciers.add(echeancier);
        echeancier.setPret(this);
    }
    
    public void addAvisCredit(AvisCredit avisCredit) {
        avisCredits.add(avisCredit);
        avisCredit.setPret(this);
    }
    
    public void addOrdrePaiement(OrdrePaiement ordrePaiement) {
        ordresPaiement.add(ordrePaiement);
        ordrePaiement.setPret(this);
    }
}