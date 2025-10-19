package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.Adjudication;
import com.microservices.detteinterieur.entity.AvisAdjudication;
import com.microservices.detteinterieur.repository.AdjudicationRepository;
import com.microservices.detteinterieur.repository.AvisAdjudicationRepository;
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
public class AdjudicationService {
    
    private final AdjudicationRepository adjudicationRepository;
    private final AvisAdjudicationRepository avisAdjudicationRepository;
    
    /**
     * Create a new auction
     */
    public Adjudication createAdjudication(Adjudication adjudication) {
        log.info("Creating new auction: {}", adjudication.getNumeroAdjud());
        
        // Validate auction number uniqueness
        if (adjudicationRepository.existsByNumeroAdjud(adjudication.getNumeroAdjud())) {
            throw new IllegalArgumentException("Auction number already exists: " + adjudication.getNumeroAdjud());
        }
        
        // Validate business rules
        validateAdjudicationBusinessRules(adjudication);
        
        Adjudication savedAdjudication = adjudicationRepository.save(adjudication);
        log.info("Auction created successfully with ID: {}", savedAdjudication.getId());
        return savedAdjudication;
    }
    
    /**
     * Update an existing auction
     */
    public Adjudication updateAdjudication(Long id, Adjudication adjudication) {
        log.info("Updating auction with ID: {}", id);
        
        Adjudication existingAdjudication = getAdjudicationById(id);
        
        // Validate auction number uniqueness (excluding current auction)
        if (!existingAdjudication.getNumeroAdjud().equals(adjudication.getNumeroAdjud()) &&
            adjudicationRepository.existsByNumeroAdjud(adjudication.getNumeroAdjud())) {
            throw new IllegalArgumentException("Auction number already exists: " + adjudication.getNumeroAdjud());
        }
        
        // Update fields
        existingAdjudication.setNumeroAdjud(adjudication.getNumeroAdjud());
        existingAdjudication.setDateAdjud(adjudication.getDateAdjud());
        existingAdjudication.setMontantTotal(adjudication.getMontantTotal());
        existingAdjudication.setStatut(adjudication.getStatut());
        
        // Validate business rules
        validateAdjudicationBusinessRules(existingAdjudication);
        
        Adjudication updatedAdjudication = adjudicationRepository.save(existingAdjudication);
        log.info("Auction updated successfully: {}", updatedAdjudication.getNumeroAdjud());
        return updatedAdjudication;
    }
    
    /**
     * Get auction by ID
     */
    @Transactional(readOnly = true)
    public Adjudication getAdjudicationById(Long id) {
        return adjudicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found with ID: " + id));
    }
    
    /**
     * Get auction by number
     */
    @Transactional(readOnly = true)
    public Optional<Adjudication> getAdjudicationByNumber(String numeroAdjud) {
        return adjudicationRepository.findByNumeroAdjud(numeroAdjud);
    }
    
    /**
     * Get all auctions with pagination
     */
    @Transactional(readOnly = true)
    public Page<Adjudication> getAllAdjudications(Pageable pageable) {
        return adjudicationRepository.findAll(pageable);
    }
    
    /**
     * Get all auctions
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAllAdjudications() {
        return adjudicationRepository.findAll();
    }
    
    /**
     * Delete auction
     */
    public void deleteAdjudication(Long id) {
        log.info("Deleting auction with ID: {}", id);
        
        Adjudication adjudication = getAdjudicationById(id);
        
        // Check if auction has associated advice notices
        if (!adjudication.getAvisAdjudications().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete auction with existing advice notices");
        }
        
        adjudicationRepository.delete(adjudication);
        log.info("Auction deleted successfully: {}", adjudication.getNumeroAdjud());
    }
    
    /**
     * Get auctions by status
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsByStatus(Adjudication.StatutAdjudication statut) {
        return adjudicationRepository.findByStatutOrderByDateAdjudDesc(statut);
    }
    
    /**
     * Get active auctions
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getActiveAdjudications() {
        return adjudicationRepository.findActiveAuctions();
    }
    
    /**
     * Get closed auctions
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getClosedAdjudications() {
        return adjudicationRepository.findClosedAuctions();
    }
    
    /**
     * Get cancelled auctions
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getCancelledAdjudications() {
        return adjudicationRepository.findCancelledAuctions();
    }
    
    /**
     * Get auctions by date range
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return adjudicationRepository.findByDateAdjudBetween(startDate, endDate);
    }
    
    /**
     * Get auctions by amount range
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return adjudicationRepository.findByMontantTotalBetween(minAmount, maxAmount);
    }
    
    /**
     * Search auctions by number
     */
    @Transactional(readOnly = true)
    public List<Adjudication> searchAdjudications(String searchTerm) {
        return adjudicationRepository.searchByNumeroAdjud(searchTerm);
    }
    
    /**
     * Close auction
     */
    public Adjudication closeAdjudication(Long id) {
        log.info("Closing auction with ID: {}", id);
        
        Adjudication adjudication = getAdjudicationById(id);
        
        if (!adjudication.isActif()) {
            throw new IllegalArgumentException("Only active auctions can be closed");
        }
        
        adjudication.setStatut(Adjudication.StatutAdjudication.CLOTUREE);
        
        Adjudication closedAdjudication = adjudicationRepository.save(adjudication);
        log.info("Auction closed successfully: {}", closedAdjudication.getNumeroAdjud());
        return closedAdjudication;
    }
    
    /**
     * Cancel auction with reason
     */
    public Adjudication cancelAdjudication(Long id, String motifAnnulation) {
        log.info("Cancelling auction with ID: {} with reason: {}", id, motifAnnulation);
        
        Adjudication adjudication = getAdjudicationById(id);
        
        if (adjudication.isCloture()) {
            throw new IllegalArgumentException("Cannot cancel closed auction");
        }
        
        if (motifAnnulation == null || motifAnnulation.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        
        // Check for unprocessed advice notices
        List<AvisAdjudication> pendingAdvices = avisAdjudicationRepository
                .findByAdjudicationIdAndStatut(id, AvisAdjudication.StatutAvis.PRIS_EN_CHARGE);
        
        if (!pendingAdvices.isEmpty()) {
            throw new IllegalArgumentException("Cannot cancel auction with pending advice notices");
        }
        
        adjudication.setStatut(Adjudication.StatutAdjudication.ANNULEE);
        
        Adjudication cancelledAdjudication = adjudicationRepository.save(adjudication);
        log.info("Auction cancelled successfully: {}", cancelledAdjudication.getNumeroAdjud());
        return cancelledAdjudication;
    }
    
    /**
     * Get auction statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdjudicationStatisticsByStatus() {
        // Placeholder implementation - would need to be implemented in repository
        return List.of(); 
    }
    
    /**
     * Get count by status
     */
    @Transactional(readOnly = true)
    public Long getCountByStatus(Adjudication.StatutAdjudication statut) {
        return adjudicationRepository.countByStatut(statut);
    }
    
    /**
     * Get large auctions in period
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getLargeAdjudicationsInPeriod(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        return adjudicationRepository.findByMontantTotalGreaterThan(minAmount);
    }
    
    /**
     * Get auction statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdjudicationStatistics() {
        return adjudicationRepository.getAuctionStatistics();
    }
    
    /**
     * Get auction count by status
     */
    @Transactional(readOnly = true)
    public Long getAdjudicationCountByStatus(Adjudication.StatutAdjudication statut) {
        return adjudicationRepository.countByStatut(statut);
    }
    
    /**
     * Get total amount by status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(Adjudication.StatutAdjudication statut) {
        return adjudicationRepository.getTotalAmountByStatut(statut);
    }
    
    /**
     * Get auctions with advice notices
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsWithAdvices() {
        return adjudicationRepository.findAuctionsWithAdvices();
    }
    
    /**
     * Get auctions without advice notices
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsWithoutAdvices() {
        return adjudicationRepository.findAuctionsWithoutAdvices();
    }
    
    /**
     * Get auctions with pending advice notices
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getAdjudicationsWithPendingAdvices() {
        return adjudicationRepository.findAuctionsWithPendingAdvices();
    }
    
    /**
     * Get monthly auction report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyAdjudicationReport(LocalDate startDate, LocalDate endDate) {
        return adjudicationRepository.getMonthlyAuctionReport(startDate, endDate);
    }
    
    /**
     * Get yearly auction report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getYearlyAdjudicationReport() {
        return adjudicationRepository.getYearlyAuctionReport();
    }
    
    /**
     * Get auctions with financial summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdjudicationsWithFinancialSummary() {
        return adjudicationRepository.getAuctionsWithFinancialSummary();
    }
    
    /**
     * Get recent auctions
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getRecentAdjudications(int limit) {
        return adjudicationRepository.findRecentAuctions(limit);
    }
    
    /**
     * Get large auctions by status
     */
    @Transactional(readOnly = true)
    public List<Adjudication> getLargeAdjudicationsByStatus(BigDecimal minAmount, 
                                                           List<Adjudication.StatutAdjudication> statuts) {
        return adjudicationRepository.findLargeAuctionsByStatus(minAmount, statuts);
    }
    
    /**
     * Calculate auction financial summary
     */
    @Transactional(readOnly = true)
    public AdjudicationFinancialSummary calculateFinancialSummary(Long id) {
        Adjudication adjudication = getAdjudicationById(id);
        
        BigDecimal totalCredits = adjudication.getTotalCredits();
        BigDecimal totalDebits = adjudication.getTotalDebits();
        BigDecimal soldeNet = adjudication.getSoldeNet();
        
        return AdjudicationFinancialSummary.builder()
                .adjudicationId(adjudication.getId())
                .numeroAdjud(adjudication.getNumeroAdjud())
                .montantTotal(adjudication.getMontantTotal())
                .totalCredits(totalCredits)
                .totalDebits(totalDebits)
                .soldeNet(soldeNet)
                .nombreAvis(adjudication.getAvisAdjudications().size())
                .statut(adjudication.getStatut())
                .build();
    }
    
    /**
     * Validate auction business rules
     */
    private void validateAdjudicationBusinessRules(Adjudication adjudication) {
        // Validate amount
        if (adjudication.getMontantTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Auction total amount must be positive");
        }
        
        // Validate date
        if (adjudication.getDateAdjud().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Auction date cannot be in the future");
        }
        
        // Additional business rules can be added here
    }
    
    /**
     * Financial summary DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AdjudicationFinancialSummary {
        private Long adjudicationId;
        private String numeroAdjud;
        private BigDecimal montantTotal;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private BigDecimal soldeNet;
        private Integer nombreAvis;
        private Adjudication.StatutAdjudication statut;
    }
}