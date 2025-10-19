package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.LettreReglementDTO;
import com.microservices.dettetresor.dto.OrdrePaiementDTO;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.service.LettreReglementService;
import com.microservices.dettetresor.service.OrdrePaiementService;
import com.microservices.dettetresor.service.pdf.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dette-tresor/pdf")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PDF Generation", description = "Operations related to PDF generation and display")
public class PdfController {

    private final PdfGenerationService pdfGenerationService;
    private final LettreReglementService lettreReglementService;
    private final OrdrePaiementService ordrePaiementService;

    @GetMapping("/lettres-reglement/{id}")
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
    public ResponseEntity<byte[]> generateSettlementLetterPdf(
            @Parameter(description = "Settlement letter ID") @PathVariable Long id,
            @Parameter(description = "View mode (true for inline view, false for download)") @RequestParam(required = false, defaultValue = "false") boolean viewMode) {
    
        try {
            LettreReglementDTO lettreReglement = lettreReglementService.getLettreReglementById(id);
            
            // Get associated payment order
            OrdrePaiementDTO ordrePaiement = null;
            if (lettreReglement.getOrdrePaiementId() != null) {
                try {
                    ordrePaiement = ordrePaiementService.getOrdrePaiementById(lettreReglement.getOrdrePaiementId());
                } catch (ResourceNotFoundException e) {
                    log.warn("Payment order with ID {} not found for settlement letter {}", 
                        lettreReglement.getOrdrePaiementId(), id);
                    // Try to find any available payment order to use as a fallback
                    List<OrdrePaiementDTO> availableOrders = ordrePaiementService.getOrdrePaiementsByPretId(lettreReglement.getPretId());
                    if (!availableOrders.isEmpty()) {
                        ordrePaiement = availableOrders.get(0);
                        log.info("Using fallback payment order {} for settlement letter {}", ordrePaiement.getId(), id);
                        // Update the settlement letter with the found payment order
                        lettreReglement.setOrdrePaiementId(ordrePaiement.getId());
                        lettreReglementService.updateLettreReglement(id, lettreReglement);
                    }
                }
            } else {
                // If no payment order is associated, try to find any available payment order to use as a fallback
                log.warn("Settlement letter with ID {} has no associated payment order", id);
                List<OrdrePaiementDTO> availableOrders = ordrePaiementService.getOrdrePaiementsByPretId(lettreReglement.getPretId());
                if (!availableOrders.isEmpty()) {
                    ordrePaiement = availableOrders.get(0);
                    log.info("Using fallback payment order {} for settlement letter {}", ordrePaiement.getId(), id);
                    // Update the settlement letter with the found payment order
                    lettreReglement.setOrdrePaiementId(ordrePaiement.getId());
                    lettreReglementService.updateLettreReglement(id, lettreReglement);
                }
            }
            
            if (ordrePaiement == null) {
                log.warn("Settlement letter with ID {} has no associated payment order and no fallback available", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No payment order associated with this settlement letter".getBytes());
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
}