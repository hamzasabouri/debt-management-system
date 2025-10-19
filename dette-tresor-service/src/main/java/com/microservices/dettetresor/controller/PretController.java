package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.PretDTO;
import com.microservices.dettetresor.service.PretService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dette-tresor/prets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Loans Management", description = "External loans management operations")
public class PretController {
    
    private final PretService pretService;
    
    @Operation(summary = "Create a new loan", description = "Create a new external loan contract")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> createLoan(@Valid @RequestBody PretDTO pretDTO) {
        log.info("Creating new loan with number: {}", pretDTO.getNumeroPret());
        PretDTO createdLoan = pretService.createLoan(pretDTO);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing loan", description = "Update loan information")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> updateLoan(
            @Parameter(description = "Loan ID") @PathVariable Long id,
            @Valid @RequestBody PretDTO pretDTO) {
        log.info("Updating loan with ID: {}", id);
        PretDTO updatedLoan = pretService.updateLoan(id, pretDTO);
        return ResponseEntity.ok(updatedLoan);
    }
    
    @Operation(summary = "Get loan by ID", description = "Retrieve loan details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> getLoanById(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        log.info("Retrieving loan with ID: {}", id);
        PretDTO loan = pretService.findById(id);
        return ResponseEntity.ok(loan);
    }
    
    @Operation(summary = "Get loan by ID with payment schedules", description = "Retrieve loan details by ID including payment schedules")
    @GetMapping("/{id}/with-echeanciers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> getLoanByIdWithEcheanciers(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        log.info("Retrieving loan with ID: {} including payment schedules", id);
        PretDTO loan = pretService.findByIdWithEcheanciers(id);
        return ResponseEntity.ok(loan);
    }
    
    @Operation(summary = "Get loan by loan number", description = "Retrieve loan details by loan number")
    @GetMapping("/numero/{numeroPret}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> getLoanByNumber(
            @Parameter(description = "Loan number") @PathVariable String numeroPret) {
        log.info("Retrieving loan with number: {}", numeroPret);
        PretDTO loan = pretService.findByLoanNumber(numeroPret);
        return ResponseEntity.ok(loan);
    }
    
    @Operation(summary = "Get all loans", description = "Retrieve all loans")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getAllLoans() {
        log.info("Retrieving all loans");
        List<PretDTO> loans = pretService.findAllLoans();
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Get active loans", description = "Retrieve loans with outstanding balance")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getActiveLoans() {
        log.info("Retrieving active loans");
        List<PretDTO> loans = pretService.findActiveLoans();
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Get fully paid loans", description = "Retrieve loans that are fully paid")
    @GetMapping("/paid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getFullyPaidLoans() {
        log.info("Retrieving fully paid loans");
        List<PretDTO> loans = pretService.findFullyPaidLoans();
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Get loans by lending organization", description = "Retrieve loans by lending organization")
    @GetMapping("/organisme/{organismeBailleur}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getLoansByLendingOrganization(
            @Parameter(description = "Lending organization") @PathVariable String organismeBailleur) {
        log.info("Retrieving loans by lending organization: {}", organismeBailleur);
        List<PretDTO> loans = pretService.findByLendingOrganization(organismeBailleur);
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Get loans by currency", description = "Retrieve loans by currency")
    @GetMapping("/devise/{devise}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getLoansByCurrency(
            @Parameter(description = "Currency") @PathVariable String devise) {
        log.info("Retrieving loans by currency: {}", devise);
        List<PretDTO> loans = pretService.findByCurrency(devise);
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Search loans", description = "Search loans by multiple criteria")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> searchLoans(
            @Parameter(description = "Loan number") @RequestParam(required = false) String numeroPret,
            @Parameter(description = "Lending organization") @RequestParam(required = false) String organismeBailleur,
            @Parameter(description = "Currency") @RequestParam(required = false) String devise) {
        log.info("Searching loans with criteria - Number: {}, Organization: {}, Currency: {}", 
                numeroPret, organismeBailleur, devise);
        List<PretDTO> loans = pretService.searchLoans(numeroPret, organismeBailleur, devise);
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Get loans by signature date range", description = "Retrieve loans by signature date range")
    @GetMapping("/signature-date")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<PretDTO>> getLoansBySignatureDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Retrieving loans by signature date range: {} to {}", startDate, endDate);
        List<PretDTO> loans = pretService.findBySignatureDateRange(startDate, endDate);
        return ResponseEntity.ok(loans);
    }
    
    @Operation(summary = "Update loan current balance", description = "Update the current outstanding balance of a loan")
    @PutMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> updateCurrentBalance(
            @Parameter(description = "Loan ID") @PathVariable Long id,
            @Parameter(description = "New balance amount") @RequestParam BigDecimal newBalance) {
        log.info("Updating current balance for loan ID: {} to {}", id, newBalance);
        PretDTO updatedLoan = pretService.updateCurrentBalance(id, newBalance);
        return ResponseEntity.ok(updatedLoan);
    }
    
    @Operation(summary = "Recalculate loan current balance", description = "Recalculate current balance based on payments")
    @PostMapping("/{id}/recalculate-balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<PretDTO> recalculateCurrentBalance(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        log.info("Recalculating current balance for loan ID: {}", id);
        PretDTO updatedLoan = pretService.recalculateCurrentBalance(id);
        return ResponseEntity.ok(updatedLoan);
    }
    
    @Operation(summary = "Delete loan", description = "Delete a loan and all associated data")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<Void> deleteLoan(
            @Parameter(description = "Loan ID") @PathVariable Long id) {
        log.info("Deleting loan with ID: {}", id);
        pretService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get total outstanding debt by currency", description = "Get statistics of total outstanding debt by currency")
    @GetMapping("/statistics/outstanding-debt")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<Object[]>> getTotalOutstandingDebtByCurrency() {
        log.info("Retrieving total outstanding debt by currency");
        List<Object[]> statistics = pretService.getTotalOutstandingDebtByCurrency();
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(summary = "Check if loan number exists", description = "Check if a loan number already exists")
    @GetMapping("/exists/{numeroPret}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<Boolean> checkLoanNumberExists(
            @Parameter(description = "Loan number") @PathVariable String numeroPret) {
        boolean exists = pretService.existsByLoanNumber(numeroPret);
        return ResponseEntity.ok(exists);
    }
}