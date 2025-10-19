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
@Table(name = "avis_credit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisCredit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pret_id", nullable = false)
    @NotNull(message = "Loan reference is required")
    @JsonBackReference // This will break the bidirectional relationship cycle
    private Pret pret;
    
    @Column(name = "numero_avis", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Credit advice number is required")
    @Size(max = 50, message = "Credit advice number cannot exceed 50 characters")
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
    
    @Column(name = "emetteur", nullable = false, length = 150)
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_avis", nullable = false, length = 50)
    @NotNull(message = "Credit advice type is required")
    private TypeAvisCredit typeAvis;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum TypeAvisCredit {
        VERSEMENT_CREANCIER("versement_creancier"),
        AVIS_REGLEMENT("avis_reglement");
        
        private final String description;
        
        TypeAvisCredit(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}