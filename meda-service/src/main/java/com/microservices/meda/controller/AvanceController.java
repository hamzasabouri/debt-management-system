package com.microservices.meda.controller;

import com.microservices.meda.entity.Avance;
import com.microservices.meda.entity.Avance.TypeAvance;
import com.microservices.meda.entity.Avance.StatutAvance;
import com.microservices.meda.service.AvanceService;
import com.microservices.meda.dto.ApiResponse;
import com.microservices.meda.dto.AvanceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meda/avances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Avance Management", description = "MEDA Advance management operations")
@SecurityRequirement(name = "bearerAuth")
public class AvanceController {
    
    private final AvanceService avanceService;
    
    @PostMapping
    @Operation(summary = "Create advance", description = "Create a new advance (credit notice)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> createAvance(@Valid @RequestBody AvanceDTO avanceDto) {
        log.info("Creating new advance: {}", avanceDto.getNumeroAvance());
        try {
            Avance createdAvance = avanceService.createAvanceFromDto(avanceDto);
            return ResponseEntity.ok(ApiResponse.success(createdAvance, "Advance created successfully"));
        } catch (Exception e) {
            log.error("Error creating advance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/bam")
    @Operation(summary = "Process advance from BAM", description = "Process advance received from BAM system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> processAdvanceFromBAM(@Valid @RequestBody Avance avance) {
        log.info("Processing advance from BAM: {}", avance.getNumeroAvance());
        try {
            Avance processedAvance = avanceService.processAdvanceFromBAM(avance);
            return ResponseEntity.ok(ApiResponse.success(processedAvance, "Advance from BAM processed successfully"));
        } catch (Exception e) {
            log.error("Error processing advance from BAM: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update advance", description = "Update an existing advance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> updateAvance(
            @Parameter(description = "Advance ID") @PathVariable Long id,
            @Valid @RequestBody AvanceDTO avanceDto) {
        log.info("Updating advance with ID: {}", id);
        try {
            Avance updatedAvance = avanceService.updateAvanceFromDto(id, avanceDto);
            return ResponseEntity.ok(ApiResponse.success(updatedAvance, "Advance updated successfully"));
        } catch (Exception e) {
            log.error("Error updating advance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update advance status", description = "Update the status of an advance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> updateAvanceStatus(
            @Parameter(description = "Advance ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam StatutAvance status) {
        log.info("Updating advance status for ID: {} to {}", id, status);
        try {
            Avance updatedAvance = avanceService.updateAvanceStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(updatedAvance, "Advance status updated successfully"));
        } catch (Exception e) {
            log.error("Error updating advance status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get advance by ID", description = "Retrieve a specific advance by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> getAvanceById(
            @Parameter(description = "Advance ID") @PathVariable Long id) {
        log.info("Retrieving advance with ID: {}", id);
        try {
            Avance avance = avanceService.getAvanceById(id);
            return ResponseEntity.ok(ApiResponse.success(avance));
        } catch (Exception e) {
            log.error("Error retrieving advance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/number/{numeroAvance}")
    @Operation(summary = "Get advance by number", description = "Retrieve an advance by its number")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Avance>> getAvanceByNumber(
            @Parameter(description = "Advance number") @PathVariable String numeroAvance) {
        log.info("Retrieving advance with number: {}", numeroAvance);
        try {
            return avanceService.getAvanceByNumber(numeroAvance)
                    .map(avance -> ResponseEntity.ok(ApiResponse.success(avance)))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Advance not found")));
        } catch (Exception e) {
            log.error("Error retrieving advance by number: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all advances", description = "Retrieve all advances with pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Page<Avance>>> getAllAvances(Pageable pageable) {
        log.info("Retrieving all advances with pagination");
        try {
            Page<Avance> avances = avanceService.getAllAvances(pageable);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/project/{projetId}")
    @Operation(summary = "Get advances by project", description = "Retrieve advances for a specific project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getAvancesByProjet(
            @Parameter(description = "Project ID") @PathVariable Long projetId) {
        log.info("Retrieving advances for project ID: {}", projetId);
        try {
            List<Avance> avances = avanceService.getAvancesByProjet(projetId);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances by project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/project/{projetId}/type/{typeAvance}")
    @Operation(summary = "Get advances by project and type", description = "Retrieve advances by project and type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getAvancesByProjetAndType(
            @Parameter(description = "Project ID") @PathVariable Long projetId,
            @Parameter(description = "Advance type") @PathVariable TypeAvance typeAvance) {
        log.info("Retrieving advances for project ID: {} and type: {}", projetId, typeAvance);
        try {
            List<Avance> avances = avanceService.getAvancesByProjetAndType(projetId, typeAvance);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances by project and type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/type/{typeAvance}")
    @Operation(summary = "Get advances by type", description = "Retrieve advances by type (DON/PRET)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getAvancesByType(
            @Parameter(description = "Advance type") @PathVariable TypeAvance typeAvance) {
        log.info("Retrieving advances by type: {}", typeAvance);
        try {
            List<Avance> avances = avanceService.getAvancesByType(typeAvance);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances by type: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/status/{statut}")
    @Operation(summary = "Get advances by status", description = "Retrieve advances by status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getAvancesByStatus(
            @Parameter(description = "Advance status") @PathVariable StatutAvance statut) {
        log.info("Retrieving advances by status: {}", statut);
        try {
            List<Avance> avances = avanceService.getAvancesByStatus(statut);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances by status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/pending-accounting")
    @Operation(summary = "Get advances pending accounting", description = "Retrieve advances pending accounting")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getAdvancesPendingAccounting() {
        log.info("Retrieving advances pending accounting");
        try {
            List<Avance> avances = avanceService.getAdvancesPendingAccounting();
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving advances pending accounting: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent advances", description = "Retrieve recent advances within specified days")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Avance>>> getRecentAvances(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("Retrieving recent advances within {} days", days);
        try {
            List<Avance> avances = avanceService.getRecentAvances(days);
            return ResponseEntity.ok(ApiResponse.success(avances));
        } catch (Exception e) {
            log.error("Error retrieving recent advances: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete advance", description = "Delete an advance by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Void>> deleteAvance(
            @Parameter(description = "Advance ID") @PathVariable Long id) {
        log.info("Deleting advance with ID: {}", id);
        try {
            avanceService.deleteAvance(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Advance deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting advance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}