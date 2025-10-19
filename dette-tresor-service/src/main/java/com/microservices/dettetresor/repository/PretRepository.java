package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.Pret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PretRepository extends JpaRepository<Pret, Long> {
    
    /**
     * Find loan by loan number
     */
    Optional<Pret> findByNumeroPret(String numeroPret);
    
    /**
     * Check if loan number exists
     */
    boolean existsByNumeroPret(String numeroPret);
    
    /**
     * Find loans by lending organization
     */
    List<Pret> findByOrganismeBailleurContainingIgnoreCase(String organismeBailleur);
    
    /**
     * Find loans by currency
     */
    List<Pret> findByDevise(String devise);
    
    /**
     * Find loans with current balance greater than zero
     */
    @Query("SELECT p FROM Pret p WHERE p.soldeCourant > 0")
    List<Pret> findActiveLoans();
    
    /**
     * Find loans with current balance equal to zero (fully paid)
     */
    @Query("SELECT p FROM Pret p WHERE p.soldeCourant = 0")
    List<Pret> findFullyPaidLoans();
    
    /**
     * Find loans by signature date range
     */
    @Query("SELECT p FROM Pret p WHERE p.dateSignature BETWEEN :startDate AND :endDate")
    List<Pret> findBySignatureDateBetween(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    /**
     * Find loans with total amount greater than specified value
     */
    @Query("SELECT p FROM Pret p WHERE p.montantTotal > :amount")
    List<Pret> findByTotalAmountGreaterThan(@Param("amount") BigDecimal amount);
    
    /**
     * Find loans with interest rate between range
     */
    @Query("SELECT p FROM Pret p WHERE p.tauxInteret BETWEEN :minRate AND :maxRate")
    List<Pret> findByInterestRateBetween(@Param("minRate") BigDecimal minRate, 
                                        @Param("maxRate") BigDecimal maxRate);
    
    /**
     * Search loans by multiple criteria
     */
    @Query("SELECT p FROM Pret p WHERE " +
           "(:numeroPret IS NULL OR :numeroPret = '' OR LOWER(p.numeroPret) LIKE LOWER(CONCAT('%', :numeroPret, '%'))) AND " +
           "(:organismeBailleur IS NULL OR :organismeBailleur = '' OR LOWER(p.organismeBailleur) LIKE LOWER(CONCAT('%', :organismeBailleur, '%'))) AND " +
           "(:devise IS NULL OR :devise = '' OR LOWER(p.devise) = LOWER(:devise))")
    List<Pret> searchLoans(@Param("numeroPret") String numeroPret,
                          @Param("organismeBailleur") String organismeBailleur,
                          @Param("devise") String devise);
    
    /**
     * Get total outstanding debt by currency
     */
    @Query("SELECT p.devise, SUM(p.soldeCourant) FROM Pret p WHERE p.soldeCourant > 0 GROUP BY p.devise")
    List<Object[]> getTotalOutstandingDebtByCurrency();
    
    /**
     * Count loans by lending organization
     */
    @Query("SELECT p.organismeBailleur, COUNT(p) FROM Pret p GROUP BY p.organismeBailleur")
    List<Object[]> countLoansByLendingOrganization();
}