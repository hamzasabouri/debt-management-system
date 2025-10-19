package com.microservices.meda.repository;

import com.microservices.meda.entity.Avance;
import com.microservices.meda.entity.Avance.TypeAvance;
import com.microservices.meda.entity.Avance.StatutAvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvanceRepository extends JpaRepository<Avance, Long> {
    
    /**
     * Find advance by number
     */
    Optional<Avance> findByNumeroAvance(String numeroAvance);
    
    /**
     * Check if advance number exists
     */
    boolean existsByNumeroAvance(String numeroAvance);
    
    /**
     * Check if advance number exists (excluding specific ID)
     */
    boolean existsByNumeroAvanceAndIdNot(String numeroAvance, Long id);
    
    /**
     * Find advances by project ID
     */
    List<Avance> findByProjetIdOrderByDateReception(Long projetId);
    
    /**
     * Find advances by project ID and type
     */
    List<Avance> findByProjetIdAndTypeAvanceOrderByDateReception(Long projetId, TypeAvance typeAvance);
    
    /**
     * Find advances by project ID and status
     */
    List<Avance> findByProjetIdAndStatutOrderByDateReception(Long projetId, StatutAvance statut);
    
    /**
     * Find advances by type
     */
    List<Avance> findByTypeAvanceOrderByDateReception(TypeAvance typeAvance);
    
    /**
     * Find advances by status
     */
    List<Avance> findByStatutOrderByDateReception(StatutAvance statut);
    
    /**
     * Find advances by currency
     */
    List<Avance> findByDeviseOrderByDateReception(String devise);
    
    /**
     * Find advances by issuer
     */
    List<Avance> findByEmetteurIgnoreCaseOrderByDateReception(String emetteur);
    
    /**
     * Find advances by reception date range
     */
    @Query("SELECT a FROM Avance a WHERE a.dateReception BETWEEN :startDate AND :endDate ORDER BY a.dateReception")
    List<Avance> findByReceptionDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Find advances by amount range
     */
    @Query("SELECT a FROM Avance a WHERE a.montant BETWEEN :minAmount AND :maxAmount ORDER BY a.montant")
    List<Avance> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                 @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Get total advances amount by project
     */
    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM Avance a WHERE a.projet.id = :projetId")
    BigDecimal getTotalAdvancesByProject(@Param("projetId") Long projetId);
    
    /**
     * Get total advances amount by project and type
     */
    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM Avance a WHERE a.projet.id = :projetId AND a.typeAvance = :typeAvance")
    BigDecimal getTotalAdvancesByProjectAndType(@Param("projetId") Long projetId, 
                                              @Param("typeAvance") TypeAvance typeAvance);
    
    /**
     * Get total advances amount by project and status
     */
    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM Avance a WHERE a.projet.id = :projetId AND a.statut = :statut")
    BigDecimal getTotalAdvancesByProjectAndStatus(@Param("projetId") Long projetId, 
                                                @Param("statut") StatutAvance statut);
    
    /**
     * Get advances statistics by type
     */
    @Query("SELECT a.typeAvance, COUNT(a), SUM(a.montant), AVG(a.montant) FROM Avance a GROUP BY a.typeAvance")
    List<Object[]> getAdvancesStatisticsByType();
    
    /**
     * Get advances statistics by status
     */
    @Query("SELECT a.statut, COUNT(a), SUM(a.montant) FROM Avance a GROUP BY a.statut")
    List<Object[]> getAdvancesStatisticsByStatus();
    
    /**
     * Get advances statistics by currency
     */
    @Query("SELECT a.devise, COUNT(a), SUM(a.montant) FROM Avance a GROUP BY a.devise ORDER BY a.devise")
    List<Object[]> getAdvancesStatisticsByCurrency();
    
    /**
     * Find recent advances (last N days)
     */
    @Query("SELECT a FROM Avance a WHERE a.dateReception >= :fromDate ORDER BY a.dateReception DESC")
    List<Avance> findRecentAdvances(@Param("fromDate") LocalDate fromDate);
    
    /**
     * Find advances pending accounting
     */
    @Query("SELECT a FROM Avance a WHERE a.statut = 'PRIS_EN_CHARGE' ORDER BY a.dateReception")
    List<Avance> findAdvancesPendingAccounting();
    
    /**
     * Count advances by project and type
     */
    @Query("SELECT a.typeAvance, COUNT(a) FROM Avance a WHERE a.projet.id = :projetId GROUP BY a.typeAvance")
    List<Object[]> countAdvancesByProjectAndType(@Param("projetId") Long projetId);
    
    /**
     * Find largest advances (top N by amount)
     */
    @Query("SELECT a FROM Avance a ORDER BY a.montant DESC")
    List<Avance> findLargestAdvances();
    
    /**
     * Search advances by project name or advance number
     */
    @Query("SELECT a FROM Avance a WHERE LOWER(a.numeroAvance) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(a.projet.nomProjet) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.dateReception DESC")
    List<Avance> searchAdvances(@Param("searchTerm") String searchTerm);
    
    /**
     * Get monthly advances summary
     */
    @Query("SELECT YEAR(a.dateReception), MONTH(a.dateReception), COUNT(a), SUM(a.montant) " +
           "FROM Avance a GROUP BY YEAR(a.dateReception), MONTH(a.dateReception) " +
           "ORDER BY YEAR(a.dateReception) DESC, MONTH(a.dateReception) DESC")
    List<Object[]> getMonthlyAdvancesSummary();
    
    /**
     * Find advances by reception date after a specific date
     */
    List<Avance> findByDateReceptionAfterOrderByDateReceptionDesc(LocalDate date);
}