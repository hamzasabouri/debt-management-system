package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.OrdrePaiementDTO;
import com.microservices.dettetresor.service.OrdrePaiementService;
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
@RequestMapping("/dette-tresor/ordres-paiement")
@RequiredArgsConstructor
@Tag(name = "Payment Orders Management", description = "Operations related to payment orders")
public class OrdrePaiementController {

    private final OrdrePaiementService ordrePaiementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Create a new payment order")
    public ResponseEntity<OrdrePaiementDTO> createOrdrePaiement(@Valid @RequestBody OrdrePaiementDTO ordrePaiementDTO) {
        OrdrePaiementDTO createdOrdre = ordrePaiementService.createOrdrePaiement(ordrePaiementDTO);
        return new ResponseEntity<>(createdOrdre, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Update an existing payment order")
    public ResponseEntity<OrdrePaiementDTO> updateOrdrePaiement(
            @PathVariable Long id,
            @Valid @RequestBody OrdrePaiementDTO ordrePaiementDTO) {
        OrdrePaiementDTO updatedOrdre = ordrePaiementService.updateOrdrePaiement(id, ordrePaiementDTO);
        return ResponseEntity.ok(updatedOrdre);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get payment order by ID")
    public ResponseEntity<OrdrePaiementDTO> getOrdrePaiementById(@PathVariable Long id) {
        OrdrePaiementDTO ordre = ordrePaiementService.getOrdrePaiementById(id);
        return ResponseEntity.ok(ordre);
    }

    @GetMapping("/numero/{numeroOrdre}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get payment order by order number")
    public ResponseEntity<OrdrePaiementDTO> getOrdrePaiementByNumero(@PathVariable String numeroOrdre) {
        OrdrePaiementDTO ordre = ordrePaiementService.getOrdrePaiementByNumero(numeroOrdre);
        return ResponseEntity.ok(ordre);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all payment orders")
    public ResponseEntity<List<OrdrePaiementDTO>> getAllOrdrePaiements() {
        List<OrdrePaiementDTO> ordres = ordrePaiementService.getAllOrdrePaiements();
        return ResponseEntity.ok(ordres);
    }

    @GetMapping("/pret/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all payment orders for a specific loan")
    public ResponseEntity<List<OrdrePaiementDTO>> getOrdrePaiementsByPretId(@PathVariable Long pretId) {
        List<OrdrePaiementDTO> ordres = ordrePaiementService.getOrdrePaiementsByPretId(pretId);
        return ResponseEntity.ok(ordres);
    }

    @GetMapping("/lettre-reglement/{lettreReglementId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all payment orders for a specific payment letter")
    public ResponseEntity<List<OrdrePaiementDTO>> getOrdrePaiementsByLettreReglementId(
            @PathVariable Long lettreReglementId) {
        List<OrdrePaiementDTO> ordres = ordrePaiementService.getOrdrePaiementsByLettreReglementId(lettreReglementId);
        return ResponseEntity.ok(ordres);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Delete a payment order")
    public ResponseEntity<Void> deleteOrdrePaiement(@PathVariable Long id) {
        ordrePaiementService.deleteOrdrePaiement(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Search payment orders by criteria")
    public ResponseEntity<List<OrdrePaiementDTO>> searchOrdrePaiements(@RequestParam Map<String, String> searchParams) {
        List<OrdrePaiementDTO> orders = ordrePaiementService.searchOrdrePaiements(searchParams);
        return ResponseEntity.ok(orders);
    }
}