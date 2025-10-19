package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.Commission;
import com.microservices.detteinterieur.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommissionService {
    
    private final CommissionRepository commissionRepository;
    
    /**
     * Create a new commission
     */
    public Commission createCommission(Commission commission) {
        log.info("Creating new commission: {} - {}", commission.getTypeCommission(), commission.getNumeroReference());
        
        // Validate reference number uniqueness
        if (commission.getNumeroReference() != null && commissionRepository.existsByNumeroReference(commission.getNumeroReference())) {
            throw new IllegalArgumentException("Reference number already exists: " + commission.getNumeroReference());
        }
        
        // Set default status if not provided
        if (commission.getStatut() == null) {
            commission.setStatut(Commission.StatutCommission.EN_ATTENTE);
        }
        
        // Commission amount is already provided in the montant field
        
        // Validate business rules
        validateBusinessRules(commission);
        
        Commission savedCommission = commissionRepository.save(commission);
        log.info("Commission created successfully with ID: {}", savedCommission.getId());
        return savedCommission;
    }
    
    /**
     * Update an existing commission
     */
    public Commission updateCommission(Long id, Commission commission) {
        log.info("Updating commission with ID: {}", id);
        
        Commission existingCommission = getCommissionById(id);
        
        // Validate reference number uniqueness (excluding current commission)
        if (commission.getNumeroReference() != null && 
            !existingCommission.getNumeroReference().equals(commission.getNumeroReference()) &&
            commissionRepository.existsByNumeroReference(commission.getNumeroReference())) {
            throw new IllegalArgumentException("Reference number already exists: " + commission.getNumeroReference());
        }
        
        // Prevent modification of paid commissions
        if (existingCommission.isPaye()) {
            throw new IllegalArgumentException("Cannot modify paid commission");
        }
        
        // Update fields
        existingCommission.setNumeroReference(commission.getNumeroReference());
        existingCommission.setMontant(commission.getMontant());
        existingCommission.setTypeCommission(commission.getTypeCommission());
        existingCommission.setDescription(commission.getDescription());
        existingCommission.setCommentaire(commission.getCommentaire());
        
        // Validate business rules
        validateBusinessRules(existingCommission);
        
        Commission updatedCommission = commissionRepository.save(existingCommission);
        log.info("Commission updated successfully: {}", updatedCommission.getNumeroReference());
        return updatedCommission;
    }
    
    /**
     * Get commission by ID
     */
    @Transactional(readOnly = true)
    public Commission getCommissionById(Long id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commission not found with ID: " + id));
    }
    
    /**
     * Get commission by reference number
     */
    @Transactional(readOnly = true)
    public Optional<Commission> getCommissionByReferenceNumber(String numeroReference) {
        return commissionRepository.findByNumeroReference(numeroReference);
    }
    
    /**
     * Get all commissions with pagination
     */
    @Transactional(readOnly = true)
    public Page<Commission> getAllCommissions(Pageable pageable) {
        return commissionRepository.findAll(pageable);
    }
    
    /**
     * Get all commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }
    
    /**
     * Delete commission
     */
    public void deleteCommission(Long id) {
        log.info("Deleting commission with ID: {}", id);
        
        Commission commission = getCommissionById(id);
        
        // Prevent deletion of paid commissions
        if (commission.isPaye()) {
            throw new IllegalArgumentException("Cannot delete paid commission");
        }
        
        commissionRepository.delete(commission);
        log.info("Commission deleted successfully: {}", commission.getNumeroReference());
    }
    
    /**
     * Get commissions by type
     */
    @Transactional(readOnly = true)
    public List<Commission> getCommissionsByType(Commission.TypeCommission typeCommission) {
        return commissionRepository.findByTypeCommissionOrderByCreatedAtDesc(typeCommission);
    }
    
    /**
     * Get Maroclear commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getMaroclearCommissions() {
        return commissionRepository.findMaroclearCommissions();
    }
    
    /**
     * Get BAM commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getBAMCommissions() {
        return commissionRepository.findBAMCommissions();
    }
    
    /**
     * Get commissions by status
     */
    @Transactional(readOnly = true)
    public List<Commission> getCommissionsByStatus(Commission.StatutCommission statut) {
        return commissionRepository.findByStatutOrderByCreatedAtDesc(statut);
    }
    
    /**
     * Get pending commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getPendingCommissions() {
        return commissionRepository.findByStatutOrderByCreatedAtDesc(Commission.StatutCommission.EN_ATTENTE);
    }
    
    /**
     * Get processing commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getProcessingCommissions() {
        return commissionRepository.findByStatutOrderByCreatedAtDesc(Commission.StatutCommission.EN_COURS_TRAITEMENT);
    }
    
    /**
     * Get paid commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> getPaidCommissions() {
        return commissionRepository.findByStatutOrderByCreatedAtDesc(Commission.StatutCommission.PAYE);
    }
    
    /**
     * Get commissions by date range (using createdAt)
     */
    @Transactional(readOnly = true)
    public List<Commission> getCommissionsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return commissionRepository.findByCreatedAtBetween(startDateTime, endDateTime);
    }
    
    /**
     * Get commissions by amount range
     */
    @Transactional(readOnly = true)
    public List<Commission> getCommissionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return commissionRepository.findByMontantBetween(minAmount, maxAmount);
    }
    
    /**
     * Search commissions
     */
    @Transactional(readOnly = true)
    public List<Commission> searchCommissions(String searchTerm) {
        return commissionRepository.searchCommissions(searchTerm);
    }
    
    /**
     * Process commission (change status to EN_COURS_TRAITEMENT)
     */
    public Commission processCommission(Long id) {
        log.info("Processing commission with ID: {}", id);
        
        Commission commission = getCommissionById(id);
        
        if (!commission.isEnAttente()) {
            throw new IllegalArgumentException("Only pending commissions can be processed");
        }
        
        commission.setStatut(Commission.StatutCommission.EN_COURS_TRAITEMENT);
        
        Commission processedCommission = commissionRepository.save(commission);
        log.info("Commission processed successfully: {}", processedCommission.getNumeroReference());
        return processedCommission;
    }
    
    /**
     * Pay commission (change status to PAYE)
     */
    public Commission payCommission(Long id) {
        log.info("Paying commission with ID: {}", id);
        
        Commission commission = getCommissionById(id);
        
        if (!commission.isEnCoursTraitement()) {
            throw new IllegalArgumentException("Only processing commissions can be paid");
        }
        
        commission.setStatut(Commission.StatutCommission.PAYE);
        commission.setDatePaiement(LocalDateTime.now());
        
        Commission paidCommission = commissionRepository.save(commission);
        log.info("Commission paid successfully: {}", paidCommission.getNumeroReference());
        return paidCommission;
    }
    
    /**
     * Cancel commission
     */
    public Commission cancelCommission(Long id, String motifAnnulation) {
        log.info("Cancelling commission with ID: {}", id);
        
        Commission commission = getCommissionById(id);
        
        if (commission.isPaye()) {
            throw new IllegalArgumentException("Cannot cancel paid commission");
        }
        
        if (motifAnnulation == null || motifAnnulation.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        
        commission.setStatut(Commission.StatutCommission.REJETE);
        commission.setCommentaire(motifAnnulation);
        
        Commission cancelledCommission = commissionRepository.save(commission);
        log.info("Commission cancelled successfully: {}", cancelledCommission.getNumeroReference());
        return cancelledCommission;
    }
    
    /**
     * Get commissions to pay
     */
    @Transactional(readOnly = true)
    public List<Commission> getCommissionsToPay() {
        return commissionRepository.findByStatutOrderByCreatedAtDesc(Commission.StatutCommission.EN_COURS_TRAITEMENT);
    }
    
    /**
     * Get overdue commissions (created before threshold and still pending)
     */
    @Transactional(readOnly = true)
    public List<Commission> getOverdueCommissions(LocalDate threshold) {
        LocalDateTime thresholdDateTime = threshold.atStartOfDay();
        return commissionRepository.findAll().stream()
                .filter(c -> c.getCreatedAt().isBefore(thresholdDateTime))
                .filter(c -> c.isEnAttente() || c.isEnCoursTraitement())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get large commissions in period
     */
    @Transactional(readOnly = true)
    public List<Commission> getLargeCommissionsInPeriod(LocalDate startDate, LocalDate endDate, BigDecimal minAmount) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return commissionRepository.findByMontantGreaterThan(minAmount);
    }
    
    // Simplified statistics methods using basic repository methods
    
    /**
     * Get commission statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCommissionStatisticsByType() {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get commission statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCommissionStatisticsByStatus() {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get monthly commission report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyCommissionReport(LocalDate startDate, LocalDate endDate) {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get commission performance analysis
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCommissionPerformanceAnalysis() {
        // This would need to be implemented in repository or calculated here
        return List.of(); // Placeholder
    }
    
    /**
     * Get count by type and status
     */
    @Transactional(readOnly = true)
    public Long getCountByTypeAndStatus(Commission.TypeCommission typeCommission, Commission.StatutCommission statut) {
        return (long) commissionRepository.findByTypeCommissionAndStatut(typeCommission, statut).size();
    }
    
    /**
     * Get total paid amount by type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmountByType(Commission.TypeCommission typeCommission) {
        List<Commission> paidCommissions = commissionRepository.findByTypeCommissionAndStatut(typeCommission, Commission.StatutCommission.PAYE);
        return paidCommissions.stream()
                .map(Commission::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total pending amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        List<Commission> pendingCommissions = commissionRepository.findByStatut(Commission.StatutCommission.EN_ATTENTE);
        List<Commission> processingCommissions = commissionRepository.findByStatut(Commission.StatutCommission.EN_COURS_TRAITEMENT);
        
        BigDecimal pendingTotal = pendingCommissions.stream()
                .map(Commission::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal processingTotal = processingCommissions.stream()
                .map(Commission::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return pendingTotal.add(processingTotal);
    }
    
    /**
     * Get calculated commissions (for compatibility)
     */
    @Transactional(readOnly = true)
    public List<Commission> getCalculatedCommissions() {
        return getPendingCommissions();
    }
    
    /**
     * Get average commission rate by type (simplified)
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageCommissionRateByType(Commission.TypeCommission typeCommission) {
        return typeCommission.equals(Commission.TypeCommission.MAROCLEAR) 
            ? BigDecimal.valueOf(0.002) : BigDecimal.valueOf(0.001);
    }
    
    /**
     * Get average processing time by type (placeholder)
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageProcessingTimeByType() {
        return List.of(); // Placeholder
    }
    
    /**
     * Validate business rules for commissions
     */
    private void validateBusinessRules(Commission commission) {
        // Validate commission amount
        if (commission.getMontant() != null && commission.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Commission amount must be positive");
        }
    }
}