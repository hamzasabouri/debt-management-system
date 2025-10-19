package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.PretDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PretService {
    
    /**
     * Create a new loan
     */
    PretDTO createLoan(PretDTO pretDTO);
    
    /**
     * Update an existing loan
     */
    PretDTO updateLoan(Long id, PretDTO pretDTO);
    
    /**
     * Find loan by ID
     */
    PretDTO findById(Long id);
    
    /**
     * Find loan by ID with Echeanciers (detailed view)
     */
    PretDTO findByIdWithEcheanciers(Long id);
    
    /**
     * Find loan by loan number
     */
    PretDTO findByLoanNumber(String numeroPret);
    
    /**
     * Find all loans
     */
    List<PretDTO> findAllLoans();
    
    /**
     * Find active loans (with outstanding balance)
     */
    List<PretDTO> findActiveLoans();
    
    /**
     * Find fully paid loans
     */
    List<PretDTO> findFullyPaidLoans();
    
    /**
     * Find loans by lending organization
     */
    List<PretDTO> findByLendingOrganization(String organismeBailleur);
    
    /**
     * Find loans by currency
     */
    List<PretDTO> findByCurrency(String devise);
    
    /**
     * Find loans by signature date range
     */
    List<PretDTO> findBySignatureDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Search loans by multiple criteria
     */
    List<PretDTO> searchLoans(String numeroPret, String organismeBailleur, String devise);
    
    /**
     * Update loan current balance
     */
    PretDTO updateCurrentBalance(Long id, BigDecimal newBalance);
    
    /**
     * Calculate and update current balance based on payments
     */
    PretDTO recalculateCurrentBalance(Long id);
    
    /**
     * Delete loan
     */
    void deleteLoan(Long id);
    
    /**
     * Get total outstanding debt by currency
     */
    List<Object[]> getTotalOutstandingDebtByCurrency();
    
    /**
     * Check if loan number exists
     */
    boolean existsByLoanNumber(String numeroPret);
}