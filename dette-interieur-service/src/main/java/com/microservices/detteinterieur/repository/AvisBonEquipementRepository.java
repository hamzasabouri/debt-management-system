package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.AvisBonEquipement;
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
public interface AvisBonEquipementRepository extends JpaRepository<AvisBonEquipement, Long> {
    
    // Basic finders
    Optional<AvisBonEquipement> findByNumeroAvis(String numeroAvis);
    
    boolean existsByNumeroAvis(String numeroAvis);
    
    // Equipment bond-based queries
    List<AvisBonEquipement> findByBonEquipementId(Long bonEquipementId);
    
    List<AvisBonEquipement> findByBonEquipementIdOrderByDateReceptionDesc(Long bonEquipementId);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.bonEquipement.numeroBon = :numeroBon")
    List<AvisBonEquipement> findByBondNumber(@Param("numeroBon") String numeroBon);
    
    // Type-based queries
    List<AvisBonEquipement> findByTypeAvis(AvisBonEquipement.TypeAvis typeAvis);
    
    List<AvisBonEquipement> findByTypeAvisOrderByDateReceptionDesc(AvisBonEquipement.TypeAvis typeAvis);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'CREDIT'")
    List<AvisBonEquipement> findCreditAdvices();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'DEBIT'")
    List<AvisBonEquipement> findDebitAdvices();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'REJET'")
    List<AvisBonEquipement> findRejectionAdvices();
    
    // Status-based queries
    List<AvisBonEquipement> findByStatut(AvisBonEquipement.StatutAvis statut);
    
    List<AvisBonEquipement> findByStatutOrderByDateReceptionDesc(AvisBonEquipement.StatutAvis statut);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.statut = 'PRIS_EN_CHARGE'")
    List<AvisBonEquipement> findPendingAdvices();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.statut = 'COMPTABILISE'")
    List<AvisBonEquipement> findAccountedAdvices();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.statut = 'REJETE'")
    List<AvisBonEquipement> findRejectedAdvices();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.statut = 'EN_ATTENTE'")
    List<AvisBonEquipement> findWaitingAdvices();
    
    // Issuer-based queries
    List<AvisBonEquipement> findByEmetteur(String emetteur);
    
    List<AvisBonEquipement> findByEmetteurContainingIgnoreCase(String emetteur);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE UPPER(av.emetteur) LIKE '%BAM%'")
    List<AvisBonEquipement> findAdvicesFromBAM();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE UPPER(av.emetteur) LIKE '%COMPTABLE%'")
    List<AvisBonEquipement> findAdvicesFromComptable();
    
    // Date-based queries
    List<AvisBonEquipement> findByDateReceptionBetween(LocalDate startDate, LocalDate endDate);
    
    List<AvisBonEquipement> findByDateReceptionAfter(LocalDate date);
    
    List<AvisBonEquipement> findByDateReceptionBefore(LocalDate date);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.dateReception >= :startDate AND av.dateReception <= :endDate ORDER BY av.dateReception DESC")
    Page<AvisBonEquipement> findAdvicesByDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  Pageable pageable);
    
    // Amount-based queries
    List<AvisBonEquipement> findByMontantBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<AvisBonEquipement> findByMontantGreaterThan(BigDecimal amount);
    
    List<AvisBonEquipement> findByMontantLessThan(BigDecimal amount);
    
    // Combined queries
    List<AvisBonEquipement> findByBonEquipementIdAndTypeAvis(Long bonEquipementId, AvisBonEquipement.TypeAvis typeAvis);
    
    List<AvisBonEquipement> findByBonEquipementIdAndStatut(Long bonEquipementId, AvisBonEquipement.StatutAvis statut);
    
    List<AvisBonEquipement> findByTypeAvisAndStatut(AvisBonEquipement.TypeAvis typeAvis, AvisBonEquipement.StatutAvis statut);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.bonEquipement.id = :bonEquipementId AND av.typeAvis = :typeAvis AND av.statut = :statut")
    List<AvisBonEquipement> findByBondTypeAndStatus(@Param("bonEquipementId") Long bonEquipementId,
                                                   @Param("typeAvis") AvisBonEquipement.TypeAvis typeAvis,
                                                   @Param("statut") AvisBonEquipement.StatutAvis statut);
    
    // Rejection-specific queries
    List<AvisBonEquipement> findByTypeAvisAndMotifRejetIsNotNull(AvisBonEquipement.TypeAvis typeAvis);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'REJET' AND av.motifRejet IS NOT NULL")
    List<AvisBonEquipement> findRejectionsWithReason();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'REJET' AND av.motifRejet IS NULL")
    List<AvisBonEquipement> findRejectionsWithoutReason();
    
    // Search queries
    @Query("SELECT av FROM AvisBonEquipement av WHERE " +
           "LOWER(av.numeroAvis) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(av.emetteur) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(av.motifRejet) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<AvisBonEquipement> searchAdvices(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT av.typeAvis, COUNT(av), SUM(av.montant) FROM AvisBonEquipement av GROUP BY av.typeAvis")
    List<Object[]> getAdviceStatisticsByType();
    
    @Query("SELECT av.statut, COUNT(av), SUM(av.montant) FROM AvisBonEquipement av GROUP BY av.statut")
    List<Object[]> getAdviceStatisticsByStatus();
    
    @Query("SELECT av.emetteur, COUNT(av), SUM(av.montant) FROM AvisBonEquipement av GROUP BY av.emetteur")
    List<Object[]> getAdviceStatisticsByIssuer();
    
    @Query("SELECT av.typeAvis, av.statut, COUNT(av), SUM(av.montant) FROM AvisBonEquipement av GROUP BY av.typeAvis, av.statut")
    List<Object[]> getDetailedAdviceStatistics();
    
    @Query("SELECT COUNT(av) FROM AvisBonEquipement av WHERE av.typeAvis = :typeAvis AND av.statut = :statut")
    Long countByTypeAndStatus(@Param("typeAvis") AvisBonEquipement.TypeAvis typeAvis,
                             @Param("statut") AvisBonEquipement.StatutAvis statut);
    
    @Query("SELECT SUM(av.montant) FROM AvisBonEquipement av WHERE av.typeAvis = :typeAvis AND av.statut = 'COMPTABILISE'")
    BigDecimal getTotalAccountedAmountByType(@Param("typeAvis") AvisBonEquipement.TypeAvis typeAvis);
    
    // Financial queries
    @Query("SELECT " +
           "SUM(CASE WHEN av.typeAvis = 'CREDIT' AND av.statut = 'COMPTABILISE' THEN av.montant ELSE 0 END) as totalCredits, " +
           "SUM(CASE WHEN av.typeAvis = 'DEBIT' AND av.statut = 'COMPTABILISE' THEN av.montant ELSE 0 END) as totalDebits, " +
           "SUM(CASE WHEN av.typeAvis = 'REJET' THEN av.montant ELSE 0 END) as totalRejections " +
           "FROM AvisBonEquipement av WHERE av.bonEquipement.id = :bonEquipementId")
    Object[] getFinancialSummaryByBond(@Param("bonEquipementId") Long bonEquipementId);
    
    @Query("SELECT " +
           "SUM(CASE WHEN av.typeAvis = 'CREDIT' THEN av.montant ELSE 0 END) as totalCredits, " +
           "SUM(CASE WHEN av.typeAvis = 'DEBIT' THEN av.montant ELSE 0 END) as totalDebits, " +
           "SUM(CASE WHEN av.typeAvis = 'REJET' THEN av.montant ELSE 0 END) as totalRejections " +
           "FROM AvisBonEquipement av WHERE av.statut = 'COMPTABILISE'")
    Object[] getOverallFinancialSummary();
    
    // Recent and trending
    @Query("SELECT av FROM AvisBonEquipement av ORDER BY av.createdAt DESC LIMIT :limit")
    List<AvisBonEquipement> findRecentAdvices(@Param("limit") int limit);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.dateReception >= :date ORDER BY av.montant DESC")
    List<AvisBonEquipement> findLargestAdvicesSince(@Param("date") LocalDate date);
    
    // Monthly and yearly reports
    @Query("SELECT EXTRACT(YEAR FROM av.dateReception) as year, " +
           "EXTRACT(MONTH FROM av.dateReception) as month, " +
           "av.typeAvis, COUNT(av), SUM(av.montant) " +
           "FROM AvisBonEquipement av " +
           "WHERE av.dateReception >= :startDate AND av.dateReception <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM av.dateReception), EXTRACT(MONTH FROM av.dateReception), av.typeAvis " +
           "ORDER BY year, month, av.typeAvis")
    List<Object[]> getMonthlyAdviceReport(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    // Rejection analysis
    @Query("SELECT av.motifRejet, COUNT(av), SUM(av.montant) FROM AvisBonEquipement av WHERE av.typeAvis = 'REJET' AND av.motifRejet IS NOT NULL GROUP BY av.motifRejet ORDER BY COUNT(av) DESC")
    List<Object[]> getRejectionReasonStatistics();
    
    @Query("SELECT COUNT(av) FROM AvisBonEquipement av WHERE av.typeAvis = 'REJET' AND av.dateReception BETWEEN :startDate AND :endDate")
    Long countRejectionsInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Business-specific queries
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.bonEquipement.statut = 'SOUSCRIT' AND av.statut = 'PRIS_EN_CHARGE'")
    List<AvisBonEquipement> findPendingAdvicesForActiveBonds();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.dateReception BETWEEN :startDate AND :endDate AND av.montant >= :minAmount")
    List<AvisBonEquipement> findLargeAdvicesInPeriod(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.typeAvis = 'CREDIT' AND av.bonEquipement.statut = 'REJETE'")
    List<AvisBonEquipement> findCreditAdvicesForRejectedBonds();
    
    @Query("SELECT av FROM AvisBonEquipement av WHERE av.statut = 'EN_ATTENTE' AND av.dateReception < :threshold")
    List<AvisBonEquipement> findOverdueAdvices(@Param("threshold") LocalDate threshold);
}