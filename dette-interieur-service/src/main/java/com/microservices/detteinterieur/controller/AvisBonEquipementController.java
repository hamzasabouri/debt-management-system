package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.entity.AvisBonEquipement;
import com.microservices.detteinterieur.dto.AvisBonEquipementDto;
import java.util.stream.Collectors;
import com.microservices.detteinterieur.service.AvisBonEquipementService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/dette-interieur/avis-bon-equipements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Bond Notices", description = "Equipment bond notices management operations")
public class AvisBonEquipementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AvisBonEquipementController.class);
    
    private final AvisBonEquipementService avisBonEquipementService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Create a new equipment bond notice")
    public ResponseEntity<AvisBonEquipementDto> createAvisBonEquipement(
            @Valid @RequestBody AvisBonEquipementDto avisBonEquipementDto) {
        log.info("Creating new equipment bond notice: {}", avisBonEquipementDto.getNumeroAvis());
        
        // Convert DTO to entity
        AvisBonEquipement avisBonEquipement = new AvisBonEquipement();
        avisBonEquipement.setTypeAvis(avisBonEquipementDto.getTypeAvis());
        avisBonEquipement.setNumeroAvis(avisBonEquipementDto.getNumeroAvis());
        avisBonEquipement.setDateReception(avisBonEquipementDto.getDateReception());
        avisBonEquipement.setMontant(avisBonEquipementDto.getMontant());
        avisBonEquipement.setEmetteur(avisBonEquipementDto.getEmetteur());
        avisBonEquipement.setStatut(avisBonEquipementDto.getStatut());
        avisBonEquipement.setMotifRejet(avisBonEquipementDto.getMotifRejet());
        avisBonEquipement.setCommentaire(avisBonEquipementDto.getCommentaire());
        
        AvisBonEquipement created = avisBonEquipementService.createAvis(avisBonEquipement);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(created);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Update an equipment bond notice")
    public ResponseEntity<AvisBonEquipementDto> updateAvisBonEquipement(
            @PathVariable Long id,
            @Valid @RequestBody AvisBonEquipementDto avisBonEquipementDto) {
        log.info("Updating equipment bond notice with ID: {}", id);
        
        // Convert DTO to entity
        AvisBonEquipement avisBonEquipement = new AvisBonEquipement();
        avisBonEquipement.setTypeAvis(avisBonEquipementDto.getTypeAvis());
        avisBonEquipement.setNumeroAvis(avisBonEquipementDto.getNumeroAvis());
        avisBonEquipement.setDateReception(avisBonEquipementDto.getDateReception());
        avisBonEquipement.setMontant(avisBonEquipementDto.getMontant());
        avisBonEquipement.setEmetteur(avisBonEquipementDto.getEmetteur());
        avisBonEquipement.setStatut(avisBonEquipementDto.getStatut());
        avisBonEquipement.setMotifRejet(avisBonEquipementDto.getMotifRejet());
        avisBonEquipement.setCommentaire(avisBonEquipementDto.getCommentaire());
        
        AvisBonEquipement updated = avisBonEquipementService.updateAvis(id, avisBonEquipement);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(updated);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bond notice by ID")
    public ResponseEntity<AvisBonEquipementDto> getAvisBonEquipementById(@PathVariable Long id) {
        AvisBonEquipement avisBonEquipement = avisBonEquipementService.getAvisById(id);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(avisBonEquipement);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/numero/{numeroAvis}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get equipment bond notice by notice number")
    public ResponseEntity<AvisBonEquipementDto> getAvisBonEquipementByNumber(
            @PathVariable String numeroAvis) {
        AvisBonEquipement avisBonEquipement = avisBonEquipementService.getAvisByNumber(numeroAvis).orElseThrow(() -> new RuntimeException("Advice not found"));
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(avisBonEquipement);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all equipment bond notices with pagination")
    public ResponseEntity<Page<AvisBonEquipementDto>> getAllAvisBonEquipements(Pageable pageable) {
        Page<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getAllAvis(pageable);
        Page<AvisBonEquipementDto> dtoPage = avisBonEquipements.map(AvisBonEquipementDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all equipment bond notices")
    public ResponseEntity<List<AvisBonEquipementDto>> getAllAvisBonEquipements() {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getAllAvis();
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an equipment bond notice")
    public ResponseEntity<Void> deleteAvisBonEquipement(@PathVariable Long id) {
        log.info("Deleting equipment bond notice with ID: {}", id);
        avisBonEquipementService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/bon-equipement/{bonEquipementId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get notices by equipment bond ID")
    public ResponseEntity<List<AvisBonEquipementDto>> getAvisByBonEquipementId(
            @PathVariable Long bonEquipementId) {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getAvisByBondId(bonEquipementId);
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/type/{typeAvis}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get notices by type")
    public ResponseEntity<List<AvisBonEquipementDto>> getAvisByType(
            @PathVariable AvisBonEquipement.TypeAvis typeAvis) {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getAvisByType(typeAvis);
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get notices by status")
    public ResponseEntity<List<AvisBonEquipementDto>> getAvisByStatus(
            @PathVariable AvisBonEquipement.StatutAvis statut) {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getAvisByStatus(statut);
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/credits")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get credit notices")
    public ResponseEntity<List<AvisBonEquipementDto>> getCreditNotices() {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getCreditAdvices();
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/debits")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get debit notices")
    public ResponseEntity<List<AvisBonEquipementDto>> getDebitNotices() {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getDebitAdvices();
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/rejections")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get rejection notices")
    public ResponseEntity<List<AvisBonEquipementDto>> getRejectionNotices() {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.getRejectionAdvices();
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Process an equipment bond notice")
    public ResponseEntity<AvisBonEquipementDto> processAvis(@PathVariable Long id) {
        log.info("Processing notice with ID: {}", id);
        AvisBonEquipement processed = avisBonEquipementService.processAdvice(id);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(processed);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{id}/account")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Account an equipment bond notice")
    public ResponseEntity<AvisBonEquipementDto> comptabiliserAvis(@PathVariable Long id) {
        log.info("Accounting notice with ID: {}", id);
        AvisBonEquipement accounted = avisBonEquipementService.accountAdvice(id);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(accounted);
        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Reject an equipment bond notice")
    public ResponseEntity<AvisBonEquipementDto> rejeterAvis(
            @PathVariable Long id,
            @RequestParam String motifRejet) {
        log.info("Rejecting notice with ID: {} with reason: {}", id, motifRejet);
        AvisBonEquipement rejected = avisBonEquipementService.rejectAdvice(id, motifRejet);
        AvisBonEquipementDto dto = AvisBonEquipementDto.fromEntity(rejected);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Search notices")
    public ResponseEntity<List<AvisBonEquipementDto>> searchNotices(
            @RequestParam String searchTerm) {
        List<AvisBonEquipement> avisBonEquipements = avisBonEquipementService.searchAdvices(searchTerm);
        List<AvisBonEquipementDto> dtoList = avisBonEquipements.stream()
                .map(AvisBonEquipementDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/statistics/type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get notice statistics by type")
    public ResponseEntity<List<Object[]>> getNoticeStatisticsByType() {
        List<Object[]> statistics = avisBonEquipementService.getAdviceStatisticsByType();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get notice statistics by status")
    public ResponseEntity<List<Object[]>> getNoticeStatisticsByStatus() {
        List<Object[]> statistics = avisBonEquipementService.getAdviceStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get monthly notice statistics")
    public ResponseEntity<List<Object[]>> getMonthlyNoticeStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Object[]> statistics = avisBonEquipementService.getMonthlyAdviceReport(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/count/type/{typeAvis}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get count by type")
    public ResponseEntity<Long> getCountByType(@PathVariable AvisBonEquipement.TypeAvis typeAvis) {
        Long count = (long) avisBonEquipementService.getAvisByType(typeAvis).size();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get count by status")
    public ResponseEntity<Long> getCountByStatus(@PathVariable AvisBonEquipement.StatutAvis statut) {
        Long count = (long) avisBonEquipementService.getAvisByStatus(statut).size();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/total/credits")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total credit amount")
    public ResponseEntity<BigDecimal> getTotalCreditAmount() {
        BigDecimal total = avisBonEquipementService.getTotalAccountedAmountByType(AvisBonEquipement.TypeAvis.CREDIT);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/total/debits")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total debit amount")
    public ResponseEntity<BigDecimal> getTotalDebitAmount() {
        BigDecimal total = avisBonEquipementService.getTotalAccountedAmountByType(AvisBonEquipement.TypeAvis.DEBIT);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/total/accounted")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total accounted amount")
    public ResponseEntity<BigDecimal> getTotalAccountedAmount() {
        Object[] summary = avisBonEquipementService.getOverallFinancialSummary();
        BigDecimal total = summary != null && summary.length > 0 ? (BigDecimal) summary[0] : BigDecimal.ZERO;
        return ResponseEntity.ok(total);
    }
}