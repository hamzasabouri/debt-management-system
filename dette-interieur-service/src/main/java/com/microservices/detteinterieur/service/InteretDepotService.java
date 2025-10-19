package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.InteretDepot;
import com.microservices.detteinterieur.repository.InteretDepotRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InteretDepotService {
    
    private final InteretDepotRepository interetDepotRepository;
    
    /**
     * Create a new interest calculation
     */
    public InteretDepot createInteret(InteretDepot interet) {
        log.info("Creating new interest calculation for account: {}", interet.getNumeroCompte());
        
        // Validate uniqueness of account and calculation date
        if (interetDepotRepository.existsByNumeroCompteAndDateCalcul(interet.getNumeroCompte(), interet.getDateCalcul())) {
            throw new IllegalArgumentException("Interest already calculated for account " + interet.getNumeroCompte() + 
                    " on date " + interet.getDateCalcul());
        }
        
        // Set default status if not provided
        if (interet.getStatut() == null) {
            interet.setStatut(InteretDepot.StatutInteret.CALCULE);
        }
        
        // Auto-calculate interest if amount is not provided
        if (interet.getMontant() == null) {
            interet.setMontant(calculateInterest(interet));
        }
        
        // Validate business rules
        validateBusinessRules(interet);
        
        InteretDepot savedInteret = interetDepotRepository.save(interet);
        log.info("Interest calculation created successfully with ID: {}", savedInteret.getId());
        return savedInteret;
    }
    
    /**
     * Update an existing interest calculation
     */
    public InteretDepot updateInteret(Long id, InteretDepot interet) {
        log.info("Updating interest calculation with ID: {}", id);
        
        InteretDepot existingInteret = getInteretById(id);
        
        // Validate uniqueness (excluding current interest)
        if ((!existingInteret.getNumeroCompte().equals(interet.getNumeroCompte()) ||
             !existingInteret.getDateCalcul().equals(interet.getDateCalcul())) &&
            interetDepotRepository.existsByNumeroCompteAndDateCalcul(interet.getNumeroCompte(), interet.getDateCalcul())) {
            throw new IllegalArgumentException("Interest already calculated for account " + interet.getNumeroCompte() + 
                    " on date " + interet.getDateCalcul());
        }
        
        // Prevent modification of transmitted or accounted interests
        if (existingInteret.isTransmis() || existingInteret.isComptabilise()) {
            throw new IllegalArgumentException("Cannot modify transmitted or accounted interest calculation");
        }
        
        // Update fields
        existingInteret.setNumeroCompte(interet.getNumeroCompte());
        existingInteret.setDateCalcul(interet.getDateCalcul());
        existingInteret.setPeriodeDebut(interet.getPeriodeDebut());
        existingInteret.setPeriodeFin(interet.getPeriodeFin());
        existingInteret.setMontantPrincipal(interet.getMontantPrincipal());
        existingInteret.setTauxInteret(interet.getTauxInteret());
        existingInteret.setTypeFonds(interet.getTypeFonds());
        existingInteret.setNomTitulaire(interet.getNomTitulaire());
        existingInteret.setOrganisme(interet.getOrganisme());
        existingInteret.setDevise(interet.getDevise());
        existingInteret.setCommentaire(interet.getCommentaire());
        
        // Recalculate interest if necessary
        if (interet.getMontant() == null) {
            existingInteret.setMontant(calculateInterest(existingInteret));
        } else {
            existingInteret.setMontant(interet.getMontant());
        }
        
        // Validate business rules
        validateBusinessRules(existingInteret);
        
        InteretDepot updatedInteret = interetDepotRepository.save(existingInteret);
        log.info("Interest calculation updated successfully for account: {}", updatedInteret.getNumeroCompte());
        return updatedInteret;
    }
    
    /**
     * Get interest calculation by ID
     */
    @Transactional(readOnly = true)
    public InteretDepot getInteretById(Long id) {
        return interetDepotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interest calculation not found with ID: " + id));
    }
    
    /**
     * Get interest calculation by account and date
     */
    @Transactional(readOnly = true)
    public Optional<InteretDepot> getInteretByAccountAndDate(String numeroCompte, LocalDate dateCalcul) {
        return interetDepotRepository.findByNumeroCompteAndDateCalcul(numeroCompte, dateCalcul);
    }
    
    /**
     * Get all interest calculations with pagination
     */
    @Transactional(readOnly = true)
    public Page<InteretDepot> getAllInterets(Pageable pageable) {
        return interetDepotRepository.findAll(pageable);
    }
    
    /**
     * Get all interest calculations
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getAllInterets() {
        return interetDepotRepository.findAll();
    }
    
    /**
     * Delete interest calculation
     */
    public void deleteInteret(Long id) {
        log.info("Deleting interest calculation with ID: {}", id);
        
        InteretDepot interet = getInteretById(id);
        
        // Prevent deletion of transmitted or accounted interests
        if (interet.isTransmis() || interet.isComptabilise()) {
            throw new IllegalArgumentException("Cannot delete transmitted or accounted interest calculation");
        }
        
        interetDepotRepository.delete(interet);
        log.info("Interest calculation deleted successfully for account: {}", interet.getNumeroCompte());
    }
    
    /**
     * Get interest calculations by account
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByAccount(String numeroCompte) {
        return interetDepotRepository.findByNumeroCompte(numeroCompte);
    }
    
    /**
     * Get interest calculations by fund type
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByFundType(InteretDepot.TypeFonds typeFonds) {
        return interetDepotRepository.findByTypeFondsOrderByDateCalculDesc(typeFonds);
    }
    
    /**
     * Get local collectivities interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getLocalCollectivitiesInterests() {
        return interetDepotRepository.findLocalCollectivitiesInterests();
    }
    
    /**
     * Get treasury deposits interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getTreasuryDepositsInterests() {
        return interetDepotRepository.findTreasuryDepositsInterests();
    }
    
    /**
     * Get interest calculations by status
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByStatus(InteretDepot.StatutInteret statut) {
        return interetDepotRepository.findByStatutOrderByDateCalculDesc(statut);
    }
    
    /**
     * Get calculated interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getCalculatedInterests() {
        return interetDepotRepository.findCalculatedInterests();
    }
    
    /**
     * Get pending interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getPendingInterests() {
        return interetDepotRepository.findPendingInterests();
    }
    
    /**
     * Get accounted interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getAccountedInterests() {
        return interetDepotRepository.findAccountedInterests();
    }
    
    /**
     * Get transmitted interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getTransmittedInterests() {
        return interetDepotRepository.findTransmittedInterests();
    }
    
    /**
     * Get rejected interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getRejectedInterests() {
        return interetDepotRepository.findRejectedInterests();
    }
    
    /**
     * Get interest calculations by date range
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByDateRange(LocalDate startDate, LocalDate endDate) {
        return interetDepotRepository.findByDateCalculBetween(startDate, endDate);
    }
    
    /**
     * Get interest calculations by amount range
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return interetDepotRepository.findByMontantBetween(minAmount, maxAmount);
    }
    
    /**
     * Get interest calculations by currency
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getInteretsByCurrency(String devise) {
        return interetDepotRepository.findByDeviseOrderByMontantDesc(devise);
    }
    
    /**
     * Search interest calculations
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> searchInterets(String searchTerm) {
        return interetDepotRepository.searchInterests(searchTerm);
    }
    
    /**
     * Process interest (change status to PRIS_EN_CHARGE)
     */
    public InteretDepot processInterest(Long id) {
        log.info("Processing interest calculation with ID: {}", id);
        
        InteretDepot interet = getInteretById(id);
        
        if (!interet.isCalcule()) {
            throw new IllegalArgumentException("Only calculated interests can be processed");
        }
        
        interet.setStatut(InteretDepot.StatutInteret.PRIS_EN_CHARGE);
        interet.setDateTraitement(LocalDateTime.now());
        
        InteretDepot processedInteret = interetDepotRepository.save(interet);
        log.info("Interest calculation processed successfully for account: {}", processedInteret.getNumeroCompte());
        return processedInteret;
    }
    
    /**
     * Account interest (change status to COMPTABILISE)
     */
    public InteretDepot accountInterest(Long id) {
        log.info("Accounting interest calculation with ID: {}", id);
        
        InteretDepot interet = getInteretById(id);
        
        if (!interet.isPrisEnCharge()) {
            throw new IllegalArgumentException("Only processed interests can be accounted");
        }
        
        interet.comptabiliser();
        
        InteretDepot accountedInteret = interetDepotRepository.save(interet);
        log.info("Interest calculation accounted successfully for account: {}", accountedInteret.getNumeroCompte());
        return accountedInteret;
    }
    
    /**
     * Transmit interest (change status to TRANSMIS)
     */
    public InteretDepot transmitInterest(Long id, Long avisCreditId) {
        log.info("Transmitting interest calculation with ID: {}", id);
        
        InteretDepot interet = getInteretById(id);
        
        if (!interet.isComptabilise()) {
            throw new IllegalArgumentException("Only accounted interests can be transmitted");
        }
        
        interet.setAvisCreditId(avisCreditId);
        interet.setStatut(InteretDepot.StatutInteret.TRANSMIS);
        interet.setDateTraitement(LocalDateTime.now());
        
        InteretDepot transmittedInteret = interetDepotRepository.save(interet);
        log.info("Interest calculation transmitted successfully for account: {}", transmittedInteret.getNumeroCompte());
        return transmittedInteret;
    }
    
    /**
     * Reject interest calculation
     */
    public InteretDepot rejectInterest(Long id, String motifRejet) {
        log.info("Rejecting interest calculation with ID: {}", id);
        
        InteretDepot interet = getInteretById(id);
        
        // Can reject from any status except already rejected
        if (interet.isRejete()) {
            throw new IllegalArgumentException("Interest calculation already rejected");
        }
        
        if (motifRejet == null || motifRejet.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        
        interet.setStatut(InteretDepot.StatutInteret.REJETE);
        interet.setCommentaire(motifRejet); // Use commentaire instead of motifRejet
        interet.setDateTraitement(LocalDateTime.now());
        
        InteretDepot rejectedInteret = interetDepotRepository.save(interet);
        log.info("Interest calculation rejected successfully for account: {}", rejectedInteret.getNumeroCompte());
        return rejectedInteret;
    }
    
    /**
     * Process interest (change status to PRIS_EN_CHARGE) - for backward compatibility
     */
    public InteretDepot processInteret(Long id) {
        return processInterest(id);
    }
    
    /**
     * Account interest (change status to COMPTABILISE) - for backward compatibility
     */
    public InteretDepot accountInteret(Long id) {
        return accountInterest(id);
    }
    
    /**
     * Transmit interest (change status to TRANSMIS) - for backward compatibility
     */
    public InteretDepot transmitInteret(Long id) {
        // For backward compatibility, we'll call transmitInterest with null avisCreditId
        return transmitInterest(id, null);
    }
    
    /**
     * Reject interest calculation - for backward compatibility
     */
    public InteretDepot rejectInteret(Long id, String motifRejet) {
        return rejectInterest(id, motifRejet);
    }
    
    /**
     * Calculate interest amount based on principal, rate, and period
     */
    private BigDecimal calculateInterest(InteretDepot interet) {
        if (interet.getMontantPrincipal() == null || interet.getTauxInteret() == null ||
            interet.getPeriodeDebut() == null || interet.getPeriodeFin() == null) {
            return BigDecimal.ZERO;
        }
        
        // Apply default interest rates if not specified
        BigDecimal tauxEffectif = interet.getTauxInteret();
        if (tauxEffectif.compareTo(BigDecimal.ZERO) == 0) {
            tauxEffectif = getDefaultInterestRate(interet.getTypeFonds());
        }
        
        // Calculate number of days
        long days = ChronoUnit.DAYS.between(interet.getPeriodeDebut(), interet.getPeriodeFin());
        
        // Calculate interest: principal * rate * days / (365 * 100)
        return interet.getMontantPrincipal()
                .multiply(tauxEffectif)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get default interest rate based on fund type
     */
    private BigDecimal getDefaultInterestRate(InteretDepot.TypeFonds typeFonds) {
        switch (typeFonds) {
            case COLLECTIVITE_LOCALE:
                return BigDecimal.valueOf(2.75); // 2.75% annual rate
            case DEPOT_TRESOR:
                return BigDecimal.valueOf(2.25); // 2.25% annual rate
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * Get interest statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInterestStatisticsByType() {
        return interetDepotRepository.getInterestStatisticsByType();
    }
    
    /**
     * Get interest statistics by fund type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInterestStatisticsByFundType() {
        return interetDepotRepository.getInterestStatisticsByType();
    }
    
    /**
     * Get interest statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInterestStatisticsByStatus() {
        return interetDepotRepository.getInterestStatisticsByStatus();
    }
    
    /**
     * Get interest statistics by currency
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInterestStatisticsByCurrency() {
        return interetDepotRepository.getInterestStatisticsByCurrency();
    }
    
    /**
     * Get detailed interest statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDetailedInterestStatistics() {
        return interetDepotRepository.getDetailedInterestStatistics();
    }
    
    /**
     * Get monthly interest report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyInterestReport(LocalDate startDate, LocalDate endDate) {
        return interetDepotRepository.getMonthlyInterestReport(startDate, endDate);
    }
    
    /**
     * Get yearly interest report
     */
    @Transactional(readOnly = true)
    public List<Object[]> getYearlyInterestReport() {
        return interetDepotRepository.getYearlyInterestReport();
    }
    
    /**
     * Get account holder statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAccountHolderStatistics() {
        return interetDepotRepository.getAccountHolderStatistics();
    }
    
    /**
     * Get organization statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getOrganizationStatistics() {
        return interetDepotRepository.getOrganizationStatistics();
    }
    
    /**
     * Get count by type and status
     */
    @Transactional(readOnly = true)
    public Long getCountByTypeAndStatus(InteretDepot.TypeFonds typeFonds, InteretDepot.StatutInteret statut) {
        return interetDepotRepository.countByTypeAndStatus(typeFonds, statut);
    }
    
    /**
     * Get total amount by fund type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByFundType(InteretDepot.TypeFonds typeFonds) {
        BigDecimal total = interetDepotRepository.getTotalAccountedAmountByType(typeFonds);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get total amount by status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(InteretDepot.StatutInteret statut) {
        BigDecimal total = interetDepotRepository.getTotalAmountByStatus(statut);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get total amount by currency
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByCurrency(String devise) {
        BigDecimal total = interetDepotRepository.getTotalAmountByCurrency(devise);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get total accounted amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAccountedAmount() {
        BigDecimal total = interetDepotRepository.getTotalAccountedAmount();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get total pending amount
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        BigDecimal total = interetDepotRepository.getTotalPendingAmount();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get average interest rate by type
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageInterestRateByType(InteretDepot.TypeFonds typeFonds) {
        BigDecimal avgRate = interetDepotRepository.getAverageInterestRateByType(typeFonds);
        return avgRate != null ? avgRate : BigDecimal.ZERO;
    }
    
    /**
     * Get interests ready to transmit for collectivities
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getReadyToTransmitCollectivitiesInterests() {
        return interetDepotRepository.findReadyToTransmitCollectivitiesInterests();
    }
    
    /**
     * Get interests ready to process for treasury
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getReadyToProcessTreasuryInterests() {
        return interetDepotRepository.findReadyToProcessTreasuryInterests();
    }
    
    /**
     * Get overdue interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getOverdueInterests(LocalDate threshold) {
        return interetDepotRepository.findOverdueInterests(threshold);
    }
    
    /**
     * Get recent interests
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getRecentInterests(int limit) {
        return interetDepotRepository.findRecentInterests(limit);
    }
    
    /**
     * Get largest interests since date
     */
    @Transactional(readOnly = true)
    public List<InteretDepot> getLargestInterestsSince(LocalDate date) {
        return interetDepotRepository.findLargestInterestsSince(date);
    }
    
    /**
     * Get average processing time in days
     */
    @Transactional(readOnly = true)
    public Double getAverageProcessingTimeInDays() {
        return interetDepotRepository.getAverageProcessingTimeInDays();
    }
    
    /**
     * Get average processing time by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageProcessingTimeByType() {
        return interetDepotRepository.getAverageProcessingTimeByType();
    }
    
    /**
     * Validate business rules for interest calculations
     */
    private void validateBusinessRules(InteretDepot interet) {
        // Validate principal amount
        if (interet.getMontantPrincipal() != null && interet.getMontantPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive");
        }
        
        // Validate interest amount
        if (interet.getMontant() != null && interet.getMontant().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest amount cannot be negative");
        }
        
        // Validate interest rate
        if (interet.getTauxInteret() != null && interet.getTauxInteret().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        // Validate period dates
        if (interet.getPeriodeDebut() != null && interet.getPeriodeFin() != null &&
            interet.getPeriodeDebut().isAfter(interet.getPeriodeFin())) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }
        
        // Validate calculation date not in future
        if (interet.getDateCalcul() != null && interet.getDateCalcul().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Calculation date cannot be in the future");
        }
        
        // Validate period end date not in future for collectivities
        if (interet.getTypeFonds() == InteretDepot.TypeFonds.COLLECTIVITE_LOCALE &&
            interet.getPeriodeFin() != null && interet.getPeriodeFin().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Period end date cannot be in the future for local collectivities");
        }
        
        // Validate interest amount consistency
        if (interet.getMontantPrincipal() != null && interet.getTauxInteret() != null &&
            interet.getPeriodeDebut() != null && interet.getPeriodeFin() != null && interet.getMontant() != null) {
            
            BigDecimal expectedInterest = calculateInterest(interet);
            if (interet.getMontant().compareTo(expectedInterest) != 0) {
                log.warn("Interest amount {} does not match expected {} for account {}", 
                        interet.getMontant(), expectedInterest, interet.getNumeroCompte());
            }
        }
        
        // Validate fund type specific rules
        if (interet.getTypeFonds() == InteretDepot.TypeFonds.COLLECTIVITE_LOCALE) {
            if (interet.getOrganisme() == null || interet.getOrganisme().trim().isEmpty()) {
                throw new IllegalArgumentException("Organization is required for local collectivities");
            }
        }
    }
}