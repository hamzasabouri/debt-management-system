package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.entity.BonEquipement;
import com.microservices.detteinterieur.dto.BonEquipementDto;
import java.util.stream.Collectors;
import com.microservices.detteinterieur.service.BonEquipementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dette-interieur/bon-equipements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Bonds", description = "Equipment bonds management operations")
public class BonEquipementController {
    
    private final BonEquipementService bonEquipementService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Create a new equipment bond")
    public ResponseEntity<BonEquipementDto> createBonEquipement(
            @Valid @RequestBody BonEquipementDto bonEquipementDto) {
        log.info("Creating new equipment bond: {}", bonEquipementDto.getNumeroBon());
        
        // Convert DTO to entity
        BonEquipement bonEquipement = new BonEquipement();
        bonEquipement.setNumeroBon(bonEquipementDto.getNumeroBon());
        bonEquipement.setDateSouscription(bonEquipementDto.getDateSouscription());
        bonEquipement.setMontant(bonEquipementDto.getMontant());
        bonEquipement.setStatut(bonEquipementDto.getStatut());
        bonEquipement.setDateEcheance(bonEquipementDto.getDateEcheance());
        bonEquipement.setTauxInteret(bonEquipementDto.getTauxInteret());
        bonEquipement.setSouscripteur(bonEquipementDto.getSouscripteur());
        bonEquipement.setCommentaire(bonEquipementDto.getCommentaire());
        bonEquipement.setDateRemboursement(bonEquipementDto.getDateRemboursement());
        
        BonEquipement created = bonEquipementService.createBon(bonEquipement);
        BonEquipementDto dto = BonEquipementDto.fromEntity(created);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Update an equipment bond")
    public ResponseEntity<BonEquipementDto> updateBonEquipement(
            @PathVariable Long id,
            @Valid @RequestBody BonEquipementDto bonEquipementDto) {
        log.info("Updating equipment bond with ID: {}", id);
        
        // Convert DTO to entity
        BonEquipement bonEquipement = new BonEquipement();
        bonEquipement.setNumeroBon(bonEquipementDto.getNumeroBon());
        bonEquipement.setDateSouscription(bonEquipementDto.getDateSouscription());
        bonEquipement.setMontant(bonEquipementDto.getMontant());
        bonEquipement.setStatut(bonEquipementDto.getStatut());
        bonEquipement.setDateEcheance(bonEquipementDto.getDateEcheance());
        bonEquipement.setTauxInteret(bonEquipementDto.getTauxInteret());
        bonEquipement.setSouscripteur(bonEquipementDto.getSouscripteur());
        bonEquipement.setCommentaire(bonEquipementDto.getCommentaire());
        bonEquipement.setDateRemboursement(bonEquipementDto.getDateRemboursement());
        
        BonEquipement updated = bonEquipementService.updateBon(id, bonEquipement);
        BonEquipementDto dto = BonEquipementDto.fromEntity(updated);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bond by ID")
    public ResponseEntity<BonEquipementDto> getBonEquipementById(@PathVariable Long id) {
        BonEquipement bonEquipement = bonEquipementService.getBonById(id);
        BonEquipementDto dto = BonEquipementDto.fromEntity(bonEquipement);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/numero/{numeroBon}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bond by bond number")
    public ResponseEntity<BonEquipementDto> getBonEquipementByNumber(
            @PathVariable String numeroBon) {
        BonEquipement bonEquipement = bonEquipementService.getBonByNumber(numeroBon).orElseThrow(() -> new RuntimeException("Bond not found"));
        BonEquipementDto dto = BonEquipementDto.fromEntity(bonEquipement);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all equipment bonds with pagination")
    public ResponseEntity<Page<BonEquipementDto>> getAllBonEquipements(Pageable pageable) {
        Page<BonEquipement> bonEquipements = bonEquipementService.getAllBons(pageable);
        Page<BonEquipementDto> dtoPage = bonEquipements.map(BonEquipementDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> getAllBonEquipements() {
        List<BonEquipement> bonEquipements = bonEquipementService.getAllBons();
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Delete an equipment bond")
    public ResponseEntity<Void> deleteBonEquipement(@PathVariable Long id) {
        log.info("Deleting equipment bond with ID: {}", id);
        bonEquipementService.deleteBon(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bonds by status")
    public ResponseEntity<List<BonEquipementDto>> getBonEquipementsByStatus(
            @PathVariable BonEquipement.StatutBon statut) {
        List<BonEquipement> bonEquipements = bonEquipementService.getBonsByStatus(statut);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/subscriber/{souscripteur}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bonds by subscriber")
    public ResponseEntity<List<BonEquipementDto>> getBonEquipementsBySubscriber(
            @PathVariable String souscripteur) {
        // Placeholder - need to implement filtering by subscriber in service
        List<BonEquipement> bonEquipements = bonEquipementService.searchBonds(souscripteur);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bonds by subscription date range")
    public ResponseEntity<List<BonEquipementDto>> getBonEquipementsByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<BonEquipement> bonEquipements = bonEquipementService.getBonsByDateRange(startDate, endDate);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bonds by amount range")
    public ResponseEntity<List<BonEquipementDto>> getBonEquipementsByAmountRange(
            @RequestParam @Parameter(description = "Minimum amount") BigDecimal minAmount,
            @RequestParam @Parameter(description = "Maximum amount") BigDecimal maxAmount) {
        List<BonEquipement> bonEquipements = bonEquipementService.getBonsByAmountRange(minAmount, maxAmount);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get expiring equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> getExpiringBonEquipements(
            @RequestParam(defaultValue = "30") @Parameter(description = "Days until expiration") int days) {
        List<BonEquipement> bonEquipements = bonEquipementService.getBonsMaturingSoon(days);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get expired equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> getExpiredBonEquipements() {
        // Placeholder - need to implement filtering for expired bonds
        List<BonEquipement> bonEquipements = bonEquipementService.getBondsRequiringRenewal(LocalDate.now());
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get active equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> getActiveBonEquipements() {
        List<BonEquipement> bonEquipements = bonEquipementService.getActiveBonds();
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/reimbursed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get reimbursed equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> getReimbursedBonEquipements() {
        List<BonEquipement> bonEquipements = bonEquipementService.getReimbursedBonds();
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @PostMapping("/{id}/reimburse")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Reimburse an equipment bond")
    public ResponseEntity<BonEquipementDto> reimburseBonEquipement(@PathVariable Long id) {
        log.info("Reimbursing equipment bond with ID: {}", id);
        BonEquipement reimbursed = bonEquipementService.reimburseBond(id);
        BonEquipementDto dto = BonEquipementDto.fromEntity(reimbursed);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Reject an equipment bond")
    public ResponseEntity<BonEquipementDto> rejectBonEquipement(
            @PathVariable Long id,
            @RequestParam String motifRejet) {
        log.info("Rejecting equipment bond with ID: {} with reason: {}", id, motifRejet);
        BonEquipement rejected = bonEquipementService.rejectBond(id, motifRejet);
        BonEquipementDto dto = BonEquipementDto.fromEntity(rejected);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Process an equipment bond")
    public ResponseEntity<BonEquipementDto> processBonEquipement(@PathVariable Long id) {
        log.info("Processing equipment bond with ID: {}", id);
        // Placeholder - no direct process method in service
        BonEquipement processed = bonEquipementService.getBonById(id);
        BonEquipementDto dto = BonEquipementDto.fromEntity(processed);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Search equipment bonds")
    public ResponseEntity<List<BonEquipementDto>> searchBonEquipements(
            @RequestParam String searchTerm) {
        List<BonEquipement> bonEquipements = bonEquipementService.searchBonds(searchTerm);
        List<BonEquipementDto> dtoList = bonEquipements.stream()
                .map(BonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/statistics/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get bond statistics by status")
    public ResponseEntity<List<Object[]>> getBondStatisticsByStatus() {
        List<Object[]> statistics = bonEquipementService.getBondStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/subscriber")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get bond statistics by subscriber")
    public ResponseEntity<List<Object[]>> getBondStatisticsBySubscriber() {
        // Placeholder - need to implement subscriber statistics
        List<Object[]> statistics = List.of();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get monthly bond statistics")
    public ResponseEntity<List<Object[]>> getMonthlyBondStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Object[]> statistics = bonEquipementService.getMonthlyBondReport(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/count/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get count by status")
    public ResponseEntity<Long> getCountByStatus(@PathVariable BonEquipement.StatutBon statut) {
        Long count = bonEquipementService.getCountByStatus(statut);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/total/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total active amount")
    public ResponseEntity<BigDecimal> getTotalActiveAmount() {
        BigDecimal total = bonEquipementService.getTotalActiveAmount();
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/total/reimbursed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total reimbursed amount")
    public ResponseEntity<BigDecimal> getTotalReimbursedAmount() {
        BigDecimal total = bonEquipementService.getTotalAmountByStatus(BonEquipement.StatutBon.REMBOURSE);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/average/interest-rate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get average interest rate")
    public ResponseEntity<BigDecimal> getAverageInterestRate() {
        // Placeholder - need to implement average interest rate calculation
        BigDecimal avgRate = BigDecimal.valueOf(2.5);
        return ResponseEntity.ok(avgRate);
    }
}