package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.AvisDebit;
import com.microservices.dettetresor.entity.AvisDebit.MotifAvisDebit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvisDebitRepository extends JpaRepository<AvisDebit, Long> {
    
    /**
     * Vérifie si un avis de débit avec le numéro donné existe (en excluant un ID spécifique)
     */
    boolean existsByNumeroAvisAndIdNot(String numeroAvis, Long id);
    
    /**
     * Trouve tous les avis de débit pour un prêt donné
     */
    List<AvisDebit> findByPretId(Long pretId);
    
    /**
     * Find debit advice by advice number
     */
    Optional<AvisDebit> findByNumeroAvis(String numeroAvis);
    
    /**
     * Check if debit advice number exists
     */
    boolean existsByNumeroAvis(String numeroAvis);
    
    /**
     * Find debit advice by payment order ID
     */
    Optional<AvisDebit> findByOrdrePaiementId(Long ordrePaiementId);
    
    /**
     * Find all debit advices by payment order ID
     */
    List<AvisDebit> findAllByOrdrePaiementId(Long ordrePaiementId);
    
    /**
     * Find debit advices by reason
     */
    List<AvisDebit> findByMotifOrderByDateReception(MotifAvisDebit motif);
    
    /**
     * Find debit advices by reception date range
     */
    @Query("SELECT ad FROM AvisDebit ad WHERE ad.dateReception BETWEEN :startDate AND :endDate ORDER BY ad.dateReception")
    List<AvisDebit> findByReceptionDateBetween(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    /**
     * Find debit advices by reason (string version for search)
     */
    List<AvisDebit> findByMotif(String motif);
    
    /**
     * Find debit advices by currency (without ordering for search)
     */
    List<AvisDebit> findByDevise(String devise);
    
    /**
     * Find debit advices by currency
     */
    List<AvisDebit> findByDeviseOrderByDateReception(String devise);
    
    /**
     * Find debit advices without payment order (special cases)
     */
    @Query("SELECT ad FROM AvisDebit ad WHERE ad.ordrePaiement IS NULL ORDER BY ad.dateReception")
    List<AvisDebit> findDebitAdvicesWithoutPaymentOrder();
    
    /**
     * Find debit advices with payment order
     */
    @Query("SELECT ad FROM AvisDebit ad WHERE ad.ordrePaiement IS NOT NULL ORDER BY ad.dateReception")
    List<AvisDebit> findDebitAdvicesWithPaymentOrder();
    
    /**
     * Get total debit amount by reason
     */
    @Query("SELECT ad.motif, SUM(ad.montant) FROM AvisDebit ad GROUP BY ad.motif")
    List<Object[]> getTotalDebitAmountByReason();
    
    /**
     * Get total debit amount by currency
     */
    @Query("SELECT ad.devise, SUM(ad.montant) FROM AvisDebit ad GROUP BY ad.devise")
    List<Object[]> getTotalDebitAmountByCurrency();
    
    /**
     * Count debit advices by reason
     */
    @Query("SELECT ad.motif, COUNT(ad) FROM AvisDebit ad GROUP BY ad.motif")
    List<Object[]> countDebitAdvicesByReason();
}