package com.microservices.meda.repository;

import com.microservices.meda.entity.PieceJustificative;
import com.microservices.meda.entity.PieceJustificative.TypePiece;
import com.microservices.meda.entity.PieceJustificative.StatutPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PieceJustificativeRepository extends JpaRepository<PieceJustificative, Long> {
    
    /**
     * Find supporting document by number
     */
    Optional<PieceJustificative> findByNumeroPiece(String numeroPiece);
    
    /**
     * Check if document number exists
     */
    boolean existsByNumeroPiece(String numeroPiece);
    
    /**
     * Check if document number exists (excluding specific ID)
     */
    boolean existsByNumeroPieceAndIdNot(String numeroPiece, Long id);
    
    /**
     * Find documents by project ID
     */
    List<PieceJustificative> findByProjetIdOrderByDateExecution(Long projetId);
    
    /**
     * Find documents by project ID and type
     */
    List<PieceJustificative> findByProjetIdAndTypePieceOrderByDateExecution(Long projetId, TypePiece typePiece);
    
    /**
     * Find documents by project ID and status
     */
    List<PieceJustificative> findByProjetIdAndStatutOrderByDateExecution(Long projetId, StatutPiece statut);
    
    /**
     * Find documents by type
     */
    List<PieceJustificative> findByTypePieceOrderByDateExecution(TypePiece typePiece);
    
    /**
     * Find documents by status
     */
    List<PieceJustificative> findByStatutOrderByDateExecution(StatutPiece statut);
    
    /**
     * Find documents by currency
     */
    List<PieceJustificative> findByDeviseOrderByDateExecution(String devise);
    
    /**
     * Find documents by issuer (accountant)
     */
    List<PieceJustificative> findByEmetteurIgnoreCaseOrderByDateExecution(String emetteur);
    
    /**
     * Find documents by execution date range
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.dateExecution BETWEEN :startDate AND :endDate ORDER BY pj.dateExecution")
    List<PieceJustificative> findByExecutionDateBetween(@Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Find documents by amount range
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.montant BETWEEN :minAmount AND :maxAmount ORDER BY pj.montant")
    List<PieceJustificative> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                             @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Get total validated expenses by project
     */
    @Query("SELECT COALESCE(SUM(pj.montant), 0) FROM PieceJustificative pj WHERE pj.projet.id = :projetId AND pj.statut = 'VALIDE'")
    BigDecimal getTotalValidatedExpensesByProject(@Param("projetId") Long projetId);
    
    /**
     * Get total expenses by project and type
     */
    @Query("SELECT COALESCE(SUM(pj.montant), 0) FROM PieceJustificative pj WHERE pj.projet.id = :projetId AND pj.typePiece = :typePiece AND pj.statut = 'VALIDE'")
    BigDecimal getTotalExpensesByProjectAndType(@Param("projetId") Long projetId, 
                                              @Param("typePiece") TypePiece typePiece);
    
    /**
     * Get total expenses by project and status
     */
    @Query("SELECT COALESCE(SUM(pj.montant), 0) FROM PieceJustificative pj WHERE pj.projet.id = :projetId AND pj.statut = :statut")
    BigDecimal getTotalExpensesByProjectAndStatus(@Param("projetId") Long projetId, 
                                                @Param("statut") StatutPiece statut);
    
    /**
     * Get documents statistics by type
     */
    @Query("SELECT pj.typePiece, COUNT(pj), SUM(pj.montant), AVG(pj.montant) FROM PieceJustificative pj GROUP BY pj.typePiece")
    List<Object[]> getDocumentsStatisticsByType();
    
    /**
     * Get documents statistics by status
     */
    @Query("SELECT pj.statut, COUNT(pj), SUM(pj.montant) FROM PieceJustificative pj GROUP BY pj.statut")
    List<Object[]> getDocumentsStatisticsByStatus();
    
    /**
     * Get documents statistics by currency
     */
    @Query("SELECT pj.devise, COUNT(pj), SUM(pj.montant) FROM PieceJustificative pj GROUP BY pj.devise ORDER BY pj.devise")
    List<Object[]> getDocumentsStatisticsByCurrency();
    
    /**
     * Find recent documents (last N days)
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.dateExecution >= :fromDate ORDER BY pj.dateExecution DESC")
    List<PieceJustificative> findRecentDocuments(@Param("fromDate") LocalDate fromDate);
    
    /**
     * Find documents pending validation
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.statut = 'PRIS_EN_CHARGE' ORDER BY pj.dateExecution")
    List<PieceJustificative> findDocumentsPendingValidation();
    
    /**
     * Find rejected documents
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.statut = 'REJETE' ORDER BY pj.dateExecution DESC")
    List<PieceJustificative> findRejectedDocuments();
    
    /**
     * Count documents by project and type
     */
    @Query("SELECT pj.typePiece, COUNT(pj) FROM PieceJustificative pj WHERE pj.projet.id = :projetId GROUP BY pj.typePiece")
    List<Object[]> countDocumentsByProjectAndType(@Param("projetId") Long projetId);
    
    /**
     * Count documents by project and status
     */
    @Query("SELECT pj.statut, COUNT(pj) FROM PieceJustificative pj WHERE pj.projet.id = :projetId GROUP BY pj.statut")
    List<Object[]> countDocumentsByProjectAndStatus(@Param("projetId") Long projetId);
    
    /**
     * Find largest expenses (top N by amount)
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.statut = 'VALIDE' ORDER BY pj.montant DESC")
    List<PieceJustificative> findLargestValidatedExpenses();
    
    /**
     * Search documents by project name or document number
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE LOWER(pj.numeroPiece) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(pj.projet.nomProjet) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY pj.dateExecution DESC")
    List<PieceJustificative> searchDocuments(@Param("searchTerm") String searchTerm);
    
    /**
     * Get monthly expenses summary (validated only)
     */
    @Query("SELECT YEAR(pj.dateExecution), MONTH(pj.dateExecution), COUNT(pj), SUM(pj.montant) " +
           "FROM PieceJustificative pj WHERE pj.statut = 'VALIDE' " +
           "GROUP BY YEAR(pj.dateExecution), MONTH(pj.dateExecution) " +
           "ORDER BY YEAR(pj.dateExecution) DESC, MONTH(pj.dateExecution) DESC")
    List<Object[]> getMonthlyValidatedExpensesSummary();
    
    /**
     * Find documents with comments (rejected or with notes)
     */
    @Query("SELECT pj FROM PieceJustificative pj WHERE pj.commentaire IS NOT NULL AND pj.commentaire != '' ORDER BY pj.dateExecution DESC")
    List<PieceJustificative> findDocumentsWithComments();
    
    /**
     * Get project expense utilization rate
     */
    @Query("SELECT p.id, p.nomProjet, p.montantTotal, " +
           "COALESCE(SUM(CASE WHEN pj.statut = 'VALIDE' THEN pj.montant ELSE 0 END), 0) as totalExpenses, " +
           "(COALESCE(SUM(CASE WHEN pj.statut = 'VALIDE' THEN pj.montant ELSE 0 END), 0) * 100.0 / p.montantTotal) as utilizationRate " +
           "FROM Projet p LEFT JOIN p.piecesJustificatives pj " +
           "GROUP BY p.id, p.nomProjet, p.montantTotal " +
           "ORDER BY utilizationRate DESC")
    List<Object[]> getProjectExpenseUtilization();
}