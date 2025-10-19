package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.Adjudication;
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
public interface AdjudicationRepository extends JpaRepository<Adjudication, Long> {
    
    // Basic finders
    Optional<Adjudication> findByNumeroAdjud(String numeroAdjud);
    
    boolean existsByNumeroAdjud(String numeroAdjud);
    
    // Status-based queries
    List<Adjudication> findByStatut(Adjudication.StatutAdjudication statut);
    
    List<Adjudication> findByStatutOrderByDateAdjudDesc(Adjudication.StatutAdjudication statut);
    
    @Query("SELECT a FROM Adjudication a WHERE a.statut = 'EN_COURS'")
    List<Adjudication> findActiveAuctions();
    
    @Query("SELECT a FROM Adjudication a WHERE a.statut = 'CLOTUREE'")
    List<Adjudication> findClosedAuctions();
    
    @Query("SELECT a FROM Adjudication a WHERE a.statut = 'ANNULEE'")
    List<Adjudication> findCancelledAuctions();
    
    // Date-based queries
    List<Adjudication> findByDateAdjudBetween(LocalDate startDate, LocalDate endDate);
    
    List<Adjudication> findByDateAdjudAfter(LocalDate date);
    
    List<Adjudication> findByDateAdjudBefore(LocalDate date);
    
    @Query("SELECT a FROM Adjudication a WHERE a.dateAdjud >= :startDate AND a.dateAdjud <= :endDate ORDER BY a.dateAdjud DESC")
    Page<Adjudication> findAuctionsByDateRange(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate, 
                                               Pageable pageable);
    
    // Amount-based queries
    List<Adjudication> findByMontantTotalBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Adjudication> findByMontantTotalGreaterThan(BigDecimal amount);
    
    List<Adjudication> findByMontantTotalLessThan(BigDecimal amount);
    
    // Search queries
    @Query("SELECT a FROM Adjudication a WHERE " +
           "LOWER(a.numeroAdjud) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Adjudication> searchByNumeroAdjud(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT COUNT(a) FROM Adjudication a WHERE a.statut = :statut")
    Long countByStatut(@Param("statut") Adjudication.StatutAdjudication statut);
    
    @Query("SELECT a.statut, COUNT(a), SUM(a.montantTotal) FROM Adjudication a GROUP BY a.statut")
    List<Object[]> getAuctionStatistics();
    
    @Query("SELECT SUM(a.montantTotal) FROM Adjudication a WHERE a.statut = :statut")
    BigDecimal getTotalAmountByStatut(@Param("statut") Adjudication.StatutAdjudication statut);
    
    // Advanced queries with avis
    @Query("SELECT a FROM Adjudication a WHERE SIZE(a.avisAdjudications) > 0")
    List<Adjudication> findAuctionsWithAdvices();
    
    @Query("SELECT a FROM Adjudication a WHERE SIZE(a.avisAdjudications) = 0")
    List<Adjudication> findAuctionsWithoutAdvices();
    
    @Query("SELECT a FROM Adjudication a JOIN a.avisAdjudications av WHERE av.statut = 'PRIS_EN_CHARGE'")
    List<Adjudication> findAuctionsWithPendingAdvices();
    
    @Query("SELECT a FROM Adjudication a JOIN a.avisAdjudications av WHERE av.statut = 'COMPTABILISE'")
    List<Adjudication> findAuctionsWithAccountedAdvices();
    
    // Monthly and yearly reports
    @Query("SELECT YEAR(a.dateAdjud) as year, " +
           "MONTH(a.dateAdjud) as month, " +
           "COUNT(a), SUM(a.montantTotal) " +
           "FROM Adjudication a " +
           "WHERE a.dateAdjud >= :startDate AND a.dateAdjud <= :endDate " +
           "GROUP BY YEAR(a.dateAdjud), MONTH(a.dateAdjud) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyAuctionReport(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT YEAR(a.dateAdjud) as year, " +
           "COUNT(a), SUM(a.montantTotal) " +
           "FROM Adjudication a " +
           "GROUP BY YEAR(a.dateAdjud) " +
           "ORDER BY year")
    List<Object[]> getYearlyAuctionReport();
    
    // Financial summary
    @Query("SELECT a, " +
           "(SELECT SUM(av.montant) FROM AvisAdjudication av WHERE av.adjudication = a AND av.typeAvis = 'CREDIT' AND av.statut = 'COMPTABILISE') as totalCredits, " +
           "(SELECT SUM(av.montant) FROM AvisAdjudication av WHERE av.adjudication = a AND av.typeAvis = 'DEBIT' AND av.statut = 'COMPTABILISE') as totalDebits " +
           "FROM Adjudication a")
    List<Object[]> getAuctionsWithFinancialSummary();
    
    // Recent auctions
    @Query("SELECT a FROM Adjudication a ORDER BY a.createdAt DESC LIMIT :limit")
    List<Adjudication> findRecentAuctions(@Param("limit") int limit);
    
    // Custom business queries
    @Query("SELECT a FROM Adjudication a WHERE a.dateAdjud BETWEEN :startDate AND :endDate AND a.statut = :statut")
    List<Adjudication> findAuctionsByDateRangeAndStatus(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("statut") Adjudication.StatutAdjudication statut);
    
    @Query("SELECT a FROM Adjudication a WHERE a.montantTotal >= :minAmount AND a.statut IN :statuts")
    List<Adjudication> findLargeAuctionsByStatus(@Param("minAmount") BigDecimal minAmount,
                                                @Param("statuts") List<Adjudication.StatutAdjudication> statuts);
}