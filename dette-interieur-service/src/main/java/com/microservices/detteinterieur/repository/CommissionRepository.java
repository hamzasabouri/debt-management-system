package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.Commission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    
    // Basic finders
    Optional<Commission> findByNumeroReference(String numeroReference);
    
    boolean existsByNumeroReference(String numeroReference);
    
    // Type-based queries
    List<Commission> findByTypeCommission(Commission.TypeCommission typeCommission);
    
    List<Commission> findByTypeCommissionOrderByCreatedAtDesc(Commission.TypeCommission typeCommission);
    
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'MAROCLEAR'")
    List<Commission> findMaroclearCommissions();
    
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'BAM'")
    List<Commission> findBAMCommissions();
    
    // Status-based queries
    List<Commission> findByStatut(Commission.StatutCommission statut);
    
    List<Commission> findByStatutOrderByCreatedAtDesc(Commission.StatutCommission statut);
    
    @Query("SELECT c FROM Commission c WHERE c.statut = 'EN_ATTENTE'")
    List<Commission> findPendingCommissions();
    
    @Query("SELECT c FROM Commission c WHERE c.statut = 'EN_COURS_TRAITEMENT'")
    List<Commission> findProcessingCommissions();
    
    @Query("SELECT c FROM Commission c WHERE c.statut = 'PAYE'")
    List<Commission> findPaidCommissions();
    
    @Query("SELECT c FROM Commission c WHERE c.statut = 'REJETE'")
    List<Commission> findRejectedCommissions();
    
    // Reference-based queries
    List<Commission> findByOrdrePaiementId(Long ordrePaiementId);
    
    List<Commission> findByLettreReglementId(Long lettreReglementId);
    
    List<Commission> findByAvisDebitId(Long avisDebitId);
    
    @Query("SELECT c FROM Commission c WHERE c.ordrePaiementId IS NOT NULL")
    List<Commission> findCommissionsWithPaymentOrder();
    
    @Query("SELECT c FROM Commission c WHERE c.lettreReglementId IS NOT NULL")
    List<Commission> findCommissionsWithSettlementLetter();
    
    @Query("SELECT c FROM Commission c WHERE c.avisDebitId IS NOT NULL")
    List<Commission> findCommissionsWithDebitAdvice();
    
    // Workflow completeness queries
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'MAROCLEAR' AND " +
           "c.ordrePaiementId IS NOT NULL AND c.lettreReglementId IS NOT NULL AND c.avisDebitId IS NOT NULL")
    List<Commission> findCompleteMaroclearWorkflow();
    
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'BAM' AND c.avisDebitId IS NOT NULL")
    List<Commission> findCompleteBAMWorkflow();
    
    @Query("SELECT c FROM Commission c WHERE " +
           "(c.typeCommission = 'MAROCLEAR' AND (c.ordrePaiementId IS NULL OR c.lettreReglementId IS NULL OR c.avisDebitId IS NULL)) OR " +
           "(c.typeCommission = 'BAM' AND c.avisDebitId IS NULL)")
    List<Commission> findIncompleteWorkflow();
    
    // Amount-based queries
    List<Commission> findByMontantBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Commission> findByMontantGreaterThan(BigDecimal amount);
    
    List<Commission> findByMontantLessThan(BigDecimal amount);
    
    // Date-based queries
    List<Commission> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Commission> findByDatePaiementBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Commission> findByDatePaiementIsNotNull();
    
    List<Commission> findByDatePaiementIsNull();
    
    @Query("SELECT c FROM Commission c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate ORDER BY c.createdAt DESC")
    Page<Commission> findCommissionsByCreationDateRange(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       Pageable pageable);
    
    // Combined queries
    List<Commission> findByTypeCommissionAndStatut(Commission.TypeCommission typeCommission, Commission.StatutCommission statut);
    
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = :typeCommission AND c.statut = :statut ORDER BY c.montant DESC")
    List<Commission> findByTypeAndStatusOrderByAmount(@Param("typeCommission") Commission.TypeCommission typeCommission,
                                                     @Param("statut") Commission.StatutCommission statut);
    
    // Search queries
    @Query("SELECT c FROM Commission c WHERE " +
           "LOWER(c.numeroReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Commission> searchCommissions(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT c.typeCommission, COUNT(c), SUM(c.montant) FROM Commission c GROUP BY c.typeCommission")
    List<Object[]> getCommissionStatisticsByType();
    
    @Query("SELECT c.statut, COUNT(c), SUM(c.montant) FROM Commission c GROUP BY c.statut")
    List<Object[]> getCommissionStatisticsByStatus();
    
    @Query("SELECT c.typeCommission, c.statut, COUNT(c), SUM(c.montant) FROM Commission c GROUP BY c.typeCommission, c.statut")
    List<Object[]> getDetailedCommissionStatistics();
    
    @Query("SELECT COUNT(c) FROM Commission c WHERE c.typeCommission = :typeCommission AND c.statut = :statut")
    Long countByTypeAndStatus(@Param("typeCommission") Commission.TypeCommission typeCommission,
                             @Param("statut") Commission.StatutCommission statut);
    
    @Query("SELECT SUM(c.montant) FROM Commission c WHERE c.typeCommission = :typeCommission AND c.statut = 'PAYE'")
    BigDecimal getTotalPaidAmountByType(@Param("typeCommission") Commission.TypeCommission typeCommission);
    
    @Query("SELECT SUM(c.montant) FROM Commission c WHERE c.statut = 'PAYE'")
    BigDecimal getTotalPaidAmount();
    
    @Query("SELECT SUM(c.montant) FROM Commission c WHERE c.statut IN ('EN_ATTENTE', 'EN_COURS_TRAITEMENT')")
    BigDecimal getTotalPendingAmount();
    
    // Performance queries
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(DAY, c.created_at, c.date_paiement)) FROM commissions c WHERE c.date_paiement IS NOT NULL", nativeQuery = true)
    Double getAverageProcessingTimeInDays();
    
    @Query(value = "SELECT c.type_commission, AVG(TIMESTAMPDIFF(DAY, c.created_at, c.date_paiement)) " +
           "FROM commissions c WHERE c.date_paiement IS NOT NULL GROUP BY c.type_commission", nativeQuery = true)
    List<Object[]> getAverageProcessingTimeByType();
    
    // Recent and trending
    @Query("SELECT c FROM Commission c ORDER BY c.createdAt DESC LIMIT :limit")
    List<Commission> findRecentCommissions(@Param("limit") int limit);
    
    @Query("SELECT c FROM Commission c WHERE c.createdAt >= :date ORDER BY c.montant DESC")
    List<Commission> findLargestCommissionsSince(@Param("date") LocalDateTime date);
    
    // Monthly and yearly reports
    @Query("SELECT EXTRACT(YEAR FROM c.createdAt) as year, " +
           "EXTRACT(MONTH FROM c.createdAt) as month, " +
           "c.typeCommission, COUNT(c), SUM(c.montant) " +
           "FROM Commission c " +
           "WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM c.createdAt), EXTRACT(MONTH FROM c.createdAt), c.typeCommission " +
           "ORDER BY year, month, c.typeCommission")
    List<Object[]> getMonthlyCommissionReport(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT EXTRACT(YEAR FROM c.createdAt) as year, " +
           "COUNT(c), SUM(c.montant) " +
           "FROM Commission c " +
           "GROUP BY EXTRACT(YEAR FROM c.createdAt) " +
           "ORDER BY year")
    List<Object[]> getYearlyCommissionReport();
    
    // Business-specific queries
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'MAROCLEAR' AND c.statut = 'EN_ATTENTE' AND c.ordrePaiementId IS NOT NULL")
    List<Commission> findMaroclearCommissionsReadyForProcessing();
    
    @Query("SELECT c FROM Commission c WHERE c.typeCommission = 'BAM' AND c.statut = 'EN_ATTENTE'")
    List<Commission> findBAMCommissionsReadyForProcessing();
    
    @Query("SELECT c FROM Commission c WHERE c.statut IN ('EN_ATTENTE', 'EN_COURS_TRAITEMENT') AND c.createdAt < :threshold")
    List<Commission> findOverdueCommissions(@Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT c FROM Commission c WHERE c.montant >= :minAmount AND c.statut = 'PAYE' AND c.datePaiement BETWEEN :startDate AND :endDate")
    List<Commission> findLargePaidCommissionsInPeriod(@Param("minAmount") BigDecimal minAmount,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
}