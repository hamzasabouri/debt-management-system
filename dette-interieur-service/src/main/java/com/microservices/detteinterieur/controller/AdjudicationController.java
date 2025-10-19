package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.entity.Adjudication;
import com.microservices.detteinterieur.dto.AdjudicationDto;
import com.microservices.detteinterieur.service.AdjudicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/dette-interieur/adjudications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Adjudication", description = "API pour la gestion des adjudications")
@SecurityRequirement(name = "bearerAuth")
public class AdjudicationController {
    
    private final AdjudicationService adjudicationService;
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Adjudication créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "409", description = "Numéro d'adjudication déjà existant")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AdjudicationDto> createAdjudication(@Valid @RequestBody AdjudicationDto adjudicationDto) {
        log.info("Creating new adjudication: {}", adjudicationDto.getNumeroAdjud());
        
        try {
            // Convert DTO to entity
            Adjudication adjudication = new Adjudication();
            adjudication.setNumeroAdjud(adjudicationDto.getNumeroAdjud());
            adjudication.setDateAdjud(adjudicationDto.getDateAdjud());
            adjudication.setMontantTotal(adjudicationDto.getMontantTotal());
            adjudication.setStatut(adjudicationDto.getStatut());
            
            Adjudication createdAdjudication = adjudicationService.createAdjudication(adjudication);
            AdjudicationDto dto = AdjudicationDto.fromEntity(createdAdjudication);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error creating adjudication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Adjudication mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "404", description = "Adjudication non trouvée")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AdjudicationDto> updateAdjudication(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long id,
            @Valid @RequestBody AdjudicationDto adjudicationDto) {
        
        log.info("Updating adjudication with ID: {}", id);
        
        try {
            // Convert DTO to entity
            Adjudication adjudication = new Adjudication();
            adjudication.setNumeroAdjud(adjudicationDto.getNumeroAdjud());
            adjudication.setDateAdjud(adjudicationDto.getDateAdjud());
            adjudication.setMontantTotal(adjudicationDto.getMontantTotal());
            adjudication.setStatut(adjudicationDto.getStatut());
            
            Adjudication updatedAdjudication = adjudicationService.updateAdjudication(id, adjudication);
            AdjudicationDto dto = AdjudicationDto.fromEntity(updatedAdjudication);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error updating adjudication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une adjudication par ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Adjudication trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "404", description = "Adjudication non trouvée")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AdjudicationDto> getAdjudicationById(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long id) {
        
        try {
            Adjudication adjudication = adjudicationService.getAdjudicationById(id);
            AdjudicationDto dto = AdjudicationDto.fromEntity(adjudication);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/numero/{numeroAdjud}")
    @Operation(summary = "Récupérer une adjudication par numéro")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AdjudicationDto> getAdjudicationByNumber(
            @Parameter(description = "Numéro de l'adjudication") @PathVariable String numeroAdjud) {
        
        Optional<Adjudication> adjudication = adjudicationService.getAdjudicationByNumber(numeroAdjud);
        return adjudication.map(a -> ResponseEntity.ok(AdjudicationDto.fromEntity(a)))
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Récupérer toutes les adjudications avec pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Page<AdjudicationDto>> getAllAdjudications(Pageable pageable) {
        Page<Adjudication> adjudications = adjudicationService.getAllAdjudications(pageable);
        Page<AdjudicationDto> dtoPage = adjudications.map(AdjudicationDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Récupérer toutes les adjudications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getAllAdjudications() {
        List<Adjudication> adjudications = adjudicationService.getAllAdjudications();
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Adjudication supprimée avec succès"),
        @ApiResponse(responseCode = "400", description = "Impossible de supprimer l'adjudication"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "404", description = "Adjudication non trouvée")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Void> deleteAdjudication(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long id) {
        
        log.info("Deleting adjudication with ID: {}", id);
        
        try {
            adjudicationService.deleteAdjudication(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting adjudication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status/{statut}")
    @Operation(summary = "Récupérer les adjudications par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getAdjudicationsByStatus(
            @Parameter(description = "Statut de l'adjudication") @PathVariable Adjudication.StatutAdjudication statut) {
        
        List<Adjudication> adjudications = adjudicationService.getAdjudicationsByStatus(statut);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Récupérer les adjudications actives")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getActiveAdjudications() {
        List<Adjudication> adjudications = adjudicationService.getActiveAdjudications();
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/closed")
    @Operation(summary = "Récupérer les adjudications clôturées")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getClosedAdjudications() {
        List<Adjudication> adjudications = adjudicationService.getClosedAdjudications();
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/cancelled")
    @Operation(summary = "Récupérer les adjudications annulées")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getCancelledAdjudications() {
        List<Adjudication> adjudications = adjudicationService.getCancelledAdjudications();
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Récupérer les adjudications par période")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getAdjudicationsByDateRange(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Adjudication> adjudications = adjudicationService.getAdjudicationsByDateRange(startDate, endDate);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/amount-range")
    @Operation(summary = "Récupérer les adjudications par montant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getAdjudicationsByAmountRange(
            @Parameter(description = "Montant minimum") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Montant maximum") @RequestParam BigDecimal maxAmount) {
        
        List<Adjudication> adjudications = adjudicationService.getAdjudicationsByAmountRange(minAmount, maxAmount);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Rechercher des adjudications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> searchAdjudications(
            @Parameter(description = "Terme de recherche") @RequestParam String searchTerm) {
        
        List<Adjudication> adjudications = adjudicationService.searchAdjudications(searchTerm);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @PatchMapping("/{id}/close")
    @Operation(summary = "Clôturer une adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Adjudication clôturée avec succès"),
        @ApiResponse(responseCode = "400", description = "Impossible de clôturer l'adjudication"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "404", description = "Adjudication non trouvée")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AdjudicationDto> closeAdjudication(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long id) {
        
        log.info("Closing adjudication with ID: {}", id);
        
        try {
            Adjudication closedAdjudication = adjudicationService.closeAdjudication(id);
            AdjudicationDto dto = AdjudicationDto.fromEntity(closedAdjudication);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error closing adjudication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Adjudication annulée avec succès"),
        @ApiResponse(responseCode = "400", description = "Impossible d'annuler l'adjudication"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "404", description = "Adjudication non trouvée")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdjudicationDto> cancelAdjudication(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long id,
            @Parameter(description = "Motif d'annulation") @RequestParam String motifAnnulation) {
        
        log.info("Cancelling adjudication with ID: {}", id);
        
        try {
            Adjudication cancelledAdjudication = adjudicationService.cancelAdjudication(id, motifAnnulation);
            AdjudicationDto dto = AdjudicationDto.fromEntity(cancelledAdjudication);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error cancelling adjudication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/statistics/status")
    @Operation(summary = "Statistiques par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getAdjudicationStatisticsByStatus() {
        List<Object[]> statistics = adjudicationService.getAdjudicationStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/monthly")
    @Operation(summary = "Rapport mensuel des adjudications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getMonthlyAdjudicationReport(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Object[]> report = adjudicationService.getMonthlyAdjudicationReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/count/status/{statut}")
    @Operation(summary = "Compter les adjudications par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Long> getCountByStatus(
            @Parameter(description = "Statut de l'adjudication") @PathVariable Adjudication.StatutAdjudication statut) {
        
        Long count = adjudicationService.getCountByStatus(statut);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/total-amount/status/{statut}")
    @Operation(summary = "Montant total par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<BigDecimal> getTotalAmountByStatus(
            @Parameter(description = "Statut de l'adjudication") @PathVariable Adjudication.StatutAdjudication statut) {
        
        BigDecimal totalAmount = adjudicationService.getTotalAmountByStatus(statut);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/large")
    @Operation(summary = "Adjudications importantes sur une période")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getLargeAdjudicationsInPeriod(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Montant minimum") @RequestParam BigDecimal minAmount) {
        
        List<Adjudication> adjudications = adjudicationService.getLargeAdjudicationsInPeriod(startDate, endDate, minAmount);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/recent/{limit}")
    @Operation(summary = "Adjudications récentes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AdjudicationDto>> getRecentAdjudications(
            @Parameter(description = "Nombre limite") @PathVariable int limit) {
        
        List<Adjudication> adjudications = adjudicationService.getRecentAdjudications(limit);
        List<AdjudicationDto> dtoList = adjudications.stream()
                .map(AdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}