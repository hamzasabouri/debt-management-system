package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.AvisBonEquipement;
import com.microservices.detteinterieur.entity.BonEquipement;
import com.microservices.detteinterieur.repository.AvisBonEquipementRepository;
import com.microservices.detteinterieur.repository.BonEquipementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvisBonEquipementService {
    
    private final AvisBonEquipementRepository avisBonEquipementRepository;
    private final BonEquipementRepository bonEquipementRepository;
    
    /**
     * Create a new equipment bond advice notice
     */
    public AvisBonEquipement createAvis(AvisBonEquipement avis) {
        log.info("Creating new equipment bond advice: {}", avis.getNumeroAvis());
        
        // Validate advice number uniqueness
        if (avisBonEquipementRepository.existsByNumeroAvis(avis.getNumeroAvis())) {
            throw new IllegalArgumentException("Advice number already exists: " + avis.getNumeroAvis());
        }
        
        // Validate that the equipment bond exists
        if (avis.getBonEquipement() != null) {
            BonEquipement bonEquipement = bonEquipementRepository.findById(avis.getBonEquipement().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Equipment bond not found"));
            
            // Validate that rejected bonds can't receive new credit notices
            if (bonEquipement.isRejete() && avis.isCredit()) {
                throw new IllegalArgumentException("Cannot add credit notice to rejected equipment bond");
            }
        }
        
        // Set default status if not provided
        if (avis.getStatut() == null) {
            avis.setStatut(AvisBonEquipement.StatutAvis.EN_ATTENTE);
        }
        
        // Validate business rules
        validateBusinessRules(avis);
        
        AvisBonEquipement savedAvis = avisBonEquipementRepository.save(avis);
        log.info("Equipment bond advice created successfully with ID: {}", savedAvis.getId());
        return savedAvis;
    }
    
    /**
     * Update an existing equipment bond advice notice
     */
    public AvisBonEquipement updateAvis(Long id, AvisBonEquipement avis) {
        log.info("Updating equipment bond advice with ID: {}", id);
        
        AvisBonEquipement existingAvis = getAvisById(id);
        
        // Validate advice number uniqueness (excluding current advice)
        if (!existingAvis.getNumeroAvis().equals(avis.getNumeroAvis()) &&
            avisBonEquipementRepository.existsByNumeroAvis(avis.getNumeroAvis())) {
            throw new IllegalArgumentException("Advice number already exists: " + avis.getNumeroAvis());
        }
        
        // Prevent modification of processed advice
        if (existingAvis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot modify accounted advice notice");
        }
        
        // Update fields
        existingAvis.setNumeroAvis(avis.getNumeroAvis());
        existingAvis.setDateReception(avis.getDateReception());
        existingAvis.setMontant(avis.getMontant());
        existingAvis.setEmetteur(avis.getEmetteur());
        existingAvis.setTypeAvis(avis.getTypeAvis());
        existingAvis.setCommentaire(avis.getCommentaire());
        
        // Validate business rules
        validateBusinessRules(existingAvis);
        
        AvisBonEquipement updatedAvis = avisBonEquipementRepository.save(existingAvis);
        log.info("Equipment bond advice updated successfully: {}", updatedAvis.getNumeroAvis());
        return updatedAvis;
    }
    
    /**
     * Get advice notice by ID
     */
    @Transactional(readOnly = true)
    public AvisBonEquipement getAvisById(Long id) {
        return avisBonEquipementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advice notice not found with ID: " + id));
    }
    
    /**
     * Get advice notice by number
     */
    @Transactional(readOnly = true)
    public Optional<AvisBonEquipement> getAvisByNumber(String numeroAvis) {
        return avisBonEquipementRepository.findByNumeroAvis(numeroAvis);
    }
    
    /**
     * Get all advice notices with pagination
     */
    @Transactional(readOnly = true)
    public Page<AvisBonEquipement> getAllAvis(Pageable pageable) {
        return avisBonEquipementRepository.findAll(pageable);
    }
    
    /**
     * Get all advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAllAvis() {
        return avisBonEquipementRepository.findAll();
    }
    
    /**
     * Delete advice notice
     */
    public void deleteAvis(Long id) {
        log.info("Deleting equipment bond advice notice with ID: {}", id);
        
        AvisBonEquipement avis = getAvisById(id);
        
        // Prevent deletion of processed advice
        if (avis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot delete accounted advice notice");
        }
        
        avisBonEquipementRepository.delete(avis);
        log.info("Equipment bond advice notice deleted successfully: {}", avis.getNumeroAvis());
    }
    
    /**
     * Get advice notices by equipment bond ID
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByBondId(Long bonEquipementId) {
        return avisBonEquipementRepository.findByBonEquipementIdOrderByDateReceptionDesc(bonEquipementId);
    }
    
    /**
     * Get advice notices by equipment bond number
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByBondNumber(String numeroBon) {
        // This would need a custom query or find by bond first
        return List.of(); // Placeholder - would need to implement proper query
    }
    
    /**
     * Get credit advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getCreditAdvices() {
        return avisBonEquipementRepository.findCreditAdvices();
    }
    
    /**
     * Get debit advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getDebitAdvices() {
        return avisBonEquipementRepository.findDebitAdvices();
    }
    
    /**
     * Get rejection advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getRejectionAdvices() {
        return avisBonEquipementRepository.findRejectionAdvices();
    }
    
    /**
     * Get pending advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getPendingAdvices() {
        return avisBonEquipementRepository.findPendingAdvices();
    }
    
    /**
     * Get processed advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getProcessedAdvices() {
        return avisBonEquipementRepository.findByStatutOrderByDateReceptionDesc(AvisBonEquipement.StatutAvis.PRIS_EN_CHARGE);
    }
    
    /**
     * Get accounted advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAccountedAdvices() {
        return avisBonEquipementRepository.findAccountedAdvices();
    }
    
    /**
     * Get advice notices by status
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByStatus(AvisBonEquipement.StatutAvis statut) {
        return avisBonEquipementRepository.findByStatutOrderByDateReceptionDesc(statut);
    }
    
    /**
     * Get advice notices by type
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByType(AvisBonEquipement.TypeAvis typeAvis) {
        return avisBonEquipementRepository.findByTypeAvisOrderByDateReceptionDesc(typeAvis);
    }
    
    /**
     * Get advice notices by issuer
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByIssuer(String emetteur) {
        return avisBonEquipementRepository.findByEmetteurContainingIgnoreCase(emetteur);
    }
    
    /**
     * Get advice notices from BAM
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAdvicesFromBAM() {
        return avisBonEquipementRepository.findAdvicesFromBAM();
    }
    
    /**
     * Get advice notices from Comptable
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAdvicesFromComptable() {
        return avisBonEquipementRepository.findAdvicesFromComptable();
    }
    
    /**
     * Get advice notices by date range
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByDateRange(LocalDate startDate, LocalDate endDate) {
        return avisBonEquipementRepository.findByDateReceptionBetween(startDate, endDate);
    }
    
    /**
     * Get advice notices by amount range
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getAvisByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return avisBonEquipementRepository.findByMontantBetween(minAmount, maxAmount);
    }
    
    /**
     * Search advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> searchAdvices(String searchTerm) {
        return avisBonEquipementRepository.searchAdvices(searchTerm);
    }
    
    /**
     * Process advice notice (change status to PRIS_EN_CHARGE)
     */
    public AvisBonEquipement processAdvice(Long id) {
        log.info("Processing equipment bond advice notice with ID: {}", id);
        
        AvisBonEquipement avis = getAvisById(id);
        
        if (!avis.isEnAttente()) {
            throw new IllegalArgumentException("Only pending advice notices can be processed");
        }
        
        avis.processer();
        
        AvisBonEquipement processedAvis = avisBonEquipementRepository.save(avis);
        log.info("Equipment bond advice notice processed successfully: {}", processedAvis.getNumeroAvis());
        return processedAvis;
    }
    
    /**
     * Account advice notice (change status to COMPTABILISE)
     */
    public AvisBonEquipement accountAdvice(Long id) {
        log.info("Accounting equipment bond advice notice with ID: {}", id);
        
        AvisBonEquipement avis = getAvisById(id);
        
        if (!avis.isPrisEnCharge()) {
            throw new IllegalArgumentException("Only processed advice notices can be accounted");
        }
        
        avis.comptabiliser();
        
        AvisBonEquipement accountedAvis = avisBonEquipementRepository.save(avis);
        log.info("Equipment bond advice notice accounted successfully: {}", accountedAvis.getNumeroAvis());
        return accountedAvis;
    }
    
    /**
     * Reject advice notice
     */
    public AvisBonEquipement rejectAdvice(Long id, String motifRejet) {
        log.info("Rejecting equipment bond advice notice with ID: {}", id);
        
        AvisBonEquipement avis = getAvisById(id);
        
        if (avis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot reject accounted advice notice");
        }
        
        if (motifRejet == null || motifRejet.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        
        avis.rejeter(motifRejet);
        
        AvisBonEquipement rejectedAvis = avisBonEquipementRepository.save(avis);
        log.info("Equipment bond advice notice rejected successfully: {}", rejectedAvis.getNumeroAvis());
        return rejectedAvis;
    }
    
    /**
     * Get advice statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdviceStatisticsByType() {
        return avisBonEquipementRepository.getAdviceStatisticsByType();
    }
    
    /**
     * Get advice statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdviceStatisticsByStatus() {
        return avisBonEquipementRepository.getAdviceStatisticsByStatus();
    }
    
    /**
     * Get advice statistics by issuer
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdviceStatisticsByIssuer() {
        return avisBonEquipementRepository.getAdviceStatisticsByIssuer();
    }
    
    /**
     * Get financial summary by bond
     */
    @Transactional(readOnly = true)
    public Object[] getFinancialSummaryByBond(Long bonEquipementId) {
        return avisBonEquipementRepository.getFinancialSummaryByBond(bonEquipementId);
    }
    
    /**
     * Get overall financial summary
     */
    @Transactional(readOnly = true)
    public Object[] getOverallFinancialSummary() {
        return avisBonEquipementRepository.getOverallFinancialSummary();
    }
    
    /**
     * Get monthly advice report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyAdviceReport(LocalDate startDate, LocalDate endDate) {
        return avisBonEquipementRepository.getMonthlyAdviceReport(startDate, endDate);
    }
    
    /**
     * Get count by type and status
     */
    @Transactional(readOnly = true)
    public Long getCountByTypeAndStatus(AvisBonEquipement.TypeAvis typeAvis, AvisBonEquipement.StatutAvis statut) {
        return avisBonEquipementRepository.countByTypeAndStatus(typeAvis, statut);
    }
    
    /**
     * Get total accounted amount by type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAccountedAmountByType(AvisBonEquipement.TypeAvis typeAvis) {
        BigDecimal total = avisBonEquipementRepository.getTotalAccountedAmountByType(typeAvis);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get pending advice notices for active bonds
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getPendingAdvicesForActiveBonds() {
        return avisBonEquipementRepository.findPendingAdvicesForActiveBonds();
    }
    
    /**
     * Get large advice notices in period
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getLargeAdvicesInPeriod(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        return avisBonEquipementRepository.findLargeAdvicesInPeriod(startDate, endDate, minAmount);
    }
    
    /**
     * Get overdue advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getOverdueAdvices(LocalDate threshold) {
        return avisBonEquipementRepository.findOverdueAdvices(threshold);
    }
    
    /**
     * Get recent advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getRecentAdvices(int limit) {
        return avisBonEquipementRepository.findRecentAdvices(limit);
    }
    
    /**
     * Get largest advice notices since date
     */
    @Transactional(readOnly = true)
    public List<AvisBonEquipement> getLargestAdvicesSince(LocalDate date) {
        return avisBonEquipementRepository.findLargestAdvicesSince(date);
    }
    
    /**
     * Get average processing time
     */
    @Transactional(readOnly = true)
    public Double getAverageProcessingTimeInDays() {
        // Placeholder implementation - would need complex date calculation
        return 5.0; // Average of 5 days
    }
    
    /**
     * Get average processing time by type (placeholder)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageProcessingTimeByType() {
        return List.of(); // Placeholder - would need to implement calculation
    }
    
    /**
     * Validate business rules for advice notices
     */
    private void validateBusinessRules(AvisBonEquipement avis) {
        // Validate amount
        if (avis.getMontant() != null && avis.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advice amount must be positive");
        }
        
        // Validate reception date not in future
        if (avis.getDateReception() != null && avis.getDateReception().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Reception date cannot be in the future");
        }
        
        // Validate rejection reason for rejection notices
        if (avis.requiresRejectionReason() && 
            (avis.getMotifRejet() == null || avis.getMotifRejet().trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required for rejection notices");
        }
        
        // Validate that rejected bond can't receive new credit notices
        if (avis.getBonEquipement() != null && avis.getBonEquipement().isRejete() && avis.isCredit()) {
            throw new IllegalArgumentException("Cannot add credit notice to rejected equipment bond");
        }
        
        // Validate issuer consistency
        if (avis.isFromBAM() && avis.isCredit()) {
            log.warn("BAM typically sends debit notices, received credit notice from BAM: {}", avis.getNumeroAvis());
        }
        
        // Validate rejection notices should reverse previous operations
        if (avis.isRejet()) {
            log.info("Rejection notice {} will reverse previous operations for bond {}", 
                    avis.getNumeroAvis(), avis.getBonEquipement().getNumeroBon());
        }
    }
}