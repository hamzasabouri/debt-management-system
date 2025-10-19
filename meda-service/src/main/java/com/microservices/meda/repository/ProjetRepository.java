package com.microservices.meda.repository;

import com.microservices.meda.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    
    /**
     * Find project by name (case-insensitive)
     */
    Optional<Projet> findByNomProjetIgnoreCase(String nomProjet);
    
    /**
     * Check if project name exists (excluding specific ID)
     */
    boolean existsByNomProjetIgnoreCaseAndIdNot(String nomProjet, Long id);
    
    /**
     * Check if project name exists
     */
    boolean existsByNomProjetIgnoreCase(String nomProjet);
    
    /**
     * Find projects by currency
     */
    List<Projet> findByDeviseOrderByDateDebut(String devise);
    
    /**
     * Find projects by date range
     */
    @Query("SELECT p FROM Projet p WHERE p.dateDebut >= :startDate AND p.dateFin <= :endDate ORDER BY p.dateDebut")
    List<Projet> findProjectsByDateRange(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    /**
     * Find active projects (end date in future)
     */
    @Query("SELECT p FROM Projet p WHERE p.dateFin >= :currentDate ORDER BY p.dateFin")
    List<Projet> findActiveProjects(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find completed projects (end date in past)
     */
    @Query("SELECT p FROM Projet p WHERE p.dateFin < :currentDate ORDER BY p.dateFin DESC")
    List<Projet> findCompletedProjects(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find projects by total amount range
     */
    @Query("SELECT p FROM Projet p WHERE p.montantTotal BETWEEN :minAmount AND :maxAmount ORDER BY p.montantTotal")
    List<Projet> findProjectsByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                         @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Find projects with advances
     */
    @Query("SELECT DISTINCT p FROM Projet p JOIN p.avances a ORDER BY p.nomProjet")
    List<Projet> findProjectsWithAdvances();
    
    /**
     * Find projects without advances
     */
    @Query("SELECT p FROM Projet p WHERE p.avances IS EMPTY ORDER BY p.nomProjet")
    List<Projet> findProjectsWithoutAdvances();
    
    /**
     * Find projects with supporting documents
     */
    @Query("SELECT DISTINCT p FROM Projet p JOIN p.piecesJustificatives pj ORDER BY p.nomProjet")
    List<Projet> findProjectsWithSupportingDocuments();
    
    /**
     * Search projects by name containing text
     */
    @Query("SELECT p FROM Projet p WHERE LOWER(p.nomProjet) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY p.nomProjet")
    List<Projet> searchProjectsByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    /**
     * Get project statistics
     */
    @Query("SELECT COUNT(p), SUM(p.montantTotal), AVG(p.montantTotal) FROM Projet p")
    Object[] getProjectStatistics();
    
    /**
     * Get project statistics by currency
     */
    @Query("SELECT p.devise, COUNT(p), SUM(p.montantTotal) FROM Projet p GROUP BY p.devise ORDER BY p.devise")
    List<Object[]> getProjectStatisticsByCurrency();
    
    /**
     * Find projects ending within next N days
     */
    @Query("SELECT p FROM Projet p WHERE p.dateFin BETWEEN :today AND :futureDate ORDER BY p.dateFin")
    List<Projet> findProjectsEndingSoon(@Param("today") LocalDate today, 
                                      @Param("futureDate") LocalDate futureDate);
    
    /**
     * Get projects with their financial summary
     */
    @Query("SELECT p, " +
           "(SELECT COALESCE(SUM(a.montant), 0) FROM Avance a WHERE a.projet = p) as totalAvances, " +
           "(SELECT COALESCE(SUM(pj.montant), 0) FROM PieceJustificative pj WHERE pj.projet = p AND pj.statut = 'VALIDE') as totalDepenses " +
           "FROM Projet p ORDER BY p.nomProjet")
    List<Object[]> getProjectsWithFinancialSummary();
    
    /**
     * Count projects by status (active/completed)
     */
    @Query("SELECT " +
           "(SELECT COUNT(p1) FROM Projet p1 WHERE p1.dateFin >= :currentDate) as active, " +
           "(SELECT COUNT(p2) FROM Projet p2 WHERE p2.dateFin < :currentDate) as completed")
    Object[] countProjectsByStatus(@Param("currentDate") LocalDate currentDate);
}