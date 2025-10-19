package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.Echeancier;
import com.microservices.dettetresor.entity.Echeancier.StatutEcheance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EcheancierRepository extends JpaRepository<Echeancier, Long> {
    
    /**
     * Find payment schedule by loan ID
     */
    List<Echeancier> findByPretIdOrderByDateEcheance(Long pretId);
    
    /**
     * Find payment schedule by loan ID and status
     */
    List<Echeancier> findByPretIdAndStatutOrderByDateEcheance(Long pretId, StatutEcheance statut);
    
    /**
     * Find payments by status
     */
    List<Echeancier> findByStatutOrderByDateEcheance(StatutEcheance statut);
    
    /**
     * Find payment schedule by payment number
     */
    List<Echeancier> findByNumeroEcheance(Integer numeroEcheance);
    
    /**
     * Find overdue payments (due date passed and not paid)
     */
    @Query("SELECT e FROM Echeancier e WHERE e.dateEcheance < :currentDate AND e.statut IN :unpaidStatuses")
    List<Echeancier> findOverduePayments(@Param("currentDate") LocalDate currentDate,
                                        @Param("unpaidStatuses") List<StatutEcheance> unpaidStatuses);
    
    /**
     * Find payments due within a date range
     */
    @Query("SELECT e FROM Echeancier e WHERE e.dateEcheance BETWEEN :startDate AND :endDate ORDER BY e.dateEcheance")
    List<Echeancier> findPaymentsDueBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * Find upcoming payments (next N days)
     */
    @Query("SELECT e FROM Echeancier e WHERE e.dateEcheance BETWEEN :today AND :futureDate AND e.statut = :statut ORDER BY e.dateEcheance")
    List<Echeancier> findUpcomingPayments(@Param("today") LocalDate today,
                                         @Param("futureDate") LocalDate futureDate,
                                         @Param("statut") StatutEcheance statut);
    
    /**
     * Find payments without payment order
     */
    @Query("SELECT e FROM Echeancier e WHERE e.ordrePaiement IS NULL AND e.statut = :statut")
    List<Echeancier> findPaymentsWithoutPaymentOrder(@Param("statut") StatutEcheance statut);
    
    /**
     * Get payment statistics by status
     */
    @Query("SELECT e.statut, COUNT(e), SUM(e.montantTotal) FROM Echeancier e GROUP BY e.statut")
    List<Object[]> getPaymentStatisticsByStatus();
    
    /**
     * Find payments by loan and payment number range
     */
    @Query("SELECT e FROM Echeancier e WHERE e.pret.id = :pretId AND e.numeroEcheance BETWEEN :startNum AND :endNum ORDER BY e.numeroEcheance")
    List<Echeancier> findByLoanAndPaymentNumberRange(@Param("pretId") Long pretId,
                                                     @Param("startNum") Integer startNum,
                                                     @Param("endNum") Integer endNum);
    
    /**
     * Count payments by status for a specific loan
     */
    @Query("SELECT e.statut, COUNT(e) FROM Echeancier e WHERE e.pret.id = :pretId GROUP BY e.statut")
    List<Object[]> countPaymentsByStatusForLoan(@Param("pretId") Long pretId);
    
    /**
     * Find payment schedule by loan entity ordered by due date
     */
    List<Echeancier> findByPretOrderByDateEcheance(com.microservices.dettetresor.entity.Pret pret);
    
    /**
     * Find payments by due date before and status in list
     */
    List<Echeancier> findByDateEcheanceBeforeAndStatutIn(LocalDate date, List<StatutEcheance> statuts);
    
    /**
     * Find payments by loan and status in list ordered by due date
     */
    List<Echeancier> findByPretAndStatutInOrderByDateEcheance(com.microservices.dettetresor.entity.Pret pret, List<StatutEcheance> statuts);
}