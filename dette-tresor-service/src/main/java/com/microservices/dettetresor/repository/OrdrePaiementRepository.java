package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.Pret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdrePaiementRepository extends JpaRepository<OrdrePaiement, Long> {
    
    /**
     * Find all payment orders for a specific loan, ordered by emission date
     */
    List<OrdrePaiement> findByPretIdOrderByDateEmission(Long pretId);
    
    /**
     * Find payment order by order number
     */
    Optional<OrdrePaiement> findByNumeroOrdre(String numeroOrdre);
    
    /**
     * Check if payment order number exists (excluding a specific ID)
     */
    boolean existsByNumeroOrdreAndIdNot(String numeroOrdre, Long id);
    
    /**
     * Check if payment order number exists
     */
    boolean existsByNumeroOrdre(String numeroOrdre);
    
    /**
     * Find payment orders by loan ID
     */
    List<OrdrePaiement> findByPretId(Long pretId);
    
    /**
     * Find payment orders by settlement letter ID
     */
    List<OrdrePaiement> findByLettreReglementId(Long lettreReglementId);
    
    /**
     * Find payment orders by currency, ordered by emission date
     */
    List<OrdrePaiement> findByDeviseOrderByDateEmission(String devise);
    
    /**
     * Find payment orders by status, ordered by emission date
     */
    List<OrdrePaiement> findByStatutOrderByDateEmission(OrdrePaiement.StatutOrdrePaiement statut);
    
    /**
     * Find payment orders by loan and status
     */
    List<OrdrePaiement> findByPretAndStatut(Pret pret, OrdrePaiement.StatutOrdrePaiement statut);
    
    /**
     * Find payment orders by loan, emission date, and status
     */
    List<OrdrePaiement> findByPretAndDateEmissionAndStatut(Pret pret, LocalDate dateEmission, OrdrePaiement.StatutOrdrePaiement statut);
    
    /**
     * Find payment orders by status in a list of statuses
     */
    List<OrdrePaiement> findByStatutIn(List<OrdrePaiement.StatutOrdrePaiement> statuts);
    
    /**
     * Find payment orders by emission date range
     */
    @Query("SELECT o FROM OrdrePaiement o WHERE o.dateEmission BETWEEN :startDate AND :endDate ORDER BY o.dateEmission")
    List<OrdrePaiement> findByEmissionDateBetween(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * Find payment orders with PDF file path
     */
    @Query("SELECT o FROM OrdrePaiement o WHERE o.lettreReglement.cheminFichierPdf IS NOT NULL ORDER BY o.dateEmission")
    List<OrdrePaiement> findPaymentOrdersWithPdf();
    
    /**
     * Find payment orders without PDF file path
     */
    @Query("SELECT o FROM OrdrePaiement o WHERE o.lettreReglement.cheminFichierPdf IS NULL ORDER BY o.dateEmission")
    List<OrdrePaiement> findPaymentOrdersWithoutPdf();
    
    /**
     * Get total payment amount by currency
     */
    @Query("SELECT o.devise, SUM(o.montant) FROM OrdrePaiement o GROUP BY o.devise")
    List<Object[]> getTotalPaymentAmountByCurrency();
    
    /**
     * Find duplicate payment orders by ID
     */
    @Query(value = "SELECT id, COUNT(*) as count FROM ordre_paiement GROUP BY id HAVING COUNT(*) > 1", nativeQuery = true)
    List<Object[]> findDuplicateIds();
}