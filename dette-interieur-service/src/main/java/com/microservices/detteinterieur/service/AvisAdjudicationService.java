package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.AvisAdjudication;
import com.microservices.detteinterieur.entity.Adjudication;
import com.microservices.detteinterieur.repository.AvisAdjudicationRepository;
import com.microservices.detteinterieur.repository.AdjudicationRepository;
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
public class AvisAdjudicationService {
    
    private final AvisAdjudicationRepository avisAdjudicationRepository;
    private final AdjudicationRepository adjudicationRepository;
    
    /**
     * Create a new auction advice notice
     */
    public AvisAdjudication createAvis(AvisAdjudication avis) {
        log.info("Creating new auction advice: {}", avis.getNumeroAvis());
        
        // Validate advice number uniqueness
        if (avisAdjudicationRepository.existsByNumeroAvis(avis.getNumeroAvis())) {
            throw new IllegalArgumentException("Advice number already exists: " + avis.getNumeroAvis());
        }
        
        // Validate that the adjudication exists and is active
        if (avis.getAdjudication() != null) {
            Adjudication adjudication = adjudicationRepository.findById(avis.getAdjudication().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
            
            if (!adjudication.isActif()) {
                throw new IllegalArgumentException("Cannot add advice to inactive auction");
            }
        }
        
        // Set default status if not provided
        if (avis.getStatut() == null) {
            avis.setStatut(AvisAdjudication.StatutAvis.PRIS_EN_CHARGE);
        }
        
        // Validate business rules
        validateBusinessRules(avis);
        
        AvisAdjudication savedAvis = avisAdjudicationRepository.save(avis);
        log.info("Auction advice created successfully with ID: {}", savedAvis.getId());
        return savedAvis;
    }
    
    /**
     * Update an existing auction advice notice
     */
    public AvisAdjudication updateAvis(Long id, AvisAdjudication avis) {
        log.info("Updating auction advice with ID: {}", id);
        
        AvisAdjudication existingAvis = getAvisById(id);
        
        // Validate advice number uniqueness (excluding current advice)
        if (!existingAvis.getNumeroAvis().equals(avis.getNumeroAvis()) &&
            avisAdjudicationRepository.existsByNumeroAvis(avis.getNumeroAvis())) {
            throw new IllegalArgumentException("Advice number already exists: " + avis.getNumeroAvis());
        }
        
        // Prevent modification of processed advice
        if (existingAvis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot modify processed advice notice");
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
        
        AvisAdjudication updatedAvis = avisAdjudicationRepository.save(existingAvis);
        log.info("Auction advice updated successfully: {}", updatedAvis.getNumeroAvis());
        return updatedAvis;
    }
    
    /**
     * Get advice notice by ID
     */
    @Transactional(readOnly = true)
    public AvisAdjudication getAvisById(Long id) {
        return avisAdjudicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advice notice not found with ID: " + id));
    }
    
    /**
     * Get advice notice by number
     */
    @Transactional(readOnly = true)
    public Optional<AvisAdjudication> getAvisByNumber(String numeroAvis) {
        return avisAdjudicationRepository.findByNumeroAvis(numeroAvis);
    }
    
    /**
     * Get all advice notices with pagination
     */
    @Transactional(readOnly = true)
    public Page<AvisAdjudication> getAllAvis(Pageable pageable) {
        return avisAdjudicationRepository.findAll(pageable);
    }
    
    /**
     * Get all advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAllAvis() {
        return avisAdjudicationRepository.findAll();
    }
    
    /**
     * Delete advice notice
     */
    public void deleteAvis(Long id) {
        log.info("Deleting advice notice with ID: {}", id);
        
        AvisAdjudication avis = getAvisById(id);
        
        // Prevent deletion of processed advice
        if (avis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot delete processed advice notice");
        }
        
        avisAdjudicationRepository.delete(avis);
        log.info("Advice notice deleted successfully: {}", avis.getNumeroAvis());
    }
    
    /**
     * Get advice notices by auction ID
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByAuctionId(Long adjudicationId) {
        return avisAdjudicationRepository.findByAdjudicationIdOrderByDateReceptionDesc(adjudicationId);
    }
    
    /**
     * Get advice notices by auction number
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByAuctionNumber(String numeroAdjud) {
        return avisAdjudicationRepository.findByAdjudicationNumber(numeroAdjud);
    }
    
    /**
     * Get credit advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getCreditAdvices() {
        return avisAdjudicationRepository.findCreditAdvices();
    }
    
    /**
     * Get debit advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getDebitAdvices() {
        return avisAdjudicationRepository.findDebitAdvices();
    }
    
    /**
     * Get pending advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getPendingAdvices() {
        return avisAdjudicationRepository.findPendingAdvices();
    }
    
    /**
     * Get accounted advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAccountedAdvices() {
        return avisAdjudicationRepository.findAccountedAdvices();
    }
    
    /**
     * Get advice notices by status
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByStatus(AvisAdjudication.StatutAvis statut) {
        return avisAdjudicationRepository.findByStatutOrderByDateReceptionDesc(statut);
    }
    
    /**
     * Get advice notices by type
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByType(AvisAdjudication.TypeAvis typeAvis) {
        return avisAdjudicationRepository.findByTypeAvisOrderByDateReceptionDesc(typeAvis);
    }
    
    /**
     * Get advice notices by issuer
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByIssuer(String emetteur) {
        return avisAdjudicationRepository.findByEmetteurContainingIgnoreCase(emetteur);
    }
    
    /**
     * Get advice notices from BAM
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAdvicesFromBAM() {
        return avisAdjudicationRepository.findAdvicesFromBAM();
    }
    
    /**
     * Get advice notices from Maroclear
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAdvicesFromMaroclear() {
        return avisAdjudicationRepository.findAdvicesFromMaroclear();
    }
    
    /**
     * Get advice notices by date range
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByDateRange(LocalDate startDate, LocalDate endDate) {
        return avisAdjudicationRepository.findByDateReceptionBetween(startDate, endDate);
    }
    
    /**
     * Get advice notices by amount range
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getAvisByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return avisAdjudicationRepository.findByMontantBetween(minAmount, maxAmount);
    }
    
    /**
     * Search advice notices
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> searchAdvices(String searchTerm) {
        return avisAdjudicationRepository.searchAdvices(searchTerm);
    }
    
    /**
     * Process advice notice (change status to PRIS_EN_CHARGE)
     */
    public AvisAdjudication processAdvice(Long id) {
        log.info("Processing advice notice with ID: {}", id);
        
        AvisAdjudication avis = getAvisById(id);
        
        // Since AvisAdjudication only has PRIS_EN_CHARGE and COMPTABILISE statuses
        // We assume new advices start as PRIS_EN_CHARGE
        if (!avis.isPrisEnCharge()) {
            avis.setStatut(AvisAdjudication.StatutAvis.PRIS_EN_CHARGE);
        }
        
        AvisAdjudication processedAvis = avisAdjudicationRepository.save(avis);
        log.info("Advice notice processed successfully: {}", processedAvis.getNumeroAvis());
        return processedAvis;
    }
    
    /**
     * Account advice notice (change status to COMPTABILISE)
     */
    public AvisAdjudication accountAdvice(Long id) {
        log.info("Accounting advice notice with ID: {}", id);
        
        AvisAdjudication avis = getAvisById(id);
        
        if (!avis.isPrisEnCharge()) {
            throw new IllegalArgumentException("Only processed advice notices can be accounted");
        }
        
        avis.setStatut(AvisAdjudication.StatutAvis.COMPTABILISE);
        
        AvisAdjudication accountedAvis = avisAdjudicationRepository.save(avis);
        log.info("Advice notice accounted successfully: {}", accountedAvis.getNumeroAvis());
        return accountedAvis;
    }
    
    /**
     * Reject advice notice
     */
    public AvisAdjudication rejectAdvice(Long id, String motifRejet) {
        log.info("Rejecting advice notice with ID: {}", id);
        
        AvisAdjudication avis = getAvisById(id);
        
        if (avis.isComptabilise()) {
            throw new IllegalArgumentException("Cannot reject accounted advice notice");
        }
        
        if (motifRejet == null || motifRejet.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        
        // Since there's no REJETE status in this entity, we add the reason as a comment
        avis.setCommentaire("REJECTED: " + motifRejet);
        
        AvisAdjudication rejectedAvis = avisAdjudicationRepository.save(avis);
        log.info("Advice notice rejected successfully: {}", rejectedAvis.getNumeroAvis());
        return rejectedAvis;
    }
    
    /**
     * Get advice statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdviceStatisticsByType() {
        return avisAdjudicationRepository.getAdviceStatisticsByType();
    }
    
    /**
     * Get advice statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdviceStatisticsByStatus() {
        return avisAdjudicationRepository.getAdviceStatisticsByStatus();
    }
    
    /**
     * Get financial summary by auction
     */
    @Transactional(readOnly = true)
    public Object[] getFinancialSummaryByAuction(Long adjudicationId) {
        return avisAdjudicationRepository.getFinancialSummaryByAuction(adjudicationId);
    }
    
    /**
     * Get overall financial summary
     */
    @Transactional(readOnly = true)
    public Object[] getOverallFinancialSummary() {
        return avisAdjudicationRepository.getOverallFinancialSummary();
    }
    
    /**
     * Get monthly advice report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyAdviceReport(LocalDate startDate, LocalDate endDate) {
        return avisAdjudicationRepository.getMonthlyAdviceReport(startDate, endDate);
    }
    
    /**
     * Validate business rules for advice notices
     */
    private void validateBusinessRules(AvisAdjudication avis) {
        // Validate amount
        if (avis.getMontant() != null && avis.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advice amount must be positive");
        }
        
        // Validate reception date not in future
        if (avis.getDateReception() != null && avis.getDateReception().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Reception date cannot be in the future");
        }
        
        // Validate issuer for specific types
        if (avis.isFromBAM() && !avis.getTypeAvis().equals(AvisAdjudication.TypeAvis.DEBIT)) {
            // BAM typically sends debit notices for commissions
            log.warn("BAM typically sends debit notices, received: {}", avis.getTypeAvis());
        }
        
        // Validate rejection reason for rejected status  
        if (avis.getCommentaire() != null && avis.getCommentaire().startsWith("REJECTED:") && 
            (avis.getCommentaire().length() <= 10)) {
            throw new IllegalArgumentException("Rejection reason is required for rejected advice");
        }
    }
    
    /**
     * Get pending advice notices for active auctions
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getPendingAdvicesForActiveAuctions() {
        return avisAdjudicationRepository.findPendingAdvicesForActiveAuctions();
    }
    
    /**
     * Get large advice notices in period
     */
    @Transactional(readOnly = true)
    public List<AvisAdjudication> getLargeAdvicesInPeriod(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        return avisAdjudicationRepository.findLargeAdvicesInPeriod(startDate, endDate, minAmount);
    }
    
    /**
     * Get count by type and status
     */
    @Transactional(readOnly = true)
    public Long getCountByTypeAndStatus(AvisAdjudication.TypeAvis typeAvis, AvisAdjudication.StatutAvis statut) {
        return avisAdjudicationRepository.countByTypeAndStatus(typeAvis, statut);
    }
    
    /**
     * Get total accounted amount by type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAccountedAmountByType(AvisAdjudication.TypeAvis typeAvis) {
        BigDecimal total = avisAdjudicationRepository.getTotalAccountedAmountByType(typeAvis);
        return total != null ? total : BigDecimal.ZERO;
    }
}