package com.microservices.detteinterieur.integration.bam;

import com.microservices.detteinterieur.dto.bam.AvisCreditBAMDto;
import com.microservices.detteinterieur.dto.bam.AvisDebitBAMDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for BAM (Bank Al-Maghrib) integration service
 * Handles communication with BAM for credit/debit notices
 */
public interface BAMIntegrationService {
    
    /**
     * Send credit notice to BAM
     * @param avisCreditDto Credit notice data
     * @return Response from BAM with processing status
     */
    AvisCreditBAMDto envoyerAvisCredit(AvisCreditBAMDto avisCreditDto);
    
    /**
     * Receive debit notice from BAM
     * @param avisDebitDto Debit notice data received from BAM
     * @return Processed debit notice with internal status
     */
    AvisDebitBAMDto recevoirAvisDebit(AvisDebitBAMDto avisDebitDto);
    
    /**
     * Check status of credit notice at BAM
     * @param numeroAvis Credit notice number
     * @return Updated credit notice status
     */
    Optional<AvisCreditBAMDto> verifierStatutAvisCredit(String numeroAvis);
    
    /**
     * Get all credit notices sent to BAM for a period
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of credit notices
     */
    List<AvisCreditBAMDto> getAvisCreditParPeriode(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get all debit notices received from BAM for a period
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of debit notices
     */
    List<AvisDebitBAMDto> getAvisDebitParPeriode(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get pending credit notices (not yet processed by BAM)
     * @return List of pending credit notices
     */
    List<AvisCreditBAMDto> getAvisCreditEnAttente();
    
    /**
     * Get unprocessed debit notices (received but not processed)
     * @return List of unprocessed debit notices
     */
    List<AvisDebitBAMDto> getAvisDebitNonTraites();
    
    /**
     * Retry sending failed credit notice
     * @param numeroAvis Credit notice number to retry
     * @return Updated credit notice status
     */
    AvisCreditBAMDto retenterEnvoiAvisCredit(String numeroAvis);
    
    /**
     * Cancel credit notice at BAM
     * @param numeroAvis Credit notice number to cancel
     * @param motifAnnulation Cancellation reason
     * @return Cancellation confirmation
     */
    boolean annulerAvisCredit(String numeroAvis, String motifAnnulation);
    
    /**
     * Test connectivity with BAM system
     * @return true if connection is successful, false otherwise
     */
    boolean testerConnexion();
    
    /**
     * Get BAM system status
     * @return System status information
     */
    String getStatutSystemeBAM();
}