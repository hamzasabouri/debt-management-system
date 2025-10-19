package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.LettreReglementDTO;
import com.microservices.dettetresor.dto.OrdrePaiementDTO;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.service.DatabaseCleanupService;
import com.microservices.dettetresor.service.LettreReglementService;
import com.microservices.dettetresor.service.OrdrePaiementService;
import com.microservices.dettetresor.service.pdf.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/lettres-reglement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Letters Management", description = "Operations related to payment letters")
public class LettreReglementController {

    private final LettreReglementService lettreReglementService;
    private final OrdrePaiementService ordrePaiementService;
    private final PdfGenerationService pdfGenerationService;
    private final DatabaseCleanupService databaseCleanupService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Create a new payment letter")
    public ResponseEntity<LettreReglementDTO> createLettreReglement(@Valid @RequestBody LettreReglementDTO lettreReglementDTO) {
        LettreReglementDTO createdLettre = lettreReglementService.createLettreReglement(lettreReglementDTO);
        return new ResponseEntity<>(createdLettre, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Update an existing payment letter")
    public ResponseEntity<LettreReglementDTO> updateLettreReglement(
            @PathVariable Long id,
            @Valid @RequestBody LettreReglementDTO lettreReglementDTO) {
        LettreReglementDTO updatedLettre = lettreReglementService.updateLettreReglement(id, lettreReglementDTO);
        return ResponseEntity.ok(updatedLettre);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get payment letter by ID")
    public ResponseEntity<LettreReglementDTO> getLettreReglementById(@PathVariable Long id) {
        LettreReglementDTO lettre = lettreReglementService.getLettreReglementById(id);
        return ResponseEntity.ok(lettre);
    }

    @GetMapping("/numero/{numeroLettre}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get payment letter by letter number")
    public ResponseEntity<LettreReglementDTO> getLettreReglementByNumero(@PathVariable String numeroLettre) {
        LettreReglementDTO lettre = lettreReglementService.getLettreReglementByNumero(numeroLettre);
        return ResponseEntity.ok(lettre);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all payment letters")
    public ResponseEntity<List<LettreReglementDTO>> getAllLettreReglements() {
        List<LettreReglementDTO> lettres = lettreReglementService.getAllLettreReglements();
        return ResponseEntity.ok(lettres);
    }

    @GetMapping("/pret/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get all payment letters for a specific loan")
    public ResponseEntity<List<LettreReglementDTO>> getLettreReglementsByPretId(@PathVariable Long pretId) {
        List<LettreReglementDTO> lettres = lettreReglementService.getLettreReglementsByPretId(pretId);
        return ResponseEntity.ok(lettres);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Delete a payment letter")
    public ResponseEntity<Void> deleteLettreReglement(@PathVariable Long id) {
        lettreReglementService.deleteLettreReglement(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Search payment letters by criteria")
    public ResponseEntity<List<LettreReglementDTO>> searchLettreReglements(@RequestParam Map<String, String> searchParams) {
        List<LettreReglementDTO> letters = lettreReglementService.searchLettreReglements(searchParams);
        return ResponseEntity.ok(letters);
    }
    
    @GetMapping("/{id}/pdf")
@PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
@Operation(
    summary = "Generate PDF for a settlement letter",
    description = "Generates a PDF for a settlement letter and returns it as a downloadable file",
    responses = {
        @ApiResponse(responseCode = "200", description = "PDF generated successfully", content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "Settlement letter not found"),
        @ApiResponse(responseCode = "500", description = "Error generating PDF")
    }
)
public ResponseEntity<byte[]> generatePdf(
        @Parameter(description = "Settlement letter ID") @PathVariable Long id,
        @Parameter(description = "View mode (true for inline view, false for download)") 
        @RequestParam(required = false, defaultValue = "false") boolean viewMode) {
    
    try {
        LettreReglementDTO lettreReglement = lettreReglementService.getLettreReglementById(id);
        
        // Get associated payment order
        OrdrePaiementDTO ordrePaiement = null;
        if (lettreReglement.getOrdrePaiementId() != null) {
            try {
                ordrePaiement = ordrePaiementService.getOrdrePaiementById(lettreReglement.getOrdrePaiementId());
            } catch (Exception e) {
                log.warn("Payment order with ID {} not found for settlement letter {}: {}", 
                    lettreReglement.getOrdrePaiementId(), id, e.getMessage());
                // Try to find any available payment order to use as a fallback
                List<OrdrePaiementDTO> availableOrders = new ArrayList<>();
                try {
                    availableOrders = ordrePaiementService.getOrdrePaiementsByPretId(lettreReglement.getPretId());
                } catch (Exception ex) {
                    log.warn("Could not retrieve payment orders for loan ID {}: {}", lettreReglement.getPretId(), ex.getMessage());
                }
                
                if (!availableOrders.isEmpty()) {
                    ordrePaiement = availableOrders.get(0);
                    log.info("Using fallback payment order {} for settlement letter {}", ordrePaiement.getId(), id);
                    // Update the settlement letter with the found payment order
                    try {
                        lettreReglement.setOrdrePaiementId(ordrePaiement.getId());
                        lettreReglementService.updateLettreReglement(id, lettreReglement);
                    } catch (Exception ex) {
                        log.warn("Could not update settlement letter {} with payment order {}: {}", 
                            id, ordrePaiement.getId(), ex.getMessage());
                    }
                }
            }
        } else {
            // If no payment order is associated, try to find any available payment order to use as a fallback
            log.warn("Settlement letter with ID {} has no associated payment order", id);
            List<OrdrePaiementDTO> availableOrders = new ArrayList<>();
            try {
                availableOrders = ordrePaiementService.getOrdrePaiementsByPretId(lettreReglement.getPretId());
            } catch (Exception e) {
                log.warn("Could not retrieve payment orders for loan ID {}: {}", lettreReglement.getPretId(), e.getMessage());
            }
            
            if (!availableOrders.isEmpty()) {
                ordrePaiement = availableOrders.get(0);
                log.info("Using fallback payment order {} for settlement letter {}", ordrePaiement.getId(), id);
                // Update the settlement letter with the found payment order
                try {
                    lettreReglement.setOrdrePaiementId(ordrePaiement.getId());
                    lettreReglementService.updateLettreReglement(id, lettreReglement);
                } catch (Exception e) {
                    log.warn("Could not update settlement letter {} with payment order {}: {}", 
                        id, ordrePaiement.getId(), e.getMessage());
                }
            }
        }
        
        if (ordrePaiement == null) {
            log.warn("Settlement letter with ID {} has no associated payment order and no fallback available", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("No payment order associated with this settlement letter. Please associate a payment order with this letter before generating the PDF.").getBytes());
        }
        
        // Generate PDF
        byte[] pdfBytes = pdfGenerationService.generateLettreReglementPdfBytes(lettreReglement, ordrePaiement);
        
        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        String filename = String.format("lettre_reglement_%s.pdf", lettreReglement.getNumeroLettre());
        
        // Set header based on view mode
        if (viewMode) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
        } else {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        
    } catch (Exception e) {
        log.error("Error generating PDF for settlement letter ID {}: {}", id, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generating PDF: " + e.getMessage()).getBytes());
    }
}

    @PostMapping("/cleanup-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clean up duplicate settlement letters in the database")
    public ResponseEntity<String> cleanupDuplicateSettlementLetters() {
        try {
            databaseCleanupService.cleanupDuplicateSettlementLetters();
            return ResponseEntity.ok("Database cleanup for settlement letters completed successfully");
        } catch (Exception e) {
            log.error("Error during settlement letter database cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during settlement letter database cleanup: " + e.getMessage());
        }
    }
    
    @PostMapping("/cleanup-payment-order-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clean up duplicate payment orders in the database")
    public ResponseEntity<String> cleanupDuplicatePaymentOrders() {
        try {
            databaseCleanupService.cleanupDuplicatePaymentOrders();
            return ResponseEntity.ok("Database cleanup for payment orders completed successfully");
        } catch (Exception e) {
            log.error("Error during payment order database cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during payment order database cleanup: " + e.getMessage());
        }
    }
    
    @PostMapping("/cleanup-all-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clean up all duplicate records in the database")
    public ResponseEntity<String> cleanupAllDuplicates() {
        try {
            databaseCleanupService.cleanupAllDuplicates();
            return ResponseEntity.ok("Database cleanup for all duplicate records completed successfully");
        } catch (Exception e) {
            log.error("Error during database cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during database cleanup: " + e.getMessage());
        }
    }
    
    @GetMapping("/has-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if there are duplicate settlement letters in the database")
    public ResponseEntity<Boolean> hasDuplicateSettlementLetters() {
        boolean hasDuplicates = databaseCleanupService.hasDuplicateSettlementLetters();
        return ResponseEntity.ok(hasDuplicates);
    }
    
    @GetMapping("/has-payment-order-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if there are duplicate payment orders in the database")
    public ResponseEntity<Boolean> hasDuplicatePaymentOrders() {
        boolean hasDuplicates = databaseCleanupService.hasDuplicatePaymentOrders();
        return ResponseEntity.ok(hasDuplicates);
    }
    
    @GetMapping("/has-any-duplicates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if there are any duplicate records in the database")
    public ResponseEntity<Boolean> hasAnyDuplicates() {
        boolean hasDuplicates = databaseCleanupService.hasAnyDuplicates();
        return ResponseEntity.ok(hasDuplicates);
    }

}