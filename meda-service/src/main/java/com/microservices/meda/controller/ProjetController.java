package com.microservices.meda.controller;

import com.microservices.meda.entity.Projet;
import com.microservices.meda.service.ProjetService;
import com.microservices.meda.dto.ProjetDTO;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/meda/projets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projet Management", description = "MEDA Project management operations")
@SecurityRequirement(name = "bearerAuth")
public class ProjetController {
    
    private final ProjetService projetService;
    
    @PostMapping
    @Operation(summary = "Create a new project", description = "Create a new MEDA project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Projet>> createProjet(@Valid @RequestBody Projet projet) {
        log.info("=== CREATE PROJECT DEBUG INFO ===");
        log.info("Creating new project: {}", projet.getNomProjet());
        
        // Log authentication details for debugging
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Authentication details - Principal: {}, Authorities: {}", 
                    authentication.getPrincipal(), authentication.getAuthorities());
            
            // Check if user has required roles
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean hasAdminRole = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean hasMedaRole = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_MEDA"));
            
            log.info("User has ROLE_ADMIN: {}", hasAdminRole);
            log.info("User has ROLE_MEDA: {}", hasMedaRole);
            log.info("User authorized: {}", (hasAdminRole || hasMedaRole));
        } else {
            log.warn("No authentication found for create project request");
        }
        
        log.info("=== END CREATE PROJECT DEBUG INFO ===");
        
        try {
            Projet createdProjet = projetService.createProjet(projet);
            return ResponseEntity.ok(ApiResponse.success(createdProjet, "Project created successfully"));
        } catch (Exception e) {
            log.error("Error creating project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Update an existing MEDA project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Projet>> updateProjet(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @Valid @RequestBody Projet projet) {
        log.info("Updating project with ID: {}", id);
        try {
            Projet updatedProjet = projetService.updateProjet(id, projet);
            return ResponseEntity.ok(ApiResponse.success(updatedProjet, "Project updated successfully"));
        } catch (Exception e) {
            log.error("Error updating project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a specific project by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Projet>> getProjetById(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        log.info("Retrieving project with ID: {}", id);
        try {
            Projet projet = projetService.getProjetById(id);
            return ResponseEntity.ok(ApiResponse.success(projet));
        } catch (Exception e) {
            log.error("Error retrieving project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/financial")
    @Operation(summary = "Get project financial details", description = "Retrieve detailed financial information for a project")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<ProjetDTO>> getProjetFinancialDetails(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        log.info("Retrieving financial details for project ID: {}", id);
        try {
            ProjetDTO projetDTO = projetService.getProjetFinancialDetails(id);
            return ResponseEntity.ok(ApiResponse.success(projetDTO));
        } catch (Exception e) {
            log.error("Error retrieving project financial details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/name/{nomProjet}")
    @Operation(summary = "Get project by name", description = "Retrieve a project by its name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Projet>> getProjetByName(
            @Parameter(description = "Project name") @PathVariable String nomProjet) {
        log.info("Retrieving project with name: {}", nomProjet);
        try {
            return projetService.getProjetByName(nomProjet)
                    .map(projet -> ResponseEntity.ok(ApiResponse.success(projet)))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Project not found")));
        } catch (Exception e) {
            log.error("Error retrieving project by name: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieve all projects with pagination")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Page<Projet>>> getAllProjets(Pageable pageable) {
        log.info("Retrieving all projects with pagination");
        try {
            Page<Projet> projets = projetService.getAllProjets(pageable);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all projects without pagination", description = "Retrieve all projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getAllProjetsWithoutPagination() {
        log.info("Retrieving all projects without pagination");
        try {
            List<Projet> projets = projetService.getAllProjets();
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Delete a project by its ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Void>> deleteProjet(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        log.info("Deleting project with ID: {}", id);
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting project: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active projects", description = "Retrieve all active projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getActiveProjets() {
        log.info("Retrieving active projects");
        try {
            List<Projet> projets = projetService.getActiveProjets();
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving active projects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/completed")
    @Operation(summary = "Get completed projects", description = "Retrieve all completed projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getCompletedProjets() {
        log.info("Retrieving completed projects");
        try {
            List<Projet> projets = projetService.getCompletedProjets();
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving completed projects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/currency/{devise}")
    @Operation(summary = "Get projects by currency", description = "Retrieve projects by currency")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsByDevise(
            @Parameter(description = "Currency code") @PathVariable String devise) {
        log.info("Retrieving projects by currency: {}", devise);
        try {
            List<Projet> projets = projetService.getProjetsByDevise(devise);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects by currency: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get projects by date range", description = "Retrieve projects within a date range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Retrieving projects by date range: {} to {}", startDate, endDate);
        try {
            List<Projet> projets = projetService.getProjetsByDateRange(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/amount-range")
    @Operation(summary = "Get projects by amount range", description = "Retrieve projects within an amount range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsByAmountRange(
            @Parameter(description = "Minimum amount") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam BigDecimal maxAmount) {
        log.info("Retrieving projects by amount range: {} to {}", minAmount, maxAmount);
        try {
            List<Projet> projets = projetService.getProjetsByAmountRange(minAmount, maxAmount);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects by amount range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search projects", description = "Search projects by name or description")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> searchProjets(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        log.info("Searching projects with term: {}", searchTerm);
        try {
            List<Projet> projets = projetService.searchProjets(searchTerm);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error searching projects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/with-advances")
    @Operation(summary = "Get projects with advances", description = "Retrieve projects that have advances")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsWithAdvances() {
        log.info("Retrieving projects with advances");
        try {
            List<Projet> projets = projetService.getProjetsWithAdvances();
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects with advances: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/without-advances")
    @Operation(summary = "Get projects without advances", description = "Retrieve projects that have no advances")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsWithoutAdvances() {
        log.info("Retrieving projects without advances");
        try {
            List<Projet> projets = projetService.getProjetsWithoutAdvances();
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects without advances: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/ending-soon")
    @Operation(summary = "Get projects ending soon", description = "Retrieve projects ending within specified days")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Projet>>> getProjetsEndingSoon(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("Retrieving projects ending soon within {} days", days);
        try {
            List<Projet> projets = projetService.getProjetsEndingSoon(days);
            return ResponseEntity.ok(ApiResponse.success(projets));
        } catch (Exception e) {
            log.error("Error retrieving projects ending soon: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get project statistics", description = "Retrieve overall project statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Object[]>> getProjetStatistics() {
        log.info("Retrieving project statistics");
        
        // Log authentication details for debugging
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Authentication details - Principal: {}, Authorities: {}", 
                    authentication.getPrincipal(), authentication.getAuthorities());
        } else {
            log.warn("No authentication found for statistics request");
        }
        
        try {
            Object[] statistics = projetService.getProjetStatistics();
            log.info("Successfully retrieved project statistics: {}", statistics);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("Error retrieving project statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/statistics/currency")
    @Operation(summary = "Get project statistics by currency", description = "Retrieve project statistics grouped by currency")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getProjetStatisticsByCurrency() {
        log.info("Retrieving project statistics by currency");
        try {
            List<Object[]> statistics = projetService.getProjetStatisticsByCurrency();
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("Error retrieving project statistics by currency: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/financial-summary")
    @Operation(summary = "Get projects with financial summary", description = "Retrieve projects with their financial details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getProjetsWithFinancialSummary() {
        log.info("Retrieving projects with financial summary");
        try {
            List<Object[]> projects = projetService.getProjetsWithFinancialSummary();
            return ResponseEntity.ok(ApiResponse.success(projects));
        } catch (Exception e) {
            log.error("Error retrieving projects with financial summary: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/count-by-status")
    @Operation(summary = "Count projects by status", description = "Get count of active and completed projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDA')")
    public ResponseEntity<ApiResponse<Object[]>> countProjetsByStatus() {
        log.info("Counting projects by status");
        try {
            Object[] counts = projetService.countProjetsByStatus();
            return ResponseEntity.ok(ApiResponse.success(counts));
        } catch (Exception e) {
            log.error("Error counting projects by status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}