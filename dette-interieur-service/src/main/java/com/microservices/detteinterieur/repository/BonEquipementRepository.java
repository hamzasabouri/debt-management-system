package com.microservices.detteinterieur.repository;

import com.microservices.detteinterieur.entity.BonEquipement;
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
public interface BonEquipementRepository extends JpaRepository<BonEquipement, Long> {
    
    // Basic finders
    Optional<BonEquipement> findByNumeroBon(String numeroBon);
    
    boolean existsByNumeroBon(String numeroBon);
    
    // Status-based queries
    List<BonEquipement> findByStatut(BonEquipement.StatutBon statut);
    
    List<BonEquipement> findByStatutOrderByDateSouscriptionDesc(BonEquipement.StatutBon statut);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = 'SOUSCRIT'")
    List<BonEquipement> findSubscribedBonds();
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = 'REMBOURSE'")
    List<BonEquipement> findReimbursedBonds();
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = 'REJETE'")
    List<BonEquipement> findRejectedBonds();
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = 'EN_COURS'")
    List<BonEquipement> findActiveBonds();
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = 'EXPIRE'")
    List<BonEquipement> findExpiredBonds();
    
    // Date-based queries
    List<BonEquipement> findByDateSouscriptionBetween(LocalDate startDate, LocalDate endDate);
    
    List<BonEquipement> findByDateSouscriptionAfter(LocalDate date);
    
    List<BonEquipement> findByDateSouscriptionBefore(LocalDate date);
    
    List<BonEquipement> findByDateEcheanceBetween(LocalDate startDate, LocalDate endDate);
    
    List<BonEquipement> findByDateEcheanceAfter(LocalDate date);
    
    List<BonEquipement> findByDateEcheanceBefore(LocalDate date);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.dateSouscription >= :startDate AND b.dateSouscription <= :endDate ORDER BY b.dateSouscription DESC")
    Page<BonEquipement> findBondsBySubscriptionDateRange(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         Pageable pageable);
    
    // Maturity queries
    @Query("SELECT b FROM BonEquipement b WHERE b.dateEcheance = :date")
    List<BonEquipement> findBondsMaturingOn(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.dateEcheance = CURRENT_DATE")
    List<BonEquipement> findBondsMaturingToday();
    
    @Query("SELECT b FROM BonEquipement b WHERE b.dateEcheance BETWEEN CURRENT_DATE AND :futureDate")
    List<BonEquipement> findBondsMaturingSoon(@Param("futureDate") LocalDate futureDate);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.dateEcheance < CURRENT_DATE AND b.statut NOT IN ('REMBOURSE', 'REJETE', 'EXPIRE')")
    List<BonEquipement> findOverdueBonds();
    
    // Amount-based queries
    List<BonEquipement> findByMontantBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<BonEquipement> findByMontantGreaterThan(BigDecimal amount);
    
    List<BonEquipement> findByMontantLessThan(BigDecimal amount);
    
    // Subscriber-based queries
    List<BonEquipement> findBySouscripteur(String souscripteur);
    
    List<BonEquipement> findBySouscripteurContainingIgnoreCase(String souscripteur);
    
    @Query("SELECT b FROM BonEquipement b WHERE LOWER(b.souscripteur) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BonEquipement> searchBySubscriber(@Param("searchTerm") String searchTerm);
    
    // Interest rate queries
    List<BonEquipement> findByTauxInteretBetween(BigDecimal minRate, BigDecimal maxRate);
    
    List<BonEquipement> findByTauxInteretGreaterThan(BigDecimal rate);
    
    List<BonEquipement> findByTauxInteretIsNotNull();
    
    List<BonEquipement> findByTauxInteretIsNull();
    
    // Combined queries
    List<BonEquipement> findByStatutAndDateSouscriptionBetween(BonEquipement.StatutBon statut, LocalDate startDate, LocalDate endDate);
    
    List<BonEquipement> findByStatutAndMontantGreaterThan(BonEquipement.StatutBon statut, BigDecimal amount);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.statut = :statut AND b.souscripteur = :souscripteur")
    List<BonEquipement> findByStatusAndSubscriber(@Param("statut") BonEquipement.StatutBon statut,
                                                 @Param("souscripteur") String souscripteur);
    
    // Search queries
    @Query("SELECT b FROM BonEquipement b WHERE " +
           "LOWER(b.numeroBon) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.souscripteur) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BonEquipement> searchBonds(@Param("searchTerm") String searchTerm);
    
    // Statistics queries
    @Query("SELECT b.statut, COUNT(b), SUM(b.montant) FROM BonEquipement b GROUP BY b.statut")
    List<Object[]> getBondStatisticsByStatus();
    
    @Query("SELECT COUNT(b) FROM BonEquipement b WHERE b.statut = :statut")
    Long countByStatus(@Param("statut") BonEquipement.StatutBon statut);
    
    // Add this method for the migration service
    Long countByStatut(BonEquipement.StatutBon statut);

    @Query("SELECT SUM(b.montant) FROM BonEquipement b WHERE b.statut = :statut")
    BigDecimal getTotalAmountByStatus(@Param("statut") BonEquipement.StatutBon statut);
    
    @Query("SELECT SUM(b.montant) FROM BonEquipement b WHERE b.statut = 'SOUSCRIT'")
    BigDecimal getTotalSubscribedAmount();
    
    @Query("SELECT SUM(b.montant) FROM BonEquipement b WHERE b.statut = 'REMBOURSE'")
    BigDecimal getTotalReimbursedAmount();
    
    // Subscriber statistics
    @Query("SELECT b.souscripteur, COUNT(b), SUM(b.montant) FROM BonEquipement b GROUP BY b.souscripteur ORDER BY SUM(b.montant) DESC")
    List<Object[]> getSubscriberStatistics();
    
    @Query("SELECT COUNT(DISTINCT b.souscripteur) FROM BonEquipement b")
    Long countUniqueSubscribers();
    
    // Advanced queries with avis
    @Query("SELECT b FROM BonEquipement b WHERE SIZE(b.avisBons) > 0")
    List<BonEquipement> findBondsWithAdvices();
    
    @Query("SELECT b FROM BonEquipement b WHERE SIZE(b.avisBons) = 0")
    List<BonEquipement> findBondsWithoutAdvices();
    
    @Query("SELECT b FROM BonEquipement b JOIN b.avisBons av WHERE av.statut = 'PRIS_EN_CHARGE'")
    List<BonEquipement> findBondsWithPendingAdvices();
    
    @Query("SELECT b FROM BonEquipement b JOIN b.avisBons av WHERE av.statut = 'COMPTABILISE'")
    List<BonEquipement> findBondsWithAccountedAdvices();
    
    // Financial summary queries
    @Query("SELECT b, " +
           "(SELECT SUM(av.montant) FROM AvisBonEquipement av WHERE av.bonEquipement = b AND av.typeAvis = 'CREDIT' AND av.statut = 'COMPTABILISE') as totalCredits, " +
           "(SELECT SUM(av.montant) FROM AvisBonEquipement av WHERE av.bonEquipement = b AND av.typeAvis = 'DEBIT' AND av.statut = 'COMPTABILISE') as totalDebits " +
           "FROM BonEquipement b")
    List<Object[]> getBondsWithFinancialSummary();
    
    // Recent and trending
    @Query("SELECT b FROM BonEquipement b ORDER BY b.createdAt DESC LIMIT :limit")
    List<BonEquipement> findRecentBonds(@Param("limit") int limit);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.dateSouscription >= :date ORDER BY b.montant DESC")
    List<BonEquipement> findLargestBondsSince(@Param("date") LocalDate date);
    
    // Monthly and yearly reports
    @Query("SELECT EXTRACT(YEAR FROM b.dateSouscription) as year, " +
           "EXTRACT(MONTH FROM b.dateSouscription) as month, " +
           "COUNT(b), SUM(b.montant) " +
           "FROM BonEquipement b " +
           "WHERE b.dateSouscription >= :startDate AND b.dateSouscription <= :endDate " +
           "GROUP BY EXTRACT(YEAR FROM b.dateSouscription), EXTRACT(MONTH FROM b.dateSouscription) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyBondReport(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT EXTRACT(YEAR FROM b.dateSouscription) as year, " +
           "COUNT(b), SUM(b.montant) " +
           "FROM BonEquipement b " +
           "GROUP BY EXTRACT(YEAR FROM b.dateSouscription) " +
           "ORDER BY year")
    List<Object[]> getYearlyBondReport();
    
    // Maturity analysis
    @Query("SELECT EXTRACT(YEAR FROM b.dateEcheance) as year, " +
           "EXTRACT(MONTH FROM b.dateEcheance) as month, " +
           "COUNT(b), SUM(b.montant) " +
           "FROM BonEquipement b " +
           "WHERE b.dateEcheance IS NOT NULL " +
           "GROUP BY EXTRACT(YEAR FROM b.dateEcheance), EXTRACT(MONTH FROM b.dateEcheance) " +
           "ORDER BY year, month")
    List<Object[]> getMaturitySchedule();
    
    // Business-specific queries
    @Query("SELECT b FROM BonEquipement b WHERE b.dateSouscription BETWEEN :startDate AND :endDate AND b.statut = :statut")
    List<BonEquipement> findBondsByDateRangeAndStatus(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("statut") BonEquipement.StatutBon statut);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.montant >= :minAmount AND b.statut IN :statuts")
    List<BonEquipement> findLargeBondsByStatus(@Param("minAmount") BigDecimal minAmount,
                                              @Param("statuts") List<BonEquipement.StatutBon> statuts);
    
    @Query("SELECT b FROM BonEquipement b WHERE b.tauxInteret >= :minRate AND b.statut = 'SOUSCRIT'")
    List<BonEquipement> findHighYieldActiveBonds(@Param("minRate") BigDecimal minRate);
}