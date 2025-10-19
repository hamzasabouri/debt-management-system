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
@Table(name = "avance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @NotNull(message = "Project reference is required")
    @JsonIgnore
    private Projet projet;
    
    @Column(name = "numero_avance", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Advance number is required")
    @Size(max = 50, message = "Advance number cannot exceed 50 characters")
    private String numeroAvance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_avance", nullable = false, length = 20)
    @NotNull(message = "Advance type is required")
    private TypeAvance typeAvance;
    
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
    @Column(name = "statut", nullable = false, length = 30)
    @NotNull(message = "Status is required")
    private StatutAvance statut;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum TypeAvance {
        DON("don"),
        PRET("prêt");
        
        private final String description;
        
        TypeAvance(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum StatutAvance {
        PRIS_EN_CHARGE("pris_en_charge"),
        COMPTABILISE("comptabilisé");
        
        private final String description;
        
        StatutAvance(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business methods
    public boolean isPret() {
        return TypeAvance.PRET.equals(this.typeAvance);
    }
    
    public boolean isDon() {
        return TypeAvance.DON.equals(this.typeAvance);
    }
    
    public boolean isComptabilise() {
        return StatutAvance.COMPTABILISE.equals(this.statut);
    }
    
    public boolean isPrisEnCharge() {
        return StatutAvance.PRIS_EN_CHARGE.equals(this.statut);
    }
    
    // Override equals and hashCode for entity management
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Avance)) return false;
        Avance avance = (Avance) o;
        return numeroAvance != null && numeroAvance.equals(avance.getNumeroAvance());
    }
    
    @Override
    public int hashCode() {
        return numeroAvance != null ? numeroAvance.hashCode() : 0;
    }
}