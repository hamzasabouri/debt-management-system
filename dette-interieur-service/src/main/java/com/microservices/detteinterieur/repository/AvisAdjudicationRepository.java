package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.AvisAdjudication;
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
public interface AvisAdjudicationRepository extends JpaRepository<AvisAdjudication, Long> {
    
    // Basic finders
    Optional<AvisAdjudication> findByNumeroAvis(String numeroAvis);
    
    boolean existsByNumeroAvis(String numeroAvis);
    
    // Adjudication-based queries
    List<AvisAdjudication> findByAdjudicationId(Long adjudicationId);
    
    List<AvisAdjudication> findByAdjudicationIdOrderByDateReceptionDesc(Long adjudicationId);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.adjudication.numeroAdjud = :numeroAdjud")
    List<AvisAdjudication> findByAdjudicationNumber(@Param("numeroAdjud") String numeroAdjud);
    
    // Type-based queries
    List<AvisAdjudication> findByTypeAvis(AvisAdjudication.TypeAvis typeAvis);
    
    List<AvisAdjudication> findByTypeAvisOrderByDateReceptionDesc(AvisAdjudication.TypeAvis typeAvis);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.typeAvis = 'CREDIT'")
    List<AvisAdjudication> findCreditAdvices();
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.typeAvis = 'DEBIT'")
    List<AvisAdjudication> findDebitAdvices();
    
    // Status-based queries
    List<AvisAdjudication> findByStatut(AvisAdjudication.StatutAvis statut);
    
    List<AvisAdjudication> findByStatutOrderByDateReceptionDesc(AvisAdjudication.StatutAvis statut);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.statut = 'PRIS_EN_CHARGE'")
    List<AvisAdjudication> findPendingAdvices();
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.statut = 'COMPTABILISE'")
    List<AvisAdjudication> findAccountedAdvices();
    
    // Issuer-based queries
    List<AvisAdjudication> findByEmetteur(String emetteur);
    
    List<AvisAdjudication> findByEmetteurContainingIgnoreCase(String emetteur);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE UPPER(av.emetteur) LIKE '%BAM%'")
    List<AvisAdjudication> findAdvicesFromBAM();
    
    @Query("SELECT av FROM AvisAdjudication av WHERE UPPER(av.emetteur) LIKE '%MAROCLEAR%'")
    List<AvisAdjudication> findAdvicesFromMaroclear();
    
    @Query("SELECT av FROM AvisAdjudication av WHERE UPPER(av.emetteur) LIKE '%COMPTABLE%'")
    List<AvisAdjudication> findAdvicesFromComptable();
    
    // Date-based queries
    List<AvisAdjudication> findByDateReceptionBetween(LocalDate startDate, LocalDate endDate);
    
    List<AvisAdjudication> findByDateReceptionAfter(LocalDate date);
    
    List<AvisAdjudication> findByDateReceptionBefore(LocalDate date);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.dateReception >= :startDate AND av.dateReception <= :endDate ORDER BY av.dateReception DESC")
    Page<AvisAdjudication> findAdvicesByDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  Pageable pageable);
    
    // Amount-based queries
    List<AvisAdjudication> findByMontantBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<AvisAdjudication> findByMontantGreaterThan(BigDecimal amount);
    
    List<AvisAdjudication> findByMontantLessThan(BigDecimal amount);
    
    // Combined queries
    List<AvisAdjudication> findByAdjudicationIdAndTypeAvis(Long adjudicationId, AvisAdjudication.TypeAvis typeAvis);
    
    List<AvisAdjudication> findByAdjudicationIdAndStatut(Long adjudicationId, AvisAdjudication.StatutAvis statut);
    
    List<AvisAdjudication> findByTypeAvisAndStatut(AvisAdjudication.TypeAvis typeAvis, AvisAdjudication.StatutAvis statut);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.adjudication.id = :adjudicationId AND av.typeAvis = :typeAvis AND av.statut = :statut")
    List<AvisAdjudication> findByAdjudicationTypeAndStatus(@Param("adjudicationId") Long adjudicationId,
                                                          @Param("typeAvis") AvisAdjudication.TypeAvis typeAvis,
                                                          @Param("statut") AvisAdjudication.StatutAvis statut);
    
    // Search queries
    @Query("SELECT av FROM AvisAdjudication av WHERE " +
           "LOWER(av.numeroAvis) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(av.emetteur) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<AvisAdjudication> searchAdvices(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT av.typeAvis, COUNT(av), SUM(av.montant) FROM AvisAdjudication av GROUP BY av.typeAvis")
    List<Object[]> getAdviceStatisticsByType();
    
    @Query("SELECT av.statut, COUNT(av), SUM(av.montant) FROM AvisAdjudication av GROUP BY av.statut")
    List<Object[]> getAdviceStatisticsByStatus();
    
    @Query("SELECT av.emetteur, COUNT(av), SUM(av.montant) FROM AvisAdjudication av GROUP BY av.emetteur")
    List<Object[]> getAdviceStatisticsByIssuer();
    
    @Query("SELECT COUNT(av) FROM AvisAdjudication av WHERE av.typeAvis = :typeAvis AND av.statut = :statut")
    Long countByTypeAndStatus(@Param("typeAvis") AvisAdjudication.TypeAvis typeAvis,
                             @Param("statut") AvisAdjudication.StatutAvis statut);
    
    @Query("SELECT SUM(av.montant) FROM AvisAdjudication av WHERE av.typeAvis = :typeAvis AND av.statut = 'COMPTABILISE'")
    BigDecimal getTotalAccountedAmountByType(@Param("typeAvis") AvisAdjudication.TypeAvis typeAvis);
    
    // Financial queries
    @Query("SELECT " +
           "SUM(CASE WHEN av.typeAvis = 'CREDIT' AND av.statut = 'COMPTABILISE' THEN av.montant ELSE 0 END) as totalCredits, " +
           "SUM(CASE WHEN av.typeAvis = 'DEBIT' AND av.statut = 'COMPTABILISE' THEN av.montant ELSE 0 END) as totalDebits " +
           "FROM AvisAdjudication av WHERE av.adjudication.id = :adjudicationId")
    Object[] getFinancialSummaryByAuction(@Param("adjudicationId") Long adjudicationId);
    
    @Query("SELECT " +
           "SUM(CASE WHEN av.typeAvis = 'CREDIT' THEN av.montant ELSE 0 END) as totalCredits, " +
           "SUM(CASE WHEN av.typeAvis = 'DEBIT' THEN av.montant ELSE 0 END) as totalDebits " +
           "FROM AvisAdjudication av WHERE av.statut = 'COMPTABILISE'")
    Object[] getOverallFinancialSummary();
    
    // Recent and trending
    @Query("SELECT av FROM AvisAdjudication av ORDER BY av.createdAt DESC LIMIT :limit")
    List<AvisAdjudication> findRecentAdvices(@Param("limit") int limit);
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.dateReception >= :date ORDER BY av.montant DESC")
    List<AvisAdjudication> findLargestAdvicesSince(@Param("date") LocalDate date);
    
    // Monthly and yearly reports
    @Query("SELECT EXTRACT(YEAR FROM av.dateReception) as year, " +
           "EXTRACT(MONTH FROM av.dateReception) as month, " +
           "av.typeAvis, COUNT(av), SUM(av.montant) " +
           "FROM AvisAdjudication av " +
           "WHERE av.dateReception >= :startDate AND av.dateReception <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM av.dateReception), EXTRACT(MONTH FROM av.dateReception), av.typeAvis " +
           "ORDER BY year, month, av.typeAvis")
    List<Object[]> getMonthlyAdviceReport(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    // Business-specific queries
    @Query("SELECT av FROM AvisAdjudication av WHERE av.adjudication.statut = 'EN_COURS' AND av.statut = 'PRIS_EN_CHARGE'")
    List<AvisAdjudication> findPendingAdvicesForActiveAuctions();
    
    @Query("SELECT av FROM AvisAdjudication av WHERE av.dateReception BETWEEN :startDate AND :endDate AND av.montant >= :minAmount")
    List<AvisAdjudication> findLargeAdvicesInPeriod(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("minAmount") BigDecimal minAmount);
}