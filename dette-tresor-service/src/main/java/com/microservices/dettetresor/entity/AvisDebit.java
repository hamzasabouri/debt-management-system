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
@Table(name = "avis_debit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisDebit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    @JsonBackReference // This will break the bidirectional relationship cycle
    private Pret pret; // Loan associated with this debit advice
    
    // Changed from OneToOne to ManyToOne to match the relationship in OrdrePaiement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_paiement_id")
    @JsonBackReference // This will break the bidirectional relationship cycle
    private OrdrePaiement ordrePaiement; // Nullable for special cases (fees, balance aid)
    
    @Column(name = "numero_avis", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Debit advice number is required")
    @Size(max = 50, message = "Debit advice number cannot exceed 50 characters")
    private String numeroAvis;
    
    @Column(name = "date_reception", nullable = false)
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @Column(name = "montant", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "motif", nullable = false, length = 100)
    @NotNull(message = "Reason is required")
    private MotifAvisDebit motif;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum MotifAvisDebit {
        REMBOURSEMENT("remboursement"),
        FRAIS_TRANSFERT("frais_transfert"),
        AIDE_BALANCE("aide_balance");
        
        private final String description;
        
        MotifAvisDebit(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}