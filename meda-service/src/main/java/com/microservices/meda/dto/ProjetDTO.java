package com.microservices.meda.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetDTO {
    
    private Long id;
    
    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name cannot exceed 150 characters")
    private String nomProjet;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDate dateDebut;
    
    @NotNull(message = "End date is required")
    private LocalDate dateFin;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    private LocalDateTime createdAt;
    
    // Financial summary fields
    private BigDecimal totalAvances;
    private BigDecimal totalDepenses;
    private BigDecimal soldeDisponible;
    
    // Relationship collections (for detailed views)
    private List<AvanceDTO> avances;
    private List<PieceJustificativeDTO> piecesJustificatives;
    
    // Summary counts (for list views)
    private Long nombreAvances;
    private Long nombrePieces;
    
    // Status derived fields
    private String statut; // ACTIF, TERMINE, EN_COURS
    private Double pourcentageUtilisation;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProjetCreateDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name cannot exceed 150 characters")
    private String nomProjet;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDate dateDebut;
    
    @NotNull(message = "End date is required")
    private LocalDate dateFin;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProjetUpdateDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name cannot exceed 150 characters")
    private String nomProjet;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDate dateDebut;
    
    @NotNull(message = "End date is required")
    private LocalDate dateFin;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProjetSummaryDTO {
    
    private Long id;
    private String nomProjet;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal montantTotal;
    private String devise;
    private String statut;
    private BigDecimal totalAvances;
    private BigDecimal totalDepenses;
    private BigDecimal soldeDisponible;
    private Double pourcentageUtilisation;
    private Long nombreAvances;
    private Long nombrePieces;
}