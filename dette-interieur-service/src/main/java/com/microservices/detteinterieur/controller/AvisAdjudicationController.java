package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.entity.AvisAdjudication;
import com.microservices.detteinterieur.dto.AvisAdjudicationDto;
import java.util.stream.Collectors;
import com.microservices.detteinterieur.service.AvisAdjudicationService;
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
@RequestMapping("/api/dette-interieur/avis-adjudications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Avis Adjudication", description = "API pour la gestion des avis d'adjudication")
@SecurityRequirement(name = "bearerAuth")
public class AvisAdjudicationController {
    
    private final AvisAdjudicationService avisAdjudicationService;
    
    @PostMapping
    @Operation(summary = "Créer un nouvel avis d'adjudication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Avis créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès interdit"),
        @ApiResponse(responseCode = "409", description = "Numéro d'avis déjà existant")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> createAvis(@Valid @RequestBody AvisAdjudicationDto avisDto) {
        log.info("Creating new auction advice: {}", avisDto.getNumeroAvis());
        
        try {
            // Convert DTO to entity
            AvisAdjudication avis = new AvisAdjudication();
            avis.setTypeAvis(avisDto.getTypeAvis());
            avis.setNumeroAvis(avisDto.getNumeroAvis());
            avis.setDateReception(avisDto.getDateReception());
            avis.setMontant(avisDto.getMontant());
            avis.setEmetteur(avisDto.getEmetteur());
            avis.setStatut(avisDto.getStatut());
            avis.setCommentaire(avisDto.getCommentaire());
            
            AvisAdjudication createdAvis = avisAdjudicationService.createAvis(avis);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(createdAvis);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error creating auction advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un avis d'adjudication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> updateAvis(
            @Parameter(description = "ID de l'avis") @PathVariable Long id,
            @Valid @RequestBody AvisAdjudicationDto avisDto) {
        
        try {
            // Convert DTO to entity
            AvisAdjudication avis = new AvisAdjudication();
            avis.setTypeAvis(avisDto.getTypeAvis());
            avis.setNumeroAvis(avisDto.getNumeroAvis());
            avis.setDateReception(avisDto.getDateReception());
            avis.setMontant(avisDto.getMontant());
            avis.setEmetteur(avisDto.getEmetteur());
            avis.setStatut(avisDto.getStatut());
            avis.setCommentaire(avisDto.getCommentaire());
            
            AvisAdjudication updatedAvis = avisAdjudicationService.updateAvis(id, avis);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(updatedAvis);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error updating auction advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un avis par ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> getAvisById(
            @Parameter(description = "ID de l'avis") @PathVariable Long id) {
        
        try {
            AvisAdjudication avis = avisAdjudicationService.getAvisById(id);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(avis);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/numero/{numeroAvis}")
    @Operation(summary = "Récupérer un avis par numéro")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> getAvisByNumber(
            @Parameter(description = "Numéro de l'avis") @PathVariable String numeroAvis) {
        
        Optional<AvisAdjudication> avis = avisAdjudicationService.getAvisByNumber(numeroAvis);
        return avis.map(a -> ResponseEntity.ok(AvisAdjudicationDto.fromEntity(a)))
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Récupérer tous les avis avec pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Page<AvisAdjudicationDto>> getAllAvis(Pageable pageable) {
        Page<AvisAdjudication> avis = avisAdjudicationService.getAllAvis(pageable);
        Page<AvisAdjudicationDto> dtoPage = avis.map(AvisAdjudicationDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avis d'adjudication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Void> deleteAvis(
            @Parameter(description = "ID de l'avis") @PathVariable Long id) {
        
        try {
            avisAdjudicationService.deleteAvis(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting auction advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/adjudication/{adjudicationId}")
    @Operation(summary = "Récupérer les avis par ID d'adjudication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByAuctionId(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long adjudicationId) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByAuctionId(adjudicationId);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/adjudication/numero/{numeroAdjud}")
    @Operation(summary = "Récupérer les avis par numéro d'adjudication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByAuctionNumber(
            @Parameter(description = "Numéro de l'adjudication") @PathVariable String numeroAdjud) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByAuctionNumber(numeroAdjud);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/credit")
    @Operation(summary = "Récupérer les avis de crédit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getCreditAdvices() {
        List<AvisAdjudication> avis = avisAdjudicationService.getCreditAdvices();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/debit")
    @Operation(summary = "Récupérer les avis de débit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getDebitAdvices() {
        List<AvisAdjudication> avis = avisAdjudicationService.getDebitAdvices();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/pending")
    @Operation(summary = "Récupérer les avis en attente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getPendingAdvices() {
        List<AvisAdjudication> avis = avisAdjudicationService.getPendingAdvices();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/accounted")
    @Operation(summary = "Récupérer les avis comptabilisés")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAccountedAdvices() {
        List<AvisAdjudication> avis = avisAdjudicationService.getAccountedAdvices();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/{statut}")
    @Operation(summary = "Récupérer les avis par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByStatus(
            @Parameter(description = "Statut de l'avis") @PathVariable AvisAdjudication.StatutAvis statut) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByStatus(statut);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/{typeAvis}")
    @Operation(summary = "Récupérer les avis par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByType(
            @Parameter(description = "Type de l'avis") @PathVariable AvisAdjudication.TypeAvis typeAvis) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByType(typeAvis);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/issuer/bam")
    @Operation(summary = "Récupérer les avis de BAM")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAdvicesFromBAM() {
        List<AvisAdjudication> avis = avisAdjudicationService.getAdvicesFromBAM();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/issuer/maroclear")
    @Operation(summary = "Récupérer les avis de Maroclear")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAdvicesFromMaroclear() {
        List<AvisAdjudication> avis = avisAdjudicationService.getAdvicesFromMaroclear();
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/issuer/{emetteur}")
    @Operation(summary = "Récupérer les avis par émetteur")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByIssuer(
            @Parameter(description = "Émetteur") @PathVariable String emetteur) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByIssuer(emetteur);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Récupérer les avis par période")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByDateRange(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByDateRange(startDate, endDate);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/amount-range")
    @Operation(summary = "Récupérer les avis par montant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> getAvisByAmountRange(
            @Parameter(description = "Montant minimum") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Montant maximum") @RequestParam BigDecimal maxAmount) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.getAvisByAmountRange(minAmount, maxAmount);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Rechercher des avis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<AvisAdjudicationDto>> searchAdvices(
            @Parameter(description = "Terme de recherche") @RequestParam String searchTerm) {
        
        List<AvisAdjudication> avis = avisAdjudicationService.searchAdvices(searchTerm);
        List<AvisAdjudicationDto> dtoList = avis.stream()
                .map(AvisAdjudicationDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @PatchMapping("/{id}/process")
    @Operation(summary = "Traiter un avis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> processAdvice(
            @Parameter(description = "ID de l'avis") @PathVariable Long id) {
        
        try {
            AvisAdjudication processedAvis = avisAdjudicationService.processAdvice(id);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(processedAvis);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error processing advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/account")
    @Operation(summary = "Comptabiliser un avis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> accountAdvice(
            @Parameter(description = "ID de l'avis") @PathVariable Long id) {
        
        try {
            AvisAdjudication accountedAvis = avisAdjudicationService.accountAdvice(id);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(accountedAvis);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error accounting advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rejeter un avis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<AvisAdjudicationDto> rejectAdvice(
            @Parameter(description = "ID de l'avis") @PathVariable Long id,
            @Parameter(description = "Motif de rejet") @RequestParam String motifRejet) {
        
        try {
            AvisAdjudication rejectedAvis = avisAdjudicationService.rejectAdvice(id, motifRejet);
            AvisAdjudicationDto dto = AvisAdjudicationDto.fromEntity(rejectedAvis);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Error rejecting advice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/statistics/type")
    @Operation(summary = "Statistiques par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getAdviceStatisticsByType() {
        List<Object[]> statistics = avisAdjudicationService.getAdviceStatisticsByType();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/status")
    @Operation(summary = "Statistiques par statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getAdviceStatisticsByStatus() {
        List<Object[]> statistics = avisAdjudicationService.getAdviceStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/financial-summary/adjudication/{adjudicationId}")
    @Operation(summary = "Résumé financier par adjudication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Object[]> getFinancialSummaryByAuction(
            @Parameter(description = "ID de l'adjudication") @PathVariable Long adjudicationId) {
        
        Object[] summary = avisAdjudicationService.getFinancialSummaryByAuction(adjudicationId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/financial-summary/overall")
    @Operation(summary = "Résumé financier global")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Object[]> getOverallFinancialSummary() {
        Object[] summary = avisAdjudicationService.getOverallFinancialSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/reports/monthly")
    @Operation(summary = "Rapport mensuel des avis")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<List<Object[]>> getMonthlyAdviceReport(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Object[]> report = avisAdjudicationService.getMonthlyAdviceReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/count/type/{typeAvis}/status/{statut}")
    @Operation(summary = "Compter par type et statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<Long> getCountByTypeAndStatus(
            @Parameter(description = "Type d'avis") @PathVariable AvisAdjudication.TypeAvis typeAvis,
            @Parameter(description = "Statut") @PathVariable AvisAdjudication.StatutAvis statut) {
        
        Long count = avisAdjudicationService.getCountByTypeAndStatus(typeAvis, statut);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/total-amount/type/{typeAvis}")
    @Operation(summary = "Montant total comptabilisé par type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    public ResponseEntity<BigDecimal> getTotalAccountedAmountByType(
            @Parameter(description = "Type d'avis") @PathVariable AvisAdjudication.TypeAvis typeAvis) {
        
        BigDecimal totalAmount = avisAdjudicationService.getTotalAccountedAmountByType(typeAvis);
        return ResponseEntity.ok(totalAmount);
    }
}