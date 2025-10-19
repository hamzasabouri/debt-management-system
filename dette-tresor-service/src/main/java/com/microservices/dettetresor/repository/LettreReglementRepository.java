package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.LettreReglement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LettreReglementRepository extends JpaRepository<LettreReglement, Long> {
    
    /**
     * Vérifie si une lettre de règlement avec le numéro donné existe (en excluant un ID spécifique)
     */
    boolean existsByNumeroLettreAndIdNot(String numeroLettre, Long id);
    
    /**
     * Trouve toutes les lettres de règlement pour un prêt donné
     */
    List<LettreReglement> findByPretId(Long pretId);
    
    /**
     * Find settlement letter by letter number
     */
    Optional<LettreReglement> findByNumeroLettre(String numeroLettre);
    
    /**
     * Check if settlement letter number exists
     */
    boolean existsByNumeroLettre(String numeroLettre);
    
    /**
     * Find settlement letter by payment order ID
     */
    Optional<LettreReglement> findByOrdrePaiementId(Long ordrePaiementId);
    
    /**
     * Find all settlement letters by payment order ID
     */
    List<LettreReglement> findAllByOrdrePaiementId(Long ordrePaiementId);
    
    /**
     * Find settlement letters by transmission date range
     */
    @Query("SELECT lr FROM LettreReglement lr WHERE lr.dateTransmission BETWEEN :startDate AND :endDate ORDER BY lr.dateTransmission")
    List<LettreReglement> findByTransmissionDateBetween(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Find settlement letters by currency
     */
    List<LettreReglement> findByDeviseOrderByDateTransmission(String devise);
    
    /**
     * Find settlement letters by treasury account
     */
    List<LettreReglement> findByCompteTresorOrderByDateTransmission(String compteTresor);
    
    /**
     * Find settlement letters with PDF file path
     */
    @Query("SELECT lr FROM LettreReglement lr WHERE lr.cheminFichierPdf IS NOT NULL ORDER BY lr.dateTransmission")
    List<LettreReglement> findSettlementLettersWithPdf();
    
    /**
     * Find settlement letters without PDF file path
     */
    @Query("SELECT lr FROM LettreReglement lr WHERE lr.cheminFichierPdf IS NULL ORDER BY lr.dateTransmission")
    List<LettreReglement> findSettlementLettersWithoutPdf();
    
    /**
     * Get total settlement amount by currency
     */
    @Query("SELECT lr.devise, SUM(lr.montant) FROM LettreReglement lr GROUP BY lr.devise")
    List<Object[]> getTotalSettlementAmountByCurrency();
    
    /**
     * Get total settlement amount by treasury account
     */
    @Query("SELECT lr.compteTresor, SUM(lr.montant) FROM LettreReglement lr GROUP BY lr.compteTresor")
    List<Object[]> getTotalSettlementAmountByAccount();

    /**
     * Find settlement letter by ID with payment order ID
     */
    @Query("SELECT lr.id, lr.ordrePaiement.id FROM LettreReglement lr WHERE lr.id = :id")
    Object[] findLettreReglementPaymentOrderId(@Param("id") Long id);
    
    /**
     * Find duplicate settlement letters by ID
     */
    @Query(value = "SELECT id, COUNT(*) as count FROM lettre_reglement GROUP BY id HAVING COUNT(*) > 1", nativeQuery = true)
    List<Object[]> findDuplicateIds();
    
    /**
     * Search settlement letters by multiple criteria
     */
    @Query("SELECT lr FROM LettreReglement lr WHERE " +
           "(:numeroLettre IS NULL OR lr.numeroLettre LIKE %:numeroLettre%) AND " +
           "(:devise IS NULL OR lr.devise = :devise) AND " +
           "(:compteTresor IS NULL OR lr.compteTresor LIKE %:compteTresor%) AND " +
           "(:pretId IS NULL OR lr.pret.id = :pretId) " +
           "ORDER BY lr.dateTransmission")
    List<LettreReglement> searchByCriteria(
        @Param("numeroLettre") String numeroLettre,
        @Param("devise") String devise,
        @Param("compteTresor") String compteTresor,
        @Param("pretId") Long pretId);
}