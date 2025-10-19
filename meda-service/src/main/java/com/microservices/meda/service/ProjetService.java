package com.microservices.meda.service;

import com.microservices.meda.entity.Projet;
import com.microservices.meda.repository.ProjetRepository;
import com.microservices.meda.dto.ProjetDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjetService {
    
    private final ProjetRepository projetRepository;
    
    /**
     * Create a new project
     */
    public Projet createProjet(Projet projet) {
        log.info("Creating new project: {}", projet.getNomProjet());
        
        // Validate project name uniqueness
        if (projetRepository.existsByNomProjetIgnoreCase(projet.getNomProjet())) {
            throw new IllegalArgumentException("Project name already exists: " + projet.getNomProjet());
        }
        
        // Validate dates
        if (projet.getDateDebut().isAfter(projet.getDateFin())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        Projet savedProjet = projetRepository.save(projet);
        log.info("Project created successfully with ID: {}", savedProjet.getId());
        return savedProjet;
    }
    
    /**
     * Update an existing project
     */
    public Projet updateProjet(Long id, Projet projet) {
        log.info("Updating project with ID: {}", id);
        
        Projet existingProjet = getProjetById(id);
        
        // Validate project name uniqueness (excluding current project)
        if (projetRepository.existsByNomProjetIgnoreCaseAndIdNot(projet.getNomProjet(), id)) {
            throw new IllegalArgumentException("Project name already exists: " + projet.getNomProjet());
        }
        
        // Update fields
        existingProjet.setNomProjet(projet.getNomProjet());
        existingProjet.setDescription(projet.getDescription());
        existingProjet.setDateDebut(projet.getDateDebut());
        existingProjet.setDateFin(projet.getDateFin());
        existingProjet.setMontantTotal(projet.getMontantTotal());
        existingProjet.setDevise(projet.getDevise());
        
        Projet updatedProjet = projetRepository.save(existingProjet);
        log.info("Project updated successfully: {}", updatedProjet.getNomProjet());
        return updatedProjet;
    }
    
    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public Projet getProjetById(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
    }
    
    /**
     * Get project by name
     */
    @Transactional(readOnly = true)
    public Optional<Projet> getProjetByName(String nomProjet) {
        return projetRepository.findByNomProjetIgnoreCase(nomProjet);
    }
    
    /**
     * Get all projects with pagination
     */
    @Transactional(readOnly = true)
    public Page<Projet> getAllProjets(Pageable pageable) {
        return projetRepository.findAll(pageable);
    }
    
    /**
     * Get all projects
     */
    @Transactional(readOnly = true)
    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }
    
    /**
     * Delete project
     */
    public void deleteProjet(Long id) {
        log.info("Deleting project with ID: {}", id);
        
        Projet projet = getProjetById(id);
        
        // Check if project has associated advances or documents
        if (!projet.getAvances().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete project with existing advances");
        }
        
        if (!projet.getPiecesJustificatives().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete project with existing supporting documents");
        }
        
        projetRepository.delete(projet);
        log.info("Project deleted successfully: {}", projet.getNomProjet());
    }
    
    /**
     * Get active projects
     */
    @Transactional(readOnly = true)
    public List<Projet> getActiveProjets() {
        return projetRepository.findActiveProjects(LocalDate.now());
    }
    
    /**
     * Get completed projects
     */
    @Transactional(readOnly = true)
    public List<Projet> getCompletedProjets() {
        return projetRepository.findCompletedProjects(LocalDate.now());
    }
    
    /**
     * Get projects by currency
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsByDevise(String devise) {
        return projetRepository.findByDeviseOrderByDateDebut(devise);
    }
    
    /**
     * Get projects by date range
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsByDateRange(LocalDate startDate, LocalDate endDate) {
        return projetRepository.findProjectsByDateRange(startDate, endDate);
    }
    
    /**
     * Get projects by amount range
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return projetRepository.findProjectsByAmountRange(minAmount, maxAmount);
    }
    
    /**
     * Search projects by name or description
     */
    @Transactional(readOnly = true)
    public List<Projet> searchProjets(String searchTerm) {
        return projetRepository.searchProjectsByNameOrDescription(searchTerm);
    }
    
    /**
     * Get projects with advances
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsWithAdvances() {
        return projetRepository.findProjectsWithAdvances();
    }
    
    /**
     * Get projects without advances
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsWithoutAdvances() {
        return projetRepository.findProjectsWithoutAdvances();
    }
    
    /**
     * Get projects with supporting documents
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsWithSupportingDocuments() {
        return projetRepository.findProjectsWithSupportingDocuments();
    }
    
    /**
     * Get projects ending soon (within next N days)
     */
    @Transactional(readOnly = true)
    public List<Projet> getProjetsEndingSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return projetRepository.findProjectsEndingSoon(today, futureDate);
    }
    
    /**
     * Get project statistics
     */
    @Transactional(readOnly = true)
    public Object[] getProjetStatistics() {
        log.info("Calling repository to get project statistics");
        try {
            Object[] statistics = projetRepository.getProjectStatistics();
            log.info("Successfully retrieved project statistics from repository: {}", statistics);
            return statistics;
        } catch (Exception e) {
            log.error("Error retrieving project statistics from repository: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get project statistics by currency
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProjetStatisticsByCurrency() {
        return projetRepository.getProjectStatisticsByCurrency();
    }
    
    /**
     * Get projects with financial summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProjetsWithFinancialSummary() {
        return projetRepository.getProjectsWithFinancialSummary();
    }
    
    /**
     * Count projects by status
     */
    @Transactional(readOnly = true)
    public Object[] countProjetsByStatus() {
        return projetRepository.countProjectsByStatus(LocalDate.now());
    }
    
    /**
     * Get project financial details
     */
    @Transactional(readOnly = true)
    public ProjetDTO getProjetFinancialDetails(Long id) {
        Projet projet = getProjetById(id);
        
        BigDecimal totalAvances = projet.getTotalAvances();
        BigDecimal totalDepenses = projet.getTotalDepenses();
        BigDecimal soldeDisponible = projet.getSoldeDisponible();
        
        return ProjetDTO.builder()
                .id(projet.getId())
                .nomProjet(projet.getNomProjet())
                .description(projet.getDescription())
                .dateDebut(projet.getDateDebut())
                .dateFin(projet.getDateFin())
                .montantTotal(projet.getMontantTotal())
                .devise(projet.getDevise())
                .createdAt(projet.getCreatedAt())
                .totalAvances(totalAvances)
                .totalDepenses(totalDepenses)
                .soldeDisponible(soldeDisponible)
                .nombreAvances((long) projet.getAvances().size())
                .nombrePieces((long) projet.getPiecesJustificatives().size())
                .statut(projet.getDateFin().isBefore(LocalDate.now()) ? "TERMINE" : "ACTIF")
                .pourcentageUtilisation(calculateUtilizationPercentage(projet.getMontantTotal(), totalDepenses))
                .build();
    }
    
    /**
     * Calculate utilization percentage
     */
    private Double calculateUtilizationPercentage(BigDecimal montantTotal, BigDecimal totalDepenses) {
        if (montantTotal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        return totalDepenses.multiply(BigDecimal.valueOf(100))
                .divide(montantTotal, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    /**
     * Validate project business rules
     */
    private void validateProjetBusinessRules(Projet projet) {
        // Additional business validation can be added here
        if (projet.getMontantTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Project total amount must be positive");
        }
        
        // Add more business rules as needed
    }
}