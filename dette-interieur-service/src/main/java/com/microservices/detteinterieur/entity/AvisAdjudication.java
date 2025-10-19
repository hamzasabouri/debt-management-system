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
@Table(name = "avis_adjudication")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisAdjudication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjudication_id", nullable = false)
    @NotNull(message = "Auction reference is required")
    @JsonBackReference
    private Adjudication adjudication;
    
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
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum TypeAvis {
        CREDIT("credit"),
        DEBIT("debit");
        
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
        // Temporary constant to handle existing database records with "VALIDEE" value
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
    
    public boolean isFromMaroclear() {
        return emetteur != null && emetteur.toUpperCase().contains("MAROCLEAR");
    }
    
    public boolean isFromComptable() {
        return emetteur != null && emetteur.toUpperCase().contains("COMPTABLE");
    }
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Validate that the advice is related to the correct auction
        if (adjudication != null && adjudication.isAnnule()) {
            throw new IllegalArgumentException("Cannot add advice to cancelled auction");
        }
        
        // Validate amount consistency
        if (montant != null && montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advice amount must be positive");
        }
        
        // Validate issuer based on advice type
        if (TypeAvis.CREDIT.equals(typeAvis) && !isFromBAM()) {
            // Credit notices typically come from BAM
            // This is a business rule that can be adjusted
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvisAdjudication)) return false;
        AvisAdjudication that = (AvisAdjudication) o;
        return numeroAvis != null && numeroAvis.equals(that.getNumeroAvis());
    }
    
    @Override
    public int hashCode() {
        return numeroAvis != null ? numeroAvis.hashCode() : 0;
    }
}