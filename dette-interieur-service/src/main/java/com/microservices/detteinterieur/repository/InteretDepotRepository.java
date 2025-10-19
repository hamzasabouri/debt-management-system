package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.InteretDepot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteretDepotRepository extends JpaRepository<InteretDepot, Long> {
    
    // Basic finders
    Optional<InteretDepot> findByNumeroCompteAndDateCalcul(String numeroCompte, LocalDate dateCalcul);
    
    List<InteretDepot> findByNumeroCompte(String numeroCompte);
    
    boolean existsByNumeroCompteAndDateCalcul(String numeroCompte, LocalDate dateCalcul);
    
    // Fund type-based queries
    List<InteretDepot> findByTypeFonds(InteretDepot.TypeFonds typeFonds);
    
    List<InteretDepot> findByTypeFondsOrderByDateCalculDesc(InteretDepot.TypeFonds typeFonds);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.typeFonds = 'COLLECTIVITE_LOCALE'")
    List<InteretDepot> findLocalCollectivitiesInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.typeFonds = 'DEPOT_TRESOR'")
    List<InteretDepot> findTreasuryDepositsInterests();
    
    // Status-based queries
    List<InteretDepot> findByStatut(InteretDepot.StatutInteret statut);
    
    List<InteretDepot> findByStatutOrderByDateCalculDesc(InteretDepot.StatutInteret statut);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut = 'CALCULE'")
    List<InteretDepot> findCalculatedInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut = 'PRIS_EN_CHARGE'")
    List<InteretDepot> findPendingInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut = 'COMPTABILISE'")
    List<InteretDepot> findAccountedInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut = 'TRANSMIS'")
    List<InteretDepot> findTransmittedInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut = 'REJETE'")
    List<InteretDepot> findRejectedInterests();
    
    // Date-based queries
    List<InteretDepot> findByDateCalculBetween(LocalDate startDate, LocalDate endDate);
    
    List<InteretDepot> findByDateCalculAfter(LocalDate date);
    
    List<InteretDepot> findByDateCalculBefore(LocalDate date);
    
    List<InteretDepot> findByPeriodeDebutBetween(LocalDate startDate, LocalDate endDate);
    
    List<InteretDepot> findByPeriodeFinBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.dateCalcul >= :startDate AND i.dateCalcul <= :endDate ORDER BY i.dateCalcul DESC")
    Page<InteretDepot> findInterestsByCalculationDateRange(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          Pageable pageable);
    
    // Amount-based queries
    List<InteretDepot> findByMontantBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<InteretDepot> findByMontantGreaterThan(BigDecimal amount);
    
    List<InteretDepot> findByMontantLessThan(BigDecimal amount);
    
    List<InteretDepot> findByMontantPrincipalBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<InteretDepot> findByMontantPrincipalGreaterThan(BigDecimal amount);
    
    // Interest rate queries
    List<InteretDepot> findByTauxInteretBetween(BigDecimal minRate, BigDecimal maxRate);
    
    List<InteretDepot> findByTauxInteretGreaterThan(BigDecimal rate);
    
    List<InteretDepot> findByTauxInteretIsNotNull();
    
    List<InteretDepot> findByTauxInteretIsNull();
    
    // Currency queries
    List<InteretDepot> findByDevise(String devise);
    
    List<InteretDepot> findByDeviseOrderByMontantDesc(String devise);
    
    // Account holder queries
    List<InteretDepot> findByNomTitulaire(String nomTitulaire);
    
    List<InteretDepot> findByNomTitulaireContainingIgnoreCase(String nomTitulaire);
    
    List<InteretDepot> findByOrganisme(String organisme);
    
    List<InteretDepot> findByOrganismeContainingIgnoreCase(String organisme);
    
    // Credit advice queries
    List<InteretDepot> findByAvisCreditId(Long avisCreditId);
    
    List<InteretDepot> findByAvisCreditIdIsNotNull();
    
    List<InteretDepot> findByAvisCreditIdIsNull();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.avisCreditId IS NOT NULL")
    List<InteretDepot> findInterestsWithCreditAdvice();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.avisCreditId IS NULL AND i.statut IN ('COMPTABILISE', 'TRANSMIS')")
    List<InteretDepot> findProcessedInterestsWithoutCreditAdvice();
    
    // Combined queries
    List<InteretDepot> findByTypeFondsAndStatut(InteretDepot.TypeFonds typeFonds, InteretDepot.StatutInteret statut);
    
    List<InteretDepot> findByTypeFondsAndDateCalculBetween(InteretDepot.TypeFonds typeFonds, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.typeFonds = :typeFonds AND i.statut = :statut ORDER BY i.montant DESC")
    List<InteretDepot> findByTypeAndStatusOrderByAmount(@Param("typeFonds") InteretDepot.TypeFonds typeFonds,
                                                       @Param("statut") InteretDepot.StatutInteret statut);
    
    // Search queries
    @Query("SELECT i FROM InteretDepot i WHERE " +
           "LOWER(i.numeroCompte) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.nomTitulaire) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.organisme) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<InteretDepot> searchInterests(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT i.typeFonds, COUNT(i), SUM(i.montant) FROM InteretDepot i GROUP BY i.typeFonds")
    List<Object[]> getInterestStatisticsByType();
    
    @Query("SELECT i.statut, COUNT(i), SUM(i.montant) FROM InteretDepot i GROUP BY i.statut")
    List<Object[]> getInterestStatisticsByStatus();
    
    @Query("SELECT i.devise, COUNT(i), SUM(i.montant) FROM InteretDepot i GROUP BY i.devise")
    List<Object[]> getInterestStatisticsByCurrency();
    
    @Query("SELECT i.typeFonds, i.statut, COUNT(i), SUM(i.montant) FROM InteretDepot i GROUP BY i.typeFonds, i.statut")
    List<Object[]> getDetailedInterestStatistics();
    
    @Query("SELECT COUNT(i) FROM InteretDepot i WHERE i.typeFonds = :typeFonds AND i.statut = :statut")
    Long countByTypeAndStatus(@Param("typeFonds") InteretDepot.TypeFonds typeFonds,
                             @Param("statut") InteretDepot.StatutInteret statut);
    
    @Query("SELECT SUM(i.montant) FROM InteretDepot i WHERE i.typeFonds = :typeFonds AND i.statut = 'COMPTABILISE'")
    BigDecimal getTotalAccountedAmountByType(@Param("typeFonds") InteretDepot.TypeFonds typeFonds);
    
    @Query("SELECT SUM(i.montant) FROM InteretDepot i WHERE i.statut = 'COMPTABILISE'")
    BigDecimal getTotalAccountedAmount();
    
    @Query("SELECT SUM(i.montant) FROM InteretDepot i WHERE i.statut = :statut")
    BigDecimal getTotalAmountByStatus(@Param("statut") InteretDepot.StatutInteret statut);
    
    @Query("SELECT SUM(i.montant) FROM InteretDepot i WHERE i.devise = :devise")
    BigDecimal getTotalAmountByCurrency(@Param("devise") String devise);
    
    @Query("SELECT SUM(i.montant) FROM InteretDepot i WHERE i.statut IN ('CALCULE', 'PRIS_EN_CHARGE')")
    BigDecimal getTotalPendingAmount();
    
    // Account holder statistics
    @Query("SELECT i.nomTitulaire, COUNT(i), SUM(i.montant) FROM InteretDepot i WHERE i.nomTitulaire IS NOT NULL GROUP BY i.nomTitulaire ORDER BY SUM(i.montant) DESC")
    List<Object[]> getAccountHolderStatistics();
    
    @Query("SELECT i.organisme, COUNT(i), SUM(i.montant) FROM InteretDepot i WHERE i.organisme IS NOT NULL GROUP BY i.organisme ORDER BY SUM(i.montant) DESC")
    List<Object[]> getOrganizationStatistics();
    
    @Query("SELECT COUNT(DISTINCT i.numeroCompte) FROM InteretDepot i")
    Long countUniqueAccounts();
    
    @Query("SELECT COUNT(DISTINCT i.nomTitulaire) FROM InteretDepot i WHERE i.nomTitulaire IS NOT NULL")
    Long countUniqueAccountHolders();
    
    // Performance queries
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(DAY, i.created_at, i.date_traitement)) FROM interet_depot i WHERE i.date_traitement IS NOT NULL", nativeQuery = true)
    Double getAverageProcessingTimeInDays();
    
    @Query(value = "SELECT i.type_fonds, AVG(TIMESTAMPDIFF(DAY, i.created_at, i.date_traitement)) FROM interet_depot i WHERE i.date_traitement IS NOT NULL GROUP BY i.type_fonds", nativeQuery = true)
    List<Object[]> getAverageProcessingTimeByType();
    
    // Recent and trending
    @Query("SELECT i FROM InteretDepot i ORDER BY i.createdAt DESC LIMIT :limit")
    List<InteretDepot> findRecentInterests(@Param("limit") int limit);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.dateCalcul >= :date ORDER BY i.montant DESC")
    List<InteretDepot> findLargestInterestsSince(@Param("date") LocalDate date);
    
    // Monthly and yearly reports
    @Query("SELECT EXTRACT(YEAR FROM i.dateCalcul) as year, " +
           "EXTRACT(MONTH FROM i.dateCalcul) as month, " +
           "i.typeFonds, COUNT(i), SUM(i.montant) " +
           "FROM InteretDepot i " +
           "WHERE i.dateCalcul >= :startDate AND i.dateCalcul <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM i.dateCalcul), EXTRACT(MONTH FROM i.dateCalcul), i.typeFonds " +
           "ORDER BY year, month, i.typeFonds")
    List<Object[]> getMonthlyInterestReport(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT EXTRACT(YEAR FROM i.dateCalcul) as year, " +
           "COUNT(i), SUM(i.montant) " +
           "FROM InteretDepot i " +
           "GROUP BY EXTRACT(YEAR FROM i.dateCalcul) " +
           "ORDER BY year")
    List<Object[]> getYearlyInterestReport();
    
    // Interest rate analysis
    @Query("SELECT i.tauxInteret, COUNT(i), SUM(i.montant) FROM InteretDepot i WHERE i.tauxInteret IS NOT NULL GROUP BY i.tauxInteret ORDER BY i.tauxInteret")
    List<Object[]> getInterestRateDistribution();
    
    @Query("SELECT AVG(i.tauxInteret) FROM InteretDepot i WHERE i.tauxInteret IS NOT NULL AND i.typeFonds = :typeFonds")
    BigDecimal getAverageInterestRateByType(@Param("typeFonds") InteretDepot.TypeFonds typeFonds);
    
    // Business-specific queries
    @Query("SELECT i FROM InteretDepot i WHERE i.dateCalcul BETWEEN :startDate AND :endDate AND i.statut = :statut")
    List<InteretDepot> findInterestsByDateRangeAndStatus(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate,
                                                        @Param("statut") InteretDepot.StatutInteret statut);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.montant >= :minAmount AND i.statut IN :statuts")
    List<InteretDepot> findLargeInterestsByStatus(@Param("minAmount") BigDecimal minAmount,
                                                 @Param("statuts") List<InteretDepot.StatutInteret> statuts);
    
    @Query("SELECT i FROM InteretDepot i WHERE i.typeFonds = 'COLLECTIVITE_LOCALE' AND i.statut = 'CALCULE'")
    List<InteretDepot> findReadyToTransmitCollectivitiesInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.typeFonds = 'DEPOT_TRESOR' AND i.statut = 'CALCULE'")
    List<InteretDepot> findReadyToProcessTreasuryInterests();
    
    @Query("SELECT i FROM InteretDepot i WHERE i.statut IN ('CALCULE', 'PRIS_EN_CHARGE') AND i.createdAt < :threshold")
    List<InteretDepot> findOverdueInterests(@Param("threshold") LocalDate threshold);
}