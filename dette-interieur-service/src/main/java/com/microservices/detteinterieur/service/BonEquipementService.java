package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.BonEquipement;
import com.microservices.detteinterieur.repository.BonEquipementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BonEquipementService {
    
    private final BonEquipementRepository bonEquipementRepository;
    
    /**
     * Create a new equipment bond
     */
    public BonEquipement createBon(BonEquipement bon) {
        log.info("Creating new equipment bond: {}", bon.getNumeroBon());
        
        // Validate bond number uniqueness
        if (bonEquipementRepository.existsByNumeroBon(bon.getNumeroBon())) {
            throw new IllegalArgumentException("Bond number already exists: " + bon.getNumeroBon());
        }
        
        // Set default status if not provided
        if (bon.getStatut() == null) {
            bon.setStatut(BonEquipement.StatutBon.EN_COURS);
        }
        
        // Calculate maturity date if not provided
        if (bon.getDateEcheance() == null && bon.getDateSouscription() != null) {
            // Default 1 year maturity
            bon.setDateEcheance(bon.getDateSouscription().plusYears(1));
        }
        
        // Auto-calculate interest if rate is provided
        if (bon.getTauxInteret() != null) {
            BigDecimal interets = bon.calculateInterets();
            log.info("Calculated interest for bond {}: {}", bon.getNumeroBon(), interets);
        }
        
        // Validate business rules
        validateBusinessRules(bon);
        
        BonEquipement savedBon = bonEquipementRepository.save(bon);
        log.info("Equipment bond created successfully with ID: {}", savedBon.getId());
        return savedBon;
    }
    
    /**
     * Update an existing equipment bond
     */
    public BonEquipement updateBon(Long id, BonEquipement bon) {
        log.info("Updating equipment bond with ID: {}", id);
        
        BonEquipement existingBon = getBonById(id);
        
        // Validate bond number uniqueness (excluding current bond)
        if (!existingBon.getNumeroBon().equals(bon.getNumeroBon()) &&
            bonEquipementRepository.existsByNumeroBon(bon.getNumeroBon())) {
            throw new IllegalArgumentException("Bond number already exists: " + bon.getNumeroBon());
        }
        
        // Prevent modification of reimbursed or rejected bonds
        if (existingBon.isRembourse() || existingBon.isRejete()) {
            throw new IllegalArgumentException("Cannot modify reimbursed or rejected equipment bond");
        }
        
        // Update fields
        existingBon.setNumeroBon(bon.getNumeroBon());
        existingBon.setDateSouscription(bon.getDateSouscription());
        existingBon.setDateEcheance(bon.getDateEcheance());
        existingBon.setMontant(bon.getMontant());
        existingBon.setTauxInteret(bon.getTauxInteret());
        existingBon.setSouscripteur(bon.getSouscripteur());
        existingBon.setCommentaire(bon.getCommentaire());
        
        // Validate business rules
        validateBusinessRules(existingBon);
        
        BonEquipement updatedBon = bonEquipementRepository.save(existingBon);
        log.info("Equipment bond updated successfully: {}", updatedBon.getNumeroBon());
        return updatedBon;
    }
    
    /**
     * Get equipment bond by ID
     */
    @Transactional(readOnly = true)
    public BonEquipement getBonById(Long id) {
        return bonEquipementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment bond not found with ID: " + id));
    }
    
    /**
     * Get equipment bond by number
     */
    @Transactional(readOnly = true)
    public Optional<BonEquipement> getBonByNumber(String numeroBon) {
        return bonEquipementRepository.findByNumeroBon(numeroBon);
    }
    
    /**
     * Get all equipment bonds with pagination
     */
    @Transactional(readOnly = true)
    public Page<BonEquipement> getAllBons(Pageable pageable) {
        return bonEquipementRepository.findAll(pageable);
    }
    
    /**
     * Get all equipment bonds
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getAllBons() {
        return bonEquipementRepository.findAll();
    }
    
    /**
     * Delete equipment bond
     */
    public void deleteBon(Long id) {
        log.info("Deleting equipment bond with ID: {}", id);
        
        BonEquipement bon = getBonById(id);
        
        // Prevent deletion of reimbursed bonds
        if (bon.isRembourse()) {
            throw new IllegalArgumentException("Cannot delete reimbursed equipment bond");
        }
        
        // Check if bond has advice notices
        if (!bon.getAvisBons().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete equipment bond with existing advice notices");
        }
        
        bonEquipementRepository.delete(bon);
        log.info("Equipment bond deleted successfully: {}", bon.getNumeroBon());
    }
    
    /**
     * Get equipment bonds by status
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBonsByStatus(BonEquipement.StatutBon statut) {
        return bonEquipementRepository.findByStatutOrderByDateSouscriptionDesc(statut);
    }
    
    /**
     * Get active equipment bonds
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getActiveBonds() {
        return bonEquipementRepository.findByStatutOrderByDateSouscriptionDesc(BonEquipement.StatutBon.EN_COURS);
    }
    
    /**
     * Get reimbursed equipment bonds
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getReimbursedBonds() {
        return bonEquipementRepository.findByStatutOrderByDateSouscriptionDesc(BonEquipement.StatutBon.REMBOURSE);
    }
    
    /**
     * Get equipment bonds by currency (not applicable, removing)
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBonsByCurrency(String devise) {
        // Currency field doesn't exist in this entity
        return getAllBons();
    }
    
    /**
     * Get equipment bonds by date range
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBonsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bonEquipementRepository.findByDateSouscriptionBetween(startDate, endDate);
    }
    
    /**
     * Get equipment bonds by amount range
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBonsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return bonEquipementRepository.findByMontantBetween(minAmount, maxAmount);
    }
    
    /**
     * Get equipment bonds maturing soon
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBonsMaturingSoon(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return bonEquipementRepository.findByDateEcheanceBetween(LocalDate.now(), futureDate);
    }
    
    /**
     * Search equipment bonds
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> searchBonds(String searchTerm) {
        return bonEquipementRepository.searchBonds(searchTerm);
    }
    
    /**
     * Reimburse equipment bond (change status to REMBOURSE)
     */
    public BonEquipement reimburseBond(Long id) {
        log.info("Reimbursing equipment bond with ID: {}", id);
        
        BonEquipement bon = getBonById(id);
        
        if (!bon.isEnCours() && !bon.isSouscrit()) {
            throw new IllegalArgumentException("Only active or subscribed bonds can be reimbursed");
        }
        
        bon.setStatut(BonEquipement.StatutBon.REMBOURSE);
        bon.setDateRemboursement(LocalDate.now());
        
        BonEquipement reimbursedBon = bonEquipementRepository.save(bon);
        log.info("Equipment bond reimbursed successfully: {}", reimbursedBon.getNumeroBon());
        return reimbursedBon;
    }
    
    /**
     * Cancel equipment bond (change status to REJETE)
     */
    public BonEquipement cancelBond(Long id, String motifAnnulation) {
        log.info("Cancelling equipment bond with ID: {}", id);
        
        BonEquipement bon = getBonById(id);
        
        if (bon.isRembourse()) {
            throw new IllegalArgumentException("Cannot cancel reimbursed equipment bond");
        }
        
        if (motifAnnulation == null || motifAnnulation.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        
        bon.setStatut(BonEquipement.StatutBon.REJETE);
        bon.setCommentaire(motifAnnulation);
        
        BonEquipement cancelledBon = bonEquipementRepository.save(bon);
        log.info("Equipment bond cancelled successfully: {}", cancelledBon.getNumeroBon());
        return cancelledBon;
    }
    
    /**
     * Reject equipment bond
     */
    public BonEquipement rejectBond(Long id, String motifRejet) {
        log.info("Rejecting equipment bond with ID: {}", id);
        
        BonEquipement bon = getBonById(id);
        
        if (bon.isRembourse()) {
            throw new IllegalArgumentException("Cannot reject reimbursed equipment bond");
        }
        
        if (motifRejet == null || motifRejet.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        
        bon.setStatut(BonEquipement.StatutBon.REJETE);
        bon.setCommentaire(motifRejet);
        
        BonEquipement rejectedBon = bonEquipementRepository.save(bon);
        log.info("Equipment bond rejected successfully: {}", rejectedBon.getNumeroBon());
        return rejectedBon;
    }
    
    // Simplified statistics methods using basic repository methods or entity calculations
    
    /**
     * Get bond statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBondStatisticsByStatus() {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get bond statistics by currency (not applicable)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBondStatisticsByCurrency() {
        // Currency field doesn't exist in this entity
        return List.of(); // Placeholder
    }
    
    /**
     * Get monthly bond report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyBondReport(LocalDate startDate, LocalDate endDate) {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get interest rate analysis
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInterestRateAnalysis() {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get count by status
     */
    @Transactional(readOnly = true)
    public Long getCountByStatus(BonEquipement.StatutBon statut) {
        return (long) bonEquipementRepository.findByStatutOrderByDateSouscriptionDesc(statut).size();
    }
    
    /**
     * Get total amount by status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(BonEquipement.StatutBon statut) {
        List<BonEquipement> bonds = bonEquipementRepository.findByStatutOrderByDateSouscriptionDesc(statut);
        return bonds.stream()
                .map(BonEquipement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total active amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalActiveAmount() {
        List<BonEquipement> activeBonds = getActiveBonds();
        return activeBonds.stream()
                .map(BonEquipement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total interest earned (calculated from entity method)
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalInterestEarned() {
        List<BonEquipement> bonds = getAllBons();
        return bonds.stream()
                .map(BonEquipement::calculateInterets)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get average interest rate by currency (simplified)
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageInterestRateByCurrency(String devise) {
        // Currency field doesn't exist, return average of all bonds
        List<BonEquipement> bonds = getAllBons();
        BigDecimal totalRate = bonds.stream()
                .filter(bon -> bon.getTauxInteret() != null)
                .map(BonEquipement::getTauxInteret)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = bonds.stream()
                .filter(bon -> bon.getTauxInteret() != null)
                .count();
        return count > 0 ? totalRate.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    /**
     * Get average duration by status (placeholder)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageDurationByStatus() {
        return List.of(); // Placeholder
    }
    
    /**
     * Get bonds requiring renewal (expiring soon)
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getBondsRequiringRenewal(LocalDate cutoffDate) {
        return bonEquipementRepository.findByDateEcheanceBefore(cutoffDate);
    }
    
    /**
     * Get large bonds in period
     */
    @Transactional(readOnly = true)
    public List<BonEquipement> getLargeBondsInPeriod(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        return bonEquipementRepository.findByMontantGreaterThan(minAmount);
    }
    
    /**
     * Validate business rules for equipment bonds
     */
    private void validateBusinessRules(BonEquipement bon) {
        // Validate principal amount
        if (bon.getMontant() != null && bon.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bond amount must be positive");
        }
        
        // Validate interest rate
        if (bon.getTauxInteret() != null && bon.getTauxInteret().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        // Validate dates
        if (bon.getDateSouscription() != null && bon.getDateEcheance() != null &&
            bon.getDateSouscription().isAfter(bon.getDateEcheance())) {
            throw new IllegalArgumentException("Subscription date cannot be after maturity date");
        }
        
        // Validate subscription date not in future
        if (bon.getDateSouscription() != null && bon.getDateSouscription().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Subscription date cannot be in the future");
        }
    }
}