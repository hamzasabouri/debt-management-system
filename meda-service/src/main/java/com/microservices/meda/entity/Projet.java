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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nom_projet", nullable = false, length = 150)
    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name cannot exceed 150 characters")
    private String nomProjet;
    
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate dateDebut;
    
    @Column(name = "date_fin", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate dateFin;
    
    @Column(name = "montant_total", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @Column(name = "devise", nullable = false, length = 10)
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Avance> avances = new ArrayList<>();
    
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<PieceJustificative> piecesJustificatives = new ArrayList<>();
    
    // Validation method
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    // Helper methods for managing relationships
    public void addAvance(Avance avance) {
        avances.add(avance);
        avance.setProjet(this);
    }
    
    public void removeAvance(Avance avance) {
        avances.remove(avance);
        avance.setProjet(null);
    }
    
    public void addPieceJustificative(PieceJustificative piece) {
        piecesJustificatives.add(piece);
        piece.setProjet(this);
    }
    
    public void removePieceJustificative(PieceJustificative piece) {
        piecesJustificatives.remove(piece);
        piece.setProjet(null);
    }
    
    // Business methods
    public BigDecimal getTotalAvances() {
        return avances.stream()
                .map(Avance::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalDepenses() {
        return piecesJustificatives.stream()
                .filter(PieceJustificative::isValide)
                .map(PieceJustificative::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getSoldeDisponible() {
        return getTotalAvances().subtract(getTotalDepenses());
    }
}