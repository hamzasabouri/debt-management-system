package com.microservices.meda.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvanceBAMDTO {
    
    // DTO for BAM integration - receiving credit notices
    @NotBlank(message = "Advance number is required")
    private String numeroAvance;
    
    @NotNull(message = "Project ID is required")
    private Long projetId;
    
    @NotNull(message = "Advance type is required")
    @Pattern(regexp = "DON|PRET", message = "Advance type must be DON or PRET")
    private String typeAvance;
    
    @NotNull(message = "Reception date is required")
    private LocalDate dateReception;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal montant;
    
    @NotBlank(message = "Currency is required")
    private String devise;
    
    @NotBlank(message = "Issuer is required")
    private String emetteur;
    
    // BAM specific fields
    private String referenceBAM;
    private String typeOperationBAM;
}