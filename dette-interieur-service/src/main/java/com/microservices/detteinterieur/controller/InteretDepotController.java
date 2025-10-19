package com.microservices.detteinterieur.controller;

import com.microservices.detteinterieur.dto.InteretDepotDto;
import com.microservices.detteinterieur.entity.InteretDepot;
import com.microservices.detteinterieur.service.InteretDepotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dette-interieur/interets-depot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deposit Interests", description = "Deposit interest management operations")
public class InteretDepotController {
    
    private final InteretDepotService interetDepotService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Create a new interest calculation")
    public ResponseEntity<InteretDepotDto> createInteret(
            @Valid @RequestBody InteretDepotDto interetDto) {
        log.info("Creating new interest calculation for account: {}", interetDto.getNumeroCompte());
        InteretDepot interet = interetDto.toEntity();
        InteretDepot created = interetDepotService.createInteret(interet);
        InteretDepotDto createdDto = InteretDepotDto.fromEntity(created);
        return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Update an interest calculation")
    public ResponseEntity<InteretDepotDto> updateInteret(
            @PathVariable Long id,
            @Valid @RequestBody InteretDepotDto interetDto) {
        log.info("Updating interest calculation with ID: {}", id);
        InteretDepot interet = interetDto.toEntity();
        InteretDepot updated = interetDepotService.updateInteret(id, interet);
        InteretDepotDto updatedDto = InteretDepotDto.fromEntity(updated);
        return ResponseEntity.ok(updatedDto);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculation by ID")
    public ResponseEntity<InteretDepotDto> getInteretById(@PathVariable Long id) {
        InteretDepot interet = interetDepotService.getInteretById(id);
        InteretDepotDto interetDto = InteretDepotDto.fromEntity(interet);
        return ResponseEntity.ok(interetDto);
    }
    
    @GetMapping("/account/{numeroCompte}/date/{dateCalcul}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculation by account and date")
    public ResponseEntity<InteretDepotDto> getInteretByAccountAndDate(
            @PathVariable String numeroCompte,
            @PathVariable LocalDate dateCalcul) {
        Optional<InteretDepot> interet = interetDepotService.getInteretByAccountAndDate(numeroCompte, dateCalcul);
        return interet.map(i -> ResponseEntity.ok(InteretDepotDto.fromEntity(i)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all interest calculations with pagination")
    public ResponseEntity<Page<InteretDepotDto>> getAllInterets(Pageable pageable) {
        Page<InteretDepot> interets = interetDepotService.getAllInterets(pageable);
        List<InteretDepotDto> interetDtos = interets.getContent().stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        Page<InteretDepotDto> interetDtoPage = new PageImpl<>(interetDtos, pageable, interets.getTotalElements());
        return ResponseEntity.ok(interetDtoPage);
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get all interest calculations")
    public ResponseEntity<List<InteretDepotDto>> getAllInterets() {
        List<InteretDepot> interets = interetDepotService.getAllInterets();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an interest calculation")
    public ResponseEntity<Void> deleteInteret(@PathVariable Long id) {
        log.info("Deleting interest calculation with ID: {}", id);
        interetDepotService.deleteInteret(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/account/{numeroCompte}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by account")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByAccount(
            @PathVariable String numeroCompte) {
        List<InteretDepot> interets = interetDepotService.getInteretsByAccount(numeroCompte);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/fund-type/{typeFonds}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by fund type")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByFundType(
            @PathVariable InteretDepot.TypeFonds typeFonds) {
        List<InteretDepot> interets = interetDepotService.getInteretsByFundType(typeFonds);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/local-collectivities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get local collectivities interests")
    public ResponseEntity<List<InteretDepotDto>> getLocalCollectivitiesInterests() {
        List<InteretDepot> interets = interetDepotService.getLocalCollectivitiesInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/treasury-deposits")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get treasury deposits interests")
    public ResponseEntity<List<InteretDepotDto>> getTreasuryDepositsInterests() {
        List<InteretDepot> interets = interetDepotService.getTreasuryDepositsInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by status")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByStatus(
            @PathVariable InteretDepot.StatutInteret statut) {
        List<InteretDepot> interets = interetDepotService.getInteretsByStatus(statut);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/calculated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get calculated interests")
    public ResponseEntity<List<InteretDepotDto>> getCalculatedInterests() {
        List<InteretDepot> interets = interetDepotService.getCalculatedInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get pending interests")
    public ResponseEntity<List<InteretDepotDto>> getPendingInterests() {
        List<InteretDepot> interets = interetDepotService.getPendingInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/accounted")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get accounted interests")
    public ResponseEntity<List<InteretDepotDto>> getAccountedInterests() {
        List<InteretDepot> interets = interetDepotService.getAccountedInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/transmitted")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get transmitted interests")
    public ResponseEntity<List<InteretDepotDto>> getTransmittedInterests() {
        List<InteretDepot> interets = interetDepotService.getTransmittedInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/rejected")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get rejected interests")
    public ResponseEntity<List<InteretDepotDto>> getRejectedInterests() {
        List<InteretDepot> interets = interetDepotService.getRejectedInterests();
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by date range")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<InteretDepot> interets = interetDepotService.getInteretsByDateRange(startDate, endDate);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by amount range")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByAmountRange(
            @RequestParam @Parameter(description = "Minimum amount") BigDecimal minAmount,
            @RequestParam @Parameter(description = "Maximum amount") BigDecimal maxAmount) {
        List<InteretDepot> interets = interetDepotService.getInteretsByAmountRange(minAmount, maxAmount);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/currency/{devise}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get interest calculations by currency")
    public ResponseEntity<List<InteretDepotDto>> getInteretsByCurrency(
            @PathVariable String devise) {
        List<InteretDepot> interets = interetDepotService.getInteretsByCurrency(devise);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Search interest calculations")
    public ResponseEntity<List<InteretDepotDto>> searchInterets(
            @RequestParam @Parameter(description = "Search term") String searchTerm) {
        List<InteretDepot> interets = interetDepotService.searchInterets(searchTerm);
        List<InteretDepotDto> interetDtos = interets.stream()
                .map(InteretDepotDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(interetDtos);
    }
    
    @PatchMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Process an interest calculation")
    public ResponseEntity<InteretDepotDto> processInteret(@PathVariable Long id) {
        try {
            InteretDepot processedInteret = interetDepotService.processInteret(id);
            InteretDepotDto interetDto = InteretDepotDto.fromEntity(processedInteret);
            return ResponseEntity.ok(interetDto);
        } catch (IllegalArgumentException e) {
            log.error("Error processing interest: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/account")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Account an interest calculation")
    public ResponseEntity<InteretDepotDto> accountInteret(@PathVariable Long id) {
        try {
            InteretDepot accountedInteret = interetDepotService.accountInteret(id);
            InteretDepotDto interetDto = InteretDepotDto.fromEntity(accountedInteret);
            return ResponseEntity.ok(interetDto);
        } catch (IllegalArgumentException e) {
            log.error("Error accounting interest: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/transmit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Transmit an interest calculation")
    public ResponseEntity<InteretDepotDto> transmitInteret(@PathVariable Long id) {
        try {
            InteretDepot transmittedInteret = interetDepotService.transmitInteret(id);
            InteretDepotDto interetDto = InteretDepotDto.fromEntity(transmittedInteret);
            return ResponseEntity.ok(interetDto);
        } catch (IllegalArgumentException e) {
            log.error("Error transmitting interest: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Reject an interest calculation")
    public ResponseEntity<InteretDepotDto> rejectInteret(
            @PathVariable Long id,
            @RequestParam @Parameter(description = "Rejection reason") String motifRejet) {
        try {
            InteretDepot rejectedInteret = interetDepotService.rejectInteret(id, motifRejet);
            InteretDepotDto interetDto = InteretDepotDto.fromEntity(rejectedInteret);
            return ResponseEntity.ok(interetDto);
        } catch (IllegalArgumentException e) {
            log.error("Error rejecting interest: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/statistics/fund-type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get statistics by fund type")
    public ResponseEntity<List<Object[]>> getInterestStatisticsByFundType() {
        List<Object[]> statistics = interetDepotService.getInterestStatisticsByFundType();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get statistics by status")
    public ResponseEntity<List<Object[]>> getInterestStatisticsByStatus() {
        List<Object[]> statistics = interetDepotService.getInterestStatisticsByStatus();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics/currency")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get statistics by currency")
    public ResponseEntity<List<Object[]>> getInterestStatisticsByCurrency() {
        List<Object[]> statistics = interetDepotService.getInterestStatisticsByCurrency();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/total-amount/fund-type/{typeFonds}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total amount by fund type")
    public ResponseEntity<BigDecimal> getTotalAmountByFundType(
            @PathVariable InteretDepot.TypeFonds typeFonds) {
        BigDecimal totalAmount = interetDepotService.getTotalAmountByFundType(typeFonds);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/total-amount/status/{statut}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total amount by status")
    public ResponseEntity<BigDecimal> getTotalAmountByStatus(
            @PathVariable InteretDepot.StatutInteret statut) {
        BigDecimal totalAmount = interetDepotService.getTotalAmountByStatus(statut);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/total-amount/currency/{devise}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get total amount by currency")
    public ResponseEntity<BigDecimal> getTotalAmountByCurrency(
            @PathVariable String devise) {
        BigDecimal totalAmount = interetDepotService.getTotalAmountByCurrency(devise);
        return ResponseEntity.ok(totalAmount);
    }
}