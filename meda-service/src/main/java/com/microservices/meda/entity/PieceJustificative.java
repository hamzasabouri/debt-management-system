package com.microservices.meda.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "piece_justificative")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJustificative {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @NotNull(message = "Project reference is required")
    @JsonIgnore
    private Projet projet;
    
    @Column(name = "numero_piece", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Document number is required")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String numeroPiece;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", nullable = false, length = 50)
    @NotNull(message = "Document type is required")
    private TypePiece typePiece;
    
    @Column(name = "date_execution", nullable = false)
    @NotNull(message = "Execution date is required")
    private LocalDate dateExecution;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @Column(name = "emetteur", nullable = false, length = 150)
    @NotBlank(message = "Issuer (accountant) is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutPiece statut;
    
    @Column(name = "commentaire", length = 500)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaire;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum TypePiece {
        FACTURE("facture"),
        BON_PAIEMENT("bon_de_paiement"),
        RECU("reçu"),
        AUTRE("autre");
        
        private final String description;
        
        TypePiece(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum StatutPiece {
        PRIS_EN_CHARGE("pris_en_charge"),
        VALIDE("validé"),
        REJETE("rejeté");
        
        private final String description;
        
        StatutPiece(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business methods
    public boolean isValide() {
        return StatutPiece.VALIDE.equals(this.statut);
    }
    
    public boolean isRejete() {
        return StatutPiece.REJETE.equals(this.statut);
    }
    
    public boolean isPrisEnCharge() {
        return StatutPiece.PRIS_EN_CHARGE.equals(this.statut);
    }
    
    public boolean isFacture() {
        return TypePiece.FACTURE.equals(this.typePiece);
    }
    
    public boolean isBonPaiement() {
        return TypePiece.BON_PAIEMENT.equals(this.typePiece);
    }
    
    public boolean isRecu() {
        return TypePiece.RECU.equals(this.typePiece);
    }
    
    // Validation method
    @PreUpdate
    private void validateStatusChange() {
        if (StatutPiece.REJETE.equals(this.statut) && (commentaire == null || commentaire.trim().isEmpty())) {
            throw new IllegalArgumentException("Comment is required when rejecting a document");
        }
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PieceJustificative)) return false;
        PieceJustificative that = (PieceJustificative) o;
        return numeroPiece != null && numeroPiece.equals(that.getNumeroPiece());
    }
    
    @Override
    public int hashCode() {
        return numeroPiece != null ? numeroPiece.hashCode() : 0;
    }
}