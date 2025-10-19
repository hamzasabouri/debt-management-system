package com.microservices.meda.controller;

import com.microservices.meda.entity.PieceJustificative;
import com.microservices.meda.entity.PieceJustificative.TypePiece;
import com.microservices.meda.entity.PieceJustificative.StatutPiece;
import com.microservices.meda.service.PieceJustificativeService;
import com.microservices.meda.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/meda/pieces-justificatives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PieceJustificative Management", description = "Supporting document management operations")
@SecurityRequirement(name = "bearerAuth")
public class PieceJustificativeController {
    
    private final PieceJustificativeService pieceJustificativeService;
    
    @PostMapping
    @Operation(summary = "Create supporting document", description = "Create a new supporting document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> createPieceJustificative(@Valid @RequestBody PieceJustificative piece) {
        log.info("Creating new supporting document: {}", piece.getNumeroPiece());
        try {
            PieceJustificative createdPiece = pieceJustificativeService.createPieceJustificative(piece);
            return ResponseEntity.ok(ApiResponse.success(createdPiece, "Supporting document created successfully"));
        } catch (Exception e) {
            log.error("Error creating supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/comptabilite")
    @Operation(summary = "Process document from Comptabilite", description = "Process supporting document from Comptabilite system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> processPieceJustificativeFromComptabilite(@Valid @RequestBody PieceJustificative piece) {
        log.info("Processing supporting document from Comptabilite: {}", piece.getNumeroPiece());
        try {
            PieceJustificative processedPiece = pieceJustificativeService.processPieceJustificativeFromComptabilite(piece);
            return ResponseEntity.ok(ApiResponse.success(processedPiece, "Document from Comptabilite processed successfully"));
        } catch (Exception e) {
            log.error("Error processing document from Comptabilite: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update supporting document", description = "Update an existing supporting document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> updatePieceJustificative(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Valid @RequestBody PieceJustificative piece) {
        log.info("Updating supporting document with ID: {}", id);
        try {
            PieceJustificative updatedPiece = pieceJustificativeService.updatePieceJustificative(id, piece);
            return ResponseEntity.ok(ApiResponse.success(updatedPiece, "Supporting document updated successfully"));
        } catch (Exception e) {
            log.error("Error updating supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/validate")
    @Operation(summary = "Validate supporting document", description = "Validate a supporting document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> validatePieceJustificative(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Parameter(description = "Validation comment") @RequestParam(required = false) String commentaire) {
        log.info("Validating supporting document with ID: {}", id);
        try {
            PieceJustificative validatedPiece = pieceJustificativeService.validatePieceJustificative(id, commentaire);
            return ResponseEntity.ok(ApiResponse.success(validatedPiece, "Supporting document validated successfully"));
        } catch (Exception e) {
            log.error("Error validating supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject supporting document", description = "Reject a supporting document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> rejectPieceJustificative(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @Parameter(description = "Rejection comment") @RequestParam String commentaire) {
        log.info("Rejecting supporting document with ID: {}", id);
        try {
            PieceJustificative rejectedPiece = pieceJustificativeService.rejectPieceJustificative(id, commentaire);
            return ResponseEntity.ok(ApiResponse.success(rejectedPiece, "Supporting document rejected successfully"));
        } catch (Exception e) {
            log.error("Error rejecting supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get supporting document by ID", description = "Retrieve a specific supporting document by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> getPieceJustificativeById(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("Retrieving supporting document with ID: {}", id);
        try {
            PieceJustificative piece = pieceJustificativeService.getPieceJustificativeById(id);
            return ResponseEntity.ok(ApiResponse.success(piece));
        } catch (Exception e) {
            log.error("Error retrieving supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/number/{numeroPiece}")
    @Operation(summary = "Get supporting document by number", description = "Retrieve a supporting document by its number")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<PieceJustificative>> getPieceJustificativeByNumber(
            @Parameter(description = "Document number") @PathVariable String numeroPiece) {
        log.info("Retrieving supporting document with number: {}", numeroPiece);
        try {
            return pieceJustificativeService.getPieceJustificativeByNumber(numeroPiece)
                    .map(piece -> ResponseEntity.ok(ApiResponse.success(piece)))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Supporting document not found")));
        } catch (Exception e) {
            log.error("Error retrieving supporting document by number: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all supporting documents", description = "Retrieve all supporting documents with pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Page<PieceJustificative>>> getAllPiecesJustificatives(Pageable pageable) {
        log.info("Retrieving all supporting documents with pagination");
        try {
            Page<PieceJustificative> pieces = pieceJustificativeService.getAllPiecesJustificatives(pageable);
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving supporting documents: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/project/{projetId}")
    @Operation(summary = "Get supporting documents by project", description = "Retrieve supporting documents for a specific project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getPiecesJustificativesByProjet(
            @Parameter(description = "Project ID") @PathVariable Long projetId) {
        log.info("Retrieving supporting documents for project ID: {}", projetId);
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getPiecesJustificativesByProjet(projetId);
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving supporting documents by project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/project/{projetId}/available-funds")
    @Operation(summary = "Get available funds for project", description = "Calculate available funds for a project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateAvailableFunds(
            @Parameter(description = "Project ID") @PathVariable Long projetId) {
        log.info("Calculating available funds for project ID: {}", projetId);
        try {
            BigDecimal availableFunds = pieceJustificativeService.calculateAvailableFunds(projetId);
            return ResponseEntity.ok(ApiResponse.success(availableFunds));
        } catch (Exception e) {
            log.error("Error calculating available funds: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/type/{typePiece}")
    @Operation(summary = "Get supporting documents by type", description = "Retrieve supporting documents by type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getPiecesJustificativesByType(
            @Parameter(description = "Document type") @PathVariable TypePiece typePiece) {
        log.info("Retrieving supporting documents by type: {}", typePiece);
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getPiecesJustificativesByType(typePiece);
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving supporting documents by type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/status/{statut}")
    @Operation(summary = "Get supporting documents by status", description = "Retrieve supporting documents by status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getPiecesJustificativesByStatus(
            @Parameter(description = "Document status") @PathVariable StatutPiece statut) {
        log.info("Retrieving supporting documents by status: {}", statut);
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getPiecesJustificativesByStatus(statut);
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving supporting documents by status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/pending-validation")
    @Operation(summary = "Get documents pending validation", description = "Retrieve supporting documents pending validation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getPiecesJustificativesPendingValidation() {
        log.info("Retrieving supporting documents pending validation");
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getPiecesJustificativesPendingValidation();
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving documents pending validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/rejected")
    @Operation(summary = "Get rejected documents", description = "Retrieve rejected supporting documents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getRejectedPiecesJustificatives() {
        log.info("Retrieving rejected supporting documents");
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getRejectedPiecesJustificatives();
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving rejected documents: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent documents", description = "Retrieve recent supporting documents within specified days")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<PieceJustificative>>> getRecentPiecesJustificatives(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("Retrieving recent supporting documents within {} days", days);
        try {
            List<PieceJustificative> pieces = pieceJustificativeService.getRecentPiecesJustificatives(days);
            return ResponseEntity.ok(ApiResponse.success(pieces));
        } catch (Exception e) {
            log.error("Error retrieving recent documents: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supporting document", description = "Delete a supporting document by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Void>> deletePieceJustificative(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        log.info("Deleting supporting document with ID: {}", id);
        try {
            pieceJustificativeService.deletePieceJustificative(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Supporting document deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting supporting document: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}