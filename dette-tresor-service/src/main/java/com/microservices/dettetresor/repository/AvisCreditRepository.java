package com.microservices.dettetresor.repository;

import com.microservices.dettetresor.entity.AvisCredit;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.entity.AvisCredit.TypeAvisCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisCreditRepository extends JpaRepository<AvisCredit, Long> {
    
    /**
     * Vérifie si un avis de crédit avec le numéro donné existe (en excluant un ID spécifique)
     */
    boolean existsByNumeroAvisAndIdNot(String numeroAvis, Long id);
    
    /**
     * Trouve tous les avis de crédit pour un prêt donné
     */
    List<AvisCredit> findByPretId(Long pretId);
    
    /**
     * Vérifie si un avis de crédit avec le numéro donné existe
     */
    boolean existsByNumeroAvis(String numeroAvis);
    
    /**
     * Trouve un avis de crédit par son numéro
     */
    Optional<AvisCredit> findByNumeroAvis(String numeroAvis);
    
    /**
     * Trouve les avis de crédit pour un prêt donné, triés par date de réception
     */
    List<AvisCredit> findByPretIdOrderByDateReception(Long pretId);
    
    /**
     * Trouve les avis de crédit pour une entité Prêt donnée
     */
    List<AvisCredit> findByPret(Pret pret);
    
    /**
     * Trouve les avis de crédit par type, triés par date de réception
     */
    List<AvisCredit> findByTypeAvisOrderByDateReception(TypeAvisCredit typeAvis);
    
    /**
     * Trouve les avis de crédit par émetteur (recherche insensible à la casse), triés par date de réception
     */
    List<AvisCredit> findByEmetteurContainingIgnoreCaseOrderByDateReception(String emetteur);
    
    /**
     * Trouve les avis de crédit par numéro (recherche insensible à la casse)
     */
    List<AvisCredit> findByNumeroAvisContainingIgnoreCase(String numeroAvis);
    
    /**
     * Trouve les avis de crédit par plage de dates de réception
     */
    List<AvisCredit> findByDateReceptionBetweenOrderByDateReception(java.time.LocalDate startDate, java.time.LocalDate endDate);
}