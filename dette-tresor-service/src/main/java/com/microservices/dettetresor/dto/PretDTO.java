package com.microservices.dettetresor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.dettetresor.entity.Pret;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PretDTO {
    
    private Long id;
    
    @NotBlank(message = "Loan number is required")
    @Size(max = 50, message = "Loan number cannot exceed 50 characters")
    private String numeroPret;
    
    @NotNull(message = "Signature date is required")
    private LocalDate dateSignature;
    
    @NotBlank(message = "Lending organization is required")
    @Size(max = 150, message = "Lending organization cannot exceed 150 characters")
    private String organismeBailleur;
    
    private String objet;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal montantTotal;
    
    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.0", message = "Current balance cannot be negative")
    private BigDecimal soldeCourant;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String devise;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer duree;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal tauxInteret;
    
    private LocalDateTime createdAt;
    
    // Related entities (for detailed view) - marked as JsonIgnore to prevent circular reference
    @JsonIgnore
    private List<EcheancierDTO> echeanciers;
    @JsonIgnore
    private List<AvisCreditDTO> avisCredits;
    @JsonIgnore
    private List<OrdrePaiementDTO> ordresPaiement;
    
    // Getter methods
    public Long getId() {
        return id;
    }
    
    public String getNumeroPret() {
        return numeroPret;
    }
    
    public LocalDate getDateSignature() {
        return dateSignature;
    }
    
    public String getOrganismeBailleur() {
        return organismeBailleur;
    }
    
    public String getObjet() {
        return objet;
    }
    
    public BigDecimal getMontantTotal() {
        return montantTotal;
    }
    
    public BigDecimal getSoldeCourant() {
        return soldeCourant;
    }
    
    public String getDevise() {
        return devise;
    }
    
    public Integer getDuree() {
        return duree;
    }
    
    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public List<EcheancierDTO> getEcheanciers() {
        return echeanciers;
    }
    
    public List<AvisCreditDTO> getAvisCredits() {
        return avisCredits;
    }
    
    public List<OrdrePaiementDTO> getOrdresPaiement() {
        return ordresPaiement;
    }
    
    // Setter methods
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setNumeroPret(String numeroPret) {
        this.numeroPret = numeroPret;
    }
    
    public void setDateSignature(LocalDate dateSignature) {
        this.dateSignature = dateSignature;
    }
    
    public void setOrganismeBailleur(String organismeBailleur) {
        this.organismeBailleur = organismeBailleur;
    }
    
    public void setObjet(String objet) {
        this.objet = objet;
    }
    
    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }
    
    public void setSoldeCourant(BigDecimal soldeCourant) {
        this.soldeCourant = soldeCourant;
    }
    
    public void setDevise(String devise) {
        this.devise = devise;
    }
    
    public void setDuree(Integer duree) {
        this.duree = duree;
    }
    
    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setEcheanciers(List<EcheancierDTO> echeanciers) {
        this.echeanciers = echeanciers;
    }
    
    public void setAvisCredits(List<AvisCreditDTO> avisCredits) {
        this.avisCredits = avisCredits;
    }
    
    public void setOrdresPaiement(List<OrdrePaiementDTO> ordresPaiement) {
        this.ordresPaiement = ordresPaiement;
    }
    
    // Helper method to convert from entity
    public static PretDTO fromEntity(Pret pret) {
        return PretDTO.builder()
                .id(pret.getId())
                .numeroPret(pret.getNumeroPret())
                .dateSignature(pret.getDateSignature())
                .organismeBailleur(pret.getOrganismeBailleur())
                .objet(pret.getObjet())
                .montantTotal(pret.getMontantTotal())
                .soldeCourant(pret.getSoldeCourant())
                .devise(pret.getDevise())
                .duree(pret.getDuree())
                .tauxInteret(pret.getTauxInteret())
                .createdAt(pret.getCreatedAt())
                .build();
    }
    
    // Helper method to convert from entity with Echeanciers (detailed view)
    public static PretDTO fromEntityWithEcheanciers(Pret pret) {
        PretDTO pretDTO = PretDTO.fromEntity(pret);
        if (pret.getEcheanciers() != null) {
            pretDTO.setEcheanciers(pret.getEcheanciers().stream()
                    .map(EcheancierDTO::fromEntity)  // Use fromEntity to avoid circular reference
                    .collect(Collectors.toList()));
        }
        return pretDTO;
    }
    
    // Helper method to convert to entity
    public Pret toEntity() {
        return Pret.builder()
                .id(this.id)
                .numeroPret(this.numeroPret)
                .dateSignature(this.dateSignature)
                .organismeBailleur(this.organismeBailleur)
                .objet(this.objet)
                .montantTotal(this.montantTotal)
                .soldeCourant(this.soldeCourant)
                .devise(this.devise)
                .duree(this.duree)
                .tauxInteret(this.tauxInteret)
                .build();
    }
}