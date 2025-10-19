package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.EcheancierDTO;
import com.microservices.dettetresor.service.EcheancierService;
import com.microservices.dettetresor.validation.CreateValidationGroup;
import com.microservices.dettetresor.validation.UpdateValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/echeanciers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Payment Schedules", description = "Payment schedules management operations")
public class EcheancierController {
    
    private final EcheancierService echeancierService;
    
    @Operation(summary = "Create a new payment schedule", description = "Create a new payment schedule")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<EcheancierDTO> createEcheancier(
            @Parameter(description = "Payment schedule data") @Validated(CreateValidationGroup.class) @RequestBody EcheancierDTO echeancierDTO) {
        log.info("Creating new payment schedule for loan ID: {}", echeancierDTO.getPretId());
        EcheancierDTO createdEcheancier = echeancierService.create(echeancierDTO);
        return new ResponseEntity<>(createdEcheancier, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing payment schedule", description = "Update an existing payment schedule")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<EcheancierDTO> updateEcheancier(
            @Parameter(description = "Payment schedule ID") @PathVariable Long id,
            @Parameter(description = "Payment schedule data") @Validated(UpdateValidationGroup.class) @RequestBody EcheancierDTO echeancierDTO) {
        log.info("Updating payment schedule ID: {} with data: {}", id, echeancierDTO);
        try {
            EcheancierDTO updatedEcheancier = echeancierService.update(id, echeancierDTO);
            return ResponseEntity.ok(updatedEcheancier);
        } catch (Exception e) {
            log.error("Error updating payment schedule ID: {}", id, e);
            throw e;
        }
    }
    
    @Operation(summary = "Delete a payment schedule", description = "Delete a payment schedule")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<Void> deleteEcheancier(
            @Parameter(description = "Payment schedule ID") @PathVariable Long id) {
        log.info("Deleting payment schedule ID: {}", id);
        echeancierService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get all payment schedules", description = "Retrieve all payment schedules")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> getAllEcheanciers() {
        log.info("Retrieving all payment schedules");
        List<EcheancierDTO> echeanciers = echeancierService.findAll();
        return ResponseEntity.ok(echeanciers);
    }
    
    @Operation(summary = "Get payment schedules by loan ID", description = "Retrieve all payment schedules for a specific loan")
    @GetMapping("/pret/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> getEcheanciersByLoanId(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        log.info("Retrieving payment schedules for loan ID: {}", loanId);
        List<EcheancierDTO> echeanciers = echeancierService.findByLoanId(loanId);
        return ResponseEntity.ok(echeanciers);
    }
    
    @Operation(summary = "Get payment schedule by ID", description = "Retrieve a specific payment schedule by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<EcheancierDTO> getEcheancierById(
            @Parameter(description = "Payment schedule ID") @PathVariable Long id) {
        log.info("Retrieving payment schedule with ID: {}", id);
        EcheancierDTO echeancier = echeancierService.findById(id);
        return ResponseEntity.ok(echeancier);
    }
    
    @Operation(summary = "Search payment schedules by criteria", description = "Search payment schedules by various criteria")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> searchEcheanciers(
            @Parameter(description = "Payment number") @RequestParam(required = false) Integer numeroEcheance,
            @Parameter(description = "Loan ID") @RequestParam(required = false) Long pretId,
            @Parameter(description = "Status") @RequestParam(required = false) String statut) {
        log.info("Searching payment schedules with parameters: numeroEcheance={}, pretId={}, statut={}", numeroEcheance, pretId, statut);
        
        Map<String, String> filters = new HashMap<>();
        if (numeroEcheance != null) {
            filters.put("numeroEcheance", numeroEcheance.toString());
        }
        if (pretId != null) {
            filters.put("pretId", pretId.toString());
        }
        if (statut != null && !statut.trim().isEmpty()) {
            filters.put("statut", statut.trim());
        }
        
        try {
            List<EcheancierDTO> echeanciers = echeancierService.searchEcheanciers(filters);
            log.info("Found {} payment schedules matching filters", echeanciers.size());
            return ResponseEntity.ok(echeanciers);
        } catch (Exception e) {
            log.error("Error searching payment schedules with filters: {}", filters, e);
            throw e;
        }
    }
    
    @Operation(summary = "Get payment schedules by status", description = "Retrieve payment schedules by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> getEcheanciersByStatus(
            @Parameter(description = "Payment schedule status") @PathVariable String status) {
        log.info("Retrieving payment schedules with status: {}", status);
        List<EcheancierDTO> echeanciers = echeancierService.findByStatus(status);
        return ResponseEntity.ok(echeanciers);
    }
    
    @Operation(summary = "Get overdue payment schedules", description = "Retrieve overdue payment schedules")
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> getOverdueEcheanciers() {
        log.info("Retrieving overdue payment schedules");
        List<EcheancierDTO> echeanciers = echeancierService.findOverdueEcheanciers();
        return ResponseEntity.ok(echeanciers);
    }
    
    @Operation(summary = "Get payment schedules due within date range", description = "Retrieve payment schedules due within a specific date range")
    @GetMapping("/due-between")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<List<EcheancierDTO>> getEcheanciersDueBetween(
            @Parameter(description = "Start date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Retrieving payment schedules due between {} and {}", startDate, endDate);
        List<EcheancierDTO> echeanciers = echeancierService.findEcheanciersDueBetween(startDate, endDate);
        return ResponseEntity.ok(echeanciers);
    }
    
    @Operation(summary = "Update payment schedule status", description = "Update the status of a payment schedule")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    public ResponseEntity<EcheancierDTO> updateEcheancierStatus(
            @Parameter(description = "Payment schedule ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status) {
        log.info("Updating status of payment schedule ID: {} to {}", id, status);
        EcheancierDTO updatedEcheancier = echeancierService.updateStatus(id, status);
        return ResponseEntity.ok(updatedEcheancier);
    }
}