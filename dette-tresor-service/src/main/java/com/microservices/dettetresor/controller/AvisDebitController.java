package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.AvisDebitDTO;
import com.microservices.dettetresor.service.AvisDebitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/avis-debits")
@RequiredArgsConstructor
@Tag(name = "Debit Advices Management", description = "Operations related to debit advices")
public class AvisDebitController {

    private final AvisDebitService avisDebitService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Create a new debit advice")
    public ResponseEntity<AvisDebitDTO> createAvisDebit(@Valid @RequestBody AvisDebitDTO avisDebitDTO) {
        AvisDebitDTO createdAvisDebit = avisDebitService.createAvisDebit(avisDebitDTO);
        return new ResponseEntity<>(createdAvisDebit, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Update an existing debit advice")
    public ResponseEntity<AvisDebitDTO> updateAvisDebit(
            @PathVariable Long id,
            @Valid @RequestBody AvisDebitDTO avisDebitDTO) {
        AvisDebitDTO updatedAvisDebit = avisDebitService.updateAvisDebit(id, avisDebitDTO);
        return ResponseEntity.ok(updatedAvisDebit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get debit advice by ID")
    public ResponseEntity<AvisDebitDTO> getAvisDebitById(@PathVariable Long id) {
        AvisDebitDTO avisDebit = avisDebitService.getAvisDebitById(id);
        return ResponseEntity.ok(avisDebit);
    }

    @GetMapping("/numero/{numeroAvis}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get debit advice by advice number")
    public ResponseEntity<AvisDebitDTO> getAvisDebitByNumero(@PathVariable String numeroAvis) {
        AvisDebitDTO avisDebit = avisDebitService.getAvisDebitByNumero(numeroAvis);
        return ResponseEntity.ok(avisDebit);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all debit advices")
    public ResponseEntity<List<AvisDebitDTO>> getAllAvisDebits() {
        List<AvisDebitDTO> avisDebits = avisDebitService.getAllAvisDebits();
        return ResponseEntity.ok(avisDebits);
    }

    @GetMapping("/pret/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all debit advices for a specific loan")
    public ResponseEntity<List<AvisDebitDTO>> getAvisDebitsByPretId(@PathVariable Long pretId) {
        List<AvisDebitDTO> avisDebits = avisDebitService.getAvisDebitsByPretId(pretId);
        return ResponseEntity.ok(avisDebits);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Delete a debit advice")
    public ResponseEntity<Void> deleteAvisDebit(@PathVariable Long id) {
        avisDebitService.deleteAvisDebit(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Search debit advices by criteria")
    public ResponseEntity<List<AvisDebitDTO>> searchAvisDebits(@RequestParam Map<String, String> searchParams) {
        List<AvisDebitDTO> avisDebits = avisDebitService.searchAvisDebits(searchParams);
        return ResponseEntity.ok(avisDebits);
    }
}