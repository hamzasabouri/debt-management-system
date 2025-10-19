package com.microservices.meda.service;

import com.microservices.meda.entity.Avance;
import com.microservices.meda.entity.Projet;
import com.microservices.meda.entity.Avance.TypeAvance;
import com.microservices.meda.entity.Avance.StatutAvance;
import com.microservices.meda.repository.AvanceRepository;
import com.microservices.meda.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvanceService {
    
    private final AvanceRepository avanceRepository;
    private final ProjetRepository projetRepository;
    
    /**
     * Create a new advance
     */
    public Avance createAvance(Avance avance) {
        log.info("Creating new advance: {}", avance.getNumeroAvance());
        
        // Validate advance number uniqueness
        if (avanceRepository.existsByNumeroAvance(avance.getNumeroAvance())) {
            throw new IllegalArgumentException("Advance number already exists: " + avance.getNumeroAvance());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(avance.getProjet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + avance.getProjet().getId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(avance.getDevise())) {
            throw new IllegalArgumentException("Advance currency must match project currency");
        }
        
        // Set the project reference
        avance.setProjet(projet);
        
        Avance savedAvance = avanceRepository.save(avance);
        log.info("Advance created successfully with ID: {}", savedAvance.getId());
        return savedAvance;
    }
    
    /**
     * Update an existing advance
     */
    public Avance updateAvance(Long id, Avance avance) {
        log.info("Updating advance with ID: {}", id);
        
        Avance existingAvance = getAvanceById(id);
        
        // Validate advance number uniqueness (excluding current advance)
        if (avanceRepository.existsByNumeroAvanceAndIdNot(avance.getNumeroAvance(), id)) {
            throw new IllegalArgumentException("Advance number already exists: " + avance.getNumeroAvance());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(avance.getProjet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + avance.getProjet().getId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(avance.getDevise())) {
            throw new IllegalArgumentException("Advance currency must match project currency");
        }
        
        // Update fields
        existingAvance.setProjet(projet);
        existingAvance.setNumeroAvance(avance.getNumeroAvance());
        existingAvance.setTypeAvance(avance.getTypeAvance());
        existingAvance.setDateReception(avance.getDateReception());
        existingAvance.setMontant(avance.getMontant());
        existingAvance.setDevise(avance.getDevise());
        existingAvance.setEmetteur(avance.getEmetteur());
        existingAvance.setStatut(avance.getStatut());
        
        Avance updatedAvance = avanceRepository.save(existingAvance);
        log.info("Advance updated successfully: {}", updatedAvance.getNumeroAvance());
        return updatedAvance;
    }
    
    /**
     * Get advance by ID
     */
    @Transactional(readOnly = true)
    public Avance getAvanceById(Long id) {
        return avanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advance not found with ID: " + id));
    }
    
    /**
     * Get advance by number
     */
    @Transactional(readOnly = true)
    public Optional<Avance> getAvanceByNumber(String numeroAvance) {
        return avanceRepository.findByNumeroAvance(numeroAvance);
    }
    
    /**
     * Get all advances with pagination
     */
    @Transactional(readOnly = true)
    public Page<Avance> getAllAvances(Pageable pageable) {
        return avanceRepository.findAll(pageable);
    }
    
    /**
     * Get all advances
     */
    @Transactional(readOnly = true)
    public List<Avance> getAllAvances() {
        return avanceRepository.findAll();
    }
    
    /**
     * Delete advance
     */
    public void deleteAvance(Long id) {
        log.info("Deleting advance with ID: {}", id);
        
        Avance avance = getAvanceById(id);
        
        // Business rule: Can only delete advances that are not yet accounted
        if (avance.isComptabilise()) {
            throw new IllegalArgumentException("Cannot delete advance that has been accounted");
        }
        
        avanceRepository.delete(avance);
        log.info("Advance deleted successfully: {}", avance.getNumeroAvance());
    }
    
    /**
     * Get advances by project ID
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByProjet(Long projetId) {
        return avanceRepository.findByProjetIdOrderByDateReception(projetId);
    }
    
    /**
     * Get advances by project ID and type
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByProjetAndType(Long projetId, TypeAvance typeAvance) {
        return avanceRepository.findByProjetIdAndTypeAvanceOrderByDateReception(projetId, typeAvance);
    }
    
    /**
     * Get advances by project ID and status
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByProjetAndStatus(Long projetId, StatutAvance statut) {
        return avanceRepository.findByProjetIdAndStatutOrderByDateReception(projetId, statut);
    }
    
    /**
     * Get advances by type
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByType(TypeAvance typeAvance) {
        return avanceRepository.findByTypeAvanceOrderByDateReception(typeAvance);
    }
    
    /**
     * Get advances by status
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByStatus(StatutAvance statut) {
        return avanceRepository.findByStatutOrderByDateReception(statut);
    }
    
    /**
     * Get advances by currency
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByDevise(String devise) {
        return avanceRepository.findByDeviseOrderByDateReception(devise);
    }
    
    /**
     * Get advances by issuer
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByEmetteur(String emetteur) {
        return avanceRepository.findByEmetteurIgnoreCaseOrderByDateReception(emetteur);
    }
    
    /**
     * Get advances by date range
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByDateRange(LocalDate startDate, LocalDate endDate) {
        return avanceRepository.findByReceptionDateBetween(startDate, endDate);
    }
    
    /**
     * Get advances by amount range
     */
    @Transactional(readOnly = true)
    public List<Avance> getAvancesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return avanceRepository.findByAmountRange(minAmount, maxAmount);
    }
    
    /**
     * Search advances
     */
    @Transactional(readOnly = true)
    public List<Avance> searchAvances(String searchTerm) {
        return avanceRepository.searchAdvances(searchTerm);
    }
    
    /**
     * Get recent advances within specified days
     */
    @Transactional(readOnly = true)
    public List<Avance> getRecentAvances(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return avanceRepository.findByDateReceptionAfterOrderByDateReceptionDesc(cutoffDate);
    }
    
    /**
     * Get advances pending accounting
     */
    @Transactional(readOnly = true)
    public List<Avance> getAdvancesPendingAccounting() {
        return avanceRepository.findAdvancesPendingAccounting();
    }
    
    /**
     * Get largest advances
     */
    @Transactional(readOnly = true)
    public List<Avance> getLargestAdvances() {
        return avanceRepository.findLargestAdvances();
    }
    
    /**
     * Get total advances amount by project
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAdvancesByProjet(Long projetId) {
        return avanceRepository.getTotalAdvancesByProject(projetId);
    }
    
    /**
     * Get total advances amount by project and type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAdvancesByProjetAndType(Long projetId, TypeAvance typeAvance) {
        return avanceRepository.getTotalAdvancesByProjectAndType(projetId, typeAvance);
    }
    
    /**
     * Get total advances amount by project and status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAdvancesByProjetAndStatus(Long projetId, StatutAvance statut) {
        return avanceRepository.getTotalAdvancesByProjectAndStatus(projetId, statut);
    }
    
    /**
     * Get advances statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdvancesStatisticsByType() {
        return avanceRepository.getAdvancesStatisticsByType();
    }
    
    /**
     * Get advances statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdvancesStatisticsByStatus() {
        return avanceRepository.getAdvancesStatisticsByStatus();
    }
    
    /**
     * Get advances statistics by currency
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdvancesStatisticsByCurrency() {
        return avanceRepository.getAdvancesStatisticsByCurrency();
    }
    
    /**
     * Count advances by project and type
     */
    @Transactional(readOnly = true)
    public List<Object[]> countAdvancesByProjetAndType(Long projetId) {
        return avanceRepository.countAdvancesByProjectAndType(projetId);
    }
    
    /**
     * Get monthly advances summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyAdvancesSummary() {
        return avanceRepository.getMonthlyAdvancesSummary();
    }
    
    /**
     * Update advance status
     */
    public Avance updateAvanceStatus(Long id, StatutAvance newStatus) {
        log.info("Updating advance status for ID: {} to {}", id, newStatus);
        
        Avance avance = getAvanceById(id);
        
        // Validate status transition
        validateStatusTransition(avance.getStatut(), newStatus);
        
        avance.setStatut(newStatus);
        
        Avance updatedAvance = avanceRepository.save(avance);
        log.info("Advance status updated successfully: {}", updatedAvance.getNumeroAvance());
        return updatedAvance;
    }
    
    /**
     * Process advance from BAM
     */
    public Avance processAdvanceFromBAM(Avance avance) {
        log.info("Processing advance from BAM: {}", avance.getNumeroAvance());
        
        // Set initial status for BAM advances
        avance.setStatut(StatutAvance.PRIS_EN_CHARGE);
        
        return createAvance(avance);
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(StatutAvance currentStatus, StatutAvance newStatus) {
        // Business rules for status transitions
        if (currentStatus == StatutAvance.COMPTABILISE && newStatus == StatutAvance.PRIS_EN_CHARGE) {
            throw new IllegalArgumentException("Cannot revert accounted advance to pending status");
        }
    }
    
    /**
     * Create advance from DTO with projetId
     * This method handles the conversion from DTO with projetId to entity with projet object
     */
    public Avance createAvanceFromDto(com.microservices.meda.dto.AvanceDTO avanceDto) {
        log.info("Creating new advance from DTO: {}", avanceDto.getNumeroAvance());
        
        // Validate advance number uniqueness
        if (avanceRepository.existsByNumeroAvance(avanceDto.getNumeroAvance())) {
            throw new IllegalArgumentException("Advance number already exists: " + avanceDto.getNumeroAvance());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(avanceDto.getProjetId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + avanceDto.getProjetId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(avanceDto.getDevise())) {
            throw new IllegalArgumentException("Advance currency must match project currency");
        }
        
        // Create Avance entity from DTO
        Avance avance = new Avance();
        avance.setProjet(projet);
        avance.setNumeroAvance(avanceDto.getNumeroAvance());
        avance.setTypeAvance(TypeAvance.valueOf(avanceDto.getTypeAvance()));
        avance.setDateReception(avanceDto.getDateReception());
        avance.setMontant(avanceDto.getMontant());
        avance.setDevise(avanceDto.getDevise());
        avance.setEmetteur(avanceDto.getEmetteur());
        avance.setStatut(StatutAvance.valueOf(avanceDto.getStatut()));
        
        Avance savedAvance = avanceRepository.save(avance);
        log.info("Advance created successfully with ID: {}", savedAvance.getId());
        return savedAvance;
    }
    
    /**
     * Update advance from DTO with projetId
     * This method handles the conversion from DTO with projetId to entity with projet object
     */
    public Avance updateAvanceFromDto(Long id, com.microservices.meda.dto.AvanceDTO avanceDto) {
        log.info("Updating advance with ID: {}", id);
        
        Avance existingAvance = getAvanceById(id);
        
        // Validate advance number uniqueness (excluding current advance)
        if (avanceRepository.existsByNumeroAvanceAndIdNot(avanceDto.getNumeroAvance(), id)) {
            throw new IllegalArgumentException("Advance number already exists: " + avanceDto.getNumeroAvance());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(avanceDto.getProjetId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + avanceDto.getProjetId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(avanceDto.getDevise())) {
            throw new IllegalArgumentException("Advance currency must match project currency");
        }
        
        // Update fields
        existingAvance.setProjet(projet);
        existingAvance.setNumeroAvance(avanceDto.getNumeroAvance());
        existingAvance.setTypeAvance(TypeAvance.valueOf(avanceDto.getTypeAvance()));
        existingAvance.setDateReception(avanceDto.getDateReception());
        existingAvance.setMontant(avanceDto.getMontant());
        existingAvance.setDevise(avanceDto.getDevise());
        existingAvance.setEmetteur(avanceDto.getEmetteur());
        existingAvance.setStatut(StatutAvance.valueOf(avanceDto.getStatut()));
        
        Avance updatedAvance = avanceRepository.save(existingAvance);
        log.info("Advance updated successfully: {}", updatedAvance.getNumeroAvance());
        return updatedAvance;
    }
}