package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.entity.Commission;
import com.microservices.detteinterieur.dto.CommissionDto;
import java.util.stream.Collectors;
import com.microservices.detteinterieur.service.CommissionService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/dette-interieur/commissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commission", description = "API pour la gestion des commissions Maroclear et BAM")
@SecurityRequirement(name = "bearerAuth")
public class CommissionController {
    
    private final CommissionService commissionService;
    
    // Helper method to convert Commission to CommissionDto
    private CommissionDto toDto(Commission commission) {
        return CommissionDto.fromEntity(commission);
    }
    
    @PostMapping
    @Operation(summary = "Créer une nouvelle commission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Commission créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "409", description = "Numéro de transaction déjà existant")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> createCommission(@Valid @RequestBody CommissionDto commissionDto) {
        log.info("Creating new commission: {}", commissionDto.getNumeroReference());
        
        try {
            // Convert DTO to entity
            Commission commission = new Commission();
            commission.setTypeCommission(commissionDto.getTypeCommission());
            commission.setOrdrePaiementId(commissionDto.getOrdrePaiementId());
            commission.setLettreReglementId(commissionDto.getLettreReglementId());
            commission.setAvisDebitId(commissionDto.getAvisDebitId());
            commission.setMontant(commissionDto.getMontant());
            commission.setStatut(commissionDto.getStatut());
            commission.setNumeroReference(commissionDto.getNumeroReference());
            commission.setDescription(commissionDto.getDescription());
            commission.setCommentaire(commissionDto.getCommentaire());
            commission.setDatePaiement(commissionDto.getDatePaiement());
            
            Commission createdCommission = commissionService.createCommission(commission);
            CommissionDto dto = CommissionDto.fromEntity(createdCommission);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error creating commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une commission")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> updateCommission(
            @Parameter(description = "ID de la commission") @PathVariable Long id,
            @Valid @RequestBody CommissionDto commissionDto) {
        
        try {
            // Convert DTO to entity
            Commission commission = new Commission();
            commission.setTypeCommission(commissionDto.getTypeCommission());
            commission.setOrdrePaiementId(commissionDto.getOrdrePaiementId());
            commission.setLettreReglementId(commissionDto.getLettreReglementId());
            commission.setAvisDebitId(commissionDto.getAvisDebitId());
            commission.setMontant(commissionDto.getMontant());
            commission.setStatut(commissionDto.getStatut());
            commission.setNumeroReference(commissionDto.getNumeroReference());
            commission.setDescription(commissionDto.getDescription());
            commission.setCommentaire(commissionDto.getCommentaire());
            commission.setDatePaiement(commissionDto.getDatePaiement());
            
            Commission updatedCommission = commissionService.updateCommission(id, commission);
            CommissionDto dto = CommissionDto.fromEntity(updatedCommission);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error updating commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une commission par ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> getCommissionById(
            @Parameter(description = "ID de la commission") @PathVariable Long id) {
        
        try {
            Commission commission = commissionService.getCommissionById(id);
            CommissionDto dto = CommissionDto.fromEntity(commission);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/reference/{numeroReference}")
    @Operation(summary = "Récupérer une commission par numéro de référence")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> getCommissionByReferenceNumber(
            @Parameter(description = "Numéro de référence") @PathVariable String numeroReference) {
        
        Optional<Commission> commission = commissionService.getCommissionByReferenceNumber(numeroReference);
        return commission.map(c -> ResponseEntity.ok(CommissionDto.fromEntity(c)))
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Récupérer toutes les commissions avec pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Page<CommissionDto>> getAllCommissions(Pageable pageable) {
        Page<Commission> commissions = commissionService.getAllCommissions(pageable);
        Page<CommissionDto> dtoPage = commissions.map(CommissionDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Récupérer toutes les commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getAllCommissions() {
        List<Commission> commissions = commissionService.getAllCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommission(
            @Parameter(description = "ID de la commission") @PathVariable Long id) {
        
        try {
            commissionService.deleteCommission(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/type/{typeCommission}")
    @Operation(summary = "Récupérer les commissions par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCommissionsByType(
            @Parameter(description = "Type de commission") @PathVariable Commission.TypeCommission typeCommission) {
        
        List<Commission> commissions = commissionService.getCommissionsByType(typeCommission);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/maroclear")
    @Operation(summary = "Récupérer les commissions Maroclear")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getMaroclearCommissions() {
        List<Commission> commissions = commissionService.getMaroclearCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/bam")
    @Operation(summary = "Récupérer les commissions BAM")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getBAMCommissions() {
        List<Commission> commissions = commissionService.getBAMCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/{statut}")
    @Operation(summary = "Récupérer les commissions par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCommissionsByStatus(
            @Parameter(description = "Statut de la commission") @PathVariable Commission.StatutCommission statut) {
        
        List<Commission> commissions = commissionService.getCommissionsByStatus(statut);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/calculated")
    @Operation(summary = "Récupérer les commissions calculées")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCalculatedCommissions() {
        List<Commission> commissions = commissionService.getCalculatedCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/pending")
    @Operation(summary = "Récupérer les commissions en attente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getPendingCommissions() {
        List<Commission> commissions = commissionService.getPendingCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/paid")
    @Operation(summary = "Récupérer les commissions payées")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getPaidCommissions() {
        List<Commission> commissions = commissionService.getPaidCommissions();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/to-pay")
    @Operation(summary = "Récupérer les commissions à payer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCommissionsToPay() {
        List<Commission> commissions = commissionService.getCommissionsToPay();
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Récupérer les commissions par période")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCommissionsByDateRange(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Commission> commissions = commissionService.getCommissionsByDateRange(startDate, endDate);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/amount-range")
    @Operation(summary = "Récupérer les commissions par montant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getCommissionsByAmountRange(
            @Parameter(description = "Montant minimum") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Montant maximum") @RequestParam BigDecimal maxAmount) {
        
        List<Commission> commissions = commissionService.getCommissionsByAmountRange(minAmount, maxAmount);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Rechercher des commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> searchCommissions(
            @Parameter(description = "Terme de recherche") @RequestParam String searchTerm) {
        
        List<Commission> commissions = commissionService.searchCommissions(searchTerm);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @PatchMapping("/{id}/process")
    @Operation(summary = "Traiter une commission")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> processCommission(
            @Parameter(description = "ID de la commission") @PathVariable Long id) {
        
        try {
            Commission processedCommission = commissionService.processCommission(id);
            CommissionDto dto = CommissionDto.fromEntity(processedCommission);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error processing commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/pay")
    @Operation(summary = "Payer une commission")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<CommissionDto> payCommission(
            @Parameter(description = "ID de la commission") @PathVariable Long id) {
        
        try {
            Commission paidCommission = commissionService.payCommission(id);
            CommissionDto dto = CommissionDto.fromEntity(paidCommission);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error paying commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une commission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommissionDto> cancelCommission(
            @Parameter(description = "ID de la commission") @PathVariable Long id,
            @Parameter(description = "Motif d'annulation") @RequestParam String motifAnnulation) {
        
        try {
            Commission cancelledCommission = commissionService.cancelCommission(id, motifAnnulation);
            CommissionDto dto = CommissionDto.fromEntity(cancelledCommission);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error cancelling commission: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/statistics/type")
    @Operation(summary = "Statistiques par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getCommissionStatisticsByType() {
        List<Object[]> statistics = commissionService.getCommissionStatisticsByType();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/status")
    @Operation(summary = "Statistiques par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getCommissionStatisticsByStatus() {
        List<Object[]> statistics = commissionService.getCommissionStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/reports/monthly")
    @Operation(summary = "Rapport mensuel des commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getMonthlyCommissionReport(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Object[]> report = commissionService.getMonthlyCommissionReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/performance")
    @Operation(summary = "Analyse de performance des commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getCommissionPerformanceAnalysis() {
        List<Object[]> analysis = commissionService.getCommissionPerformanceAnalysis();
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/count/type/{typeCommission}/status/{statut}")
    @Operation(summary = "Compter par type et statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Long> getCountByTypeAndStatus(
            @Parameter(description = "Type de commission") @PathVariable Commission.TypeCommission typeCommission,
            @Parameter(description = "Statut") @PathVariable Commission.StatutCommission statut) {
        
        Long count = commissionService.getCountByTypeAndStatus(typeCommission, statut);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/total-amount/type/{typeCommission}")
    @Operation(summary = "Montant total payé par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<BigDecimal> getTotalPaidAmountByType(
            @Parameter(description = "Type de commission") @PathVariable Commission.TypeCommission typeCommission) {
        
        BigDecimal totalAmount = commissionService.getTotalPaidAmountByType(typeCommission);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/total-pending")
    @Operation(summary = "Montant total en attente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        BigDecimal totalAmount = commissionService.getTotalPendingAmount();
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Commissions en retard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getOverdueCommissions(
            @Parameter(description = "Date seuil") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate threshold) {
        
        List<Commission> commissions = commissionService.getOverdueCommissions(threshold);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/large")
    @Operation(summary = "Commissions importantes sur une période")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<CommissionDto>> getLargeCommissionsInPeriod(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Montant minimum") @RequestParam BigDecimal minAmount) {
        
        List<Commission> commissions = commissionService.getLargeCommissionsInPeriod(startDate, endDate, minAmount);
        List<CommissionDto> dtoList = commissions.stream()
                .map(CommissionDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/average-rate/type/{typeCommission}")
    @Operation(summary = "Taux moyen par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<BigDecimal> getAverageCommissionRateByType(
            @Parameter(description = "Type de commission") @PathVariable Commission.TypeCommission typeCommission) {
        
        BigDecimal avgRate = commissionService.getAverageCommissionRateByType(typeCommission);
        return ResponseEntity.ok(avgRate);
    }
    
    @GetMapping("/processing-time")
    @Operation(summary = "Temps de traitement moyen par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getAverageProcessingTimeByType() {
        List<Object[]> processingTimes = commissionService.getAverageProcessingTimeByType();
        return ResponseEntity.ok(processingTimes);
    }
}