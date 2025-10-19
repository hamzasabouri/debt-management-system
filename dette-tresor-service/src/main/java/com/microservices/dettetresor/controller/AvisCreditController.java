package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.AvisCreditDTO;
import com.microservices.dettetresor.service.AvisCreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/avis-credits")
@Slf4j
@CrossOrigin(origins = "*")

@RequiredArgsConstructor
@Tag(name = "Credit Advices Management", description = "Operations related to credit advices")
public class AvisCreditController {

    private final AvisCreditService avisCreditService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Create a new credit advice")
    public ResponseEntity<AvisCreditDTO> createAvisCredit(@Valid @RequestBody AvisCreditDTO avisCreditDTO) {
        AvisCreditDTO createdAvisCredit = avisCreditService.createAvisCredit(avisCreditDTO);
        return new ResponseEntity<>(createdAvisCredit, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Update an existing credit advice")
    public ResponseEntity<AvisCreditDTO> updateAvisCredit(
            @PathVariable Long id,
            @Valid @RequestBody AvisCreditDTO avisCreditDTO) {
        AvisCreditDTO updatedAvisCredit = avisCreditService.updateAvisCredit(id, avisCreditDTO);
        return ResponseEntity.ok(updatedAvisCredit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get credit advice by ID")
    public ResponseEntity<AvisCreditDTO> getAvisCreditById(@PathVariable Long id) {
        AvisCreditDTO avisCredit = avisCreditService.getAvisCreditById(id);
        return ResponseEntity.ok(avisCredit);
    }

    @GetMapping("/numero/{numeroAvis}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get credit advice by advice number")
    public ResponseEntity<AvisCreditDTO> getAvisCreditByNumero(@PathVariable String numeroAvis) {
        AvisCreditDTO avisCredit = avisCreditService.getAvisCreditByNumero(numeroAvis);
        return ResponseEntity.ok(avisCredit);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all credit advices")
    public ResponseEntity<List<AvisCreditDTO>> getAllAvisCredits() {
        List<AvisCreditDTO> avisCredits = avisCreditService.getAllAvisCredits();
        return ResponseEntity.ok(avisCredits);
    }

    @GetMapping("/pret/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all credit advices for a specific loan")
    public ResponseEntity<List<AvisCreditDTO>> getAvisCreditsByPretId(@PathVariable Long pretId) {
        List<AvisCreditDTO> avisCredits = avisCreditService.getAvisCreditsByPretId(pretId);
        return ResponseEntity.ok(avisCredits);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Delete a credit advice")
    public ResponseEntity<Void> deleteAvisCredit(@PathVariable Long id) {
        avisCreditService.deleteAvisCredit(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Search credit advices by criteria")
    public ResponseEntity<List<AvisCreditDTO>> searchAvisCredits(@RequestParam Map<String, String> searchParams) {
        List<AvisCreditDTO> avisCredits = avisCreditService.searchAvisCredits(searchParams);
        return ResponseEntity.ok(avisCredits);
    }
}