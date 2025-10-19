package com.microservices.meda.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvanceDTO {
    
    private Long id;
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    // Project information for detailed views
    private String nomProjet;
    
    @NotBlank(message = "Advance number is required")
    @Size(max = 50, message = "Advance number cannot exceed 50 characters")
    private String numeroAvance;
    
    @NotNull(message = "Advance type is required")
    private String typeAvance; // DON, PRET
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    private String statut; // PRIS_EN_CHARGE, COMPTABILISE
    
    private LocalDateTime createdAt;
    
    // Derived fields
    private String typeAvanceDescription;
    private String statutDescription;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AvanceCreateDTO {
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotBlank(message = "Advance number is required")
    @Size(max = 50, message = "Advance number cannot exceed 50 characters")
    private String numeroAvance;
    
    @NotNull(message = "Advance type is required")
    @Pattern(regexp = "DON|PRET", message = "Advance type must be DON or PRET")
    private String typeAvance;
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "PRIS_EN_CHARGE|COMPTABILISE", message = "Status must be PRIS_EN_CHARGE or COMPTABILISE")
    private String statut;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AvanceUpdateDTO {
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotBlank(message = "Advance number is required")
    @Size(max = 50, message = "Advance number cannot exceed 50 characters")
    private String numeroAvance;
    
    @NotNull(message = "Advance type is required")
    @Pattern(regexp = "DON|PRET", message = "Advance type must be DON or PRET")
    private String typeAvance;
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotBlank(message = "Issuer is required")
    @Size(max = 150, message = "Issuer cannot exceed 150 characters")
    private String emetteur;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "PRIS_EN_CHARGE|COMPTABILISE", message = "Status must be PRIS_EN_CHARGE or COMPTABILISE")
    private String statut;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AvanceSummaryDTO {
    
    private Long id;
    private Long projetId;
    private String nomProjet;
    private String numeroAvance;
    private String typeAvance;
    private LocalDate dateReception;
    private BigDecimal montant;
    private String devise;
    private String emetteur;
    private String statut;
    private String typeAvanceDescription;
    private String statutDescription;
}

