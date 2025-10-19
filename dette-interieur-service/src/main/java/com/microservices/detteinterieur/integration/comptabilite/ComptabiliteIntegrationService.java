package com.microservices.detteinterieur.integration.comptabilite;

import com.microservices.detteinterieur.dto.comptabilite.EcritureComptableDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for Comptabilite integration service
 * Handles communication with accounting system for journal entries and supporting documents
 */
public interface ComptabiliteIntegrationService {
    
    /**
     * Send accounting entry to Comptabilite system
     * @param ecritureDto Accounting entry data
     * @return Response from Comptabilite with processing status
     */
    EcritureComptableDto envoyerEcritureComptable(EcritureComptableDto ecritureDto);
    
    /**
     * Check status of accounting entry in Comptabilite system
     * @param numeroEcriture Accounting entry number
     * @return Updated accounting entry status
     */
    Optional<EcritureComptableDto> verifierStatutEcriture(String numeroEcriture);
    
    /**
     * Get accounting entries for a period
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of accounting entries
     */
    List<EcritureComptableDto> getEcrituresParPeriode(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get accounting entries by journal
     * @param journal Journal code
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of accounting entries for the journal
     */
    List<EcritureComptableDto> getEcrituresParJournal(String journal, LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get pending accounting entries (not yet posted)
     * @return List of pending accounting entries
     */
    List<EcritureComptableDto> getEcrituresEnAttente();
    
    /**
     * Get rejected accounting entries
     * @return List of rejected accounting entries
     */
    List<EcritureComptableDto> getEcrituresREJETEs();
    
    /**
     * Retry sending failed accounting entry
     * @param numeroEcriture Accounting entry number to retry
     * @return Updated accounting entry status
     */
    EcritureComptableDto retenterEnvoiEcriture(String numeroEcriture);
    
    /**
     * Cancel accounting entry in Comptabilite system
     * @param numeroEcriture Accounting entry number to cancel
     * @param motifAnnulation Cancellation reason
     * @return Cancellation confirmation
     */
    boolean annulerEcriture(String numeroEcriture, String motifAnnulation);
    
    /**
     * Validate accounting entry before sending
     * @param ecritureDto Accounting entry to validate
     * @return Validation result with errors if any
     */
    ValidationResult validerEcriture(EcritureComptableDto ecritureDto);
    
    /**
     * Get chart of accounts from Comptabilite system
     * @return List of account codes and descriptions
     */
    List<CompteComptableDto> getPlanComptable();
    
    /**
     * Get cost centers from Comptabilite system
     * @return List of cost centers
     */
    List<CentreCoutDto> getCentresCout();
    
    /**
     * Get available journals from Comptabilite system
     * @return List of journals
     */
    List<JournalDto> getJournauxDisponibles();
    
    /**
     * Test connectivity with Comptabilite system
     * @return true if connection is successful, false otherwise
     */
    boolean testerConnexion();
    
    /**
     * Get Comptabilite system status
     * @return System status information
     */
    String getStatutSystemeComptabilite();
    
    /**
     * Validation result DTO
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
    
    /**
     * Chart of accounts DTO
     */
    public static class CompteComptableDto {
        private String codeCompte;
        private String libelleCompte;
        private String typeCompte;
        private boolean actif;
        
        public CompteComptableDto(String codeCompte, String libelleCompte, String typeCompte, boolean actif) {
            this.codeCompte = codeCompte;
            this.libelleCompte = libelleCompte;
            this.typeCompte = typeCompte;
            this.actif = actif;
        }
        
        public String getCodeCompte() { return codeCompte; }
        public String getLibelleCompte() { return libelleCompte; }
        public String getTypeCompte() { return typeCompte; }
        public boolean isActif() { return actif; }
    }
    
    /**
     * Cost center DTO
     */
    public static class CentreCoutDto {
        private String codeCentre;
        private String libelleCentre;
        private String responsable;
        private boolean actif;
        
        public CentreCoutDto(String codeCentre, String libelleCentre, String responsable, boolean actif) {
            this.codeCentre = codeCentre;
            this.libelleCentre = libelleCentre;
            this.responsable = responsable;
            this.actif = actif;
        }
        
        public String getCodeCentre() { return codeCentre; }
        public String getLibelleCentre() { return libelleCentre; }
        public String getResponsable() { return responsable; }
        public boolean isActif() { return actif; }
    }
    
    /**
     * Journal DTO
     */
    public static class JournalDto {
        private String codeJournal;
        private String libelleJournal;
        private String typeJournal;
        private boolean actif;
        
        public JournalDto(String codeJournal, String libelleJournal, String typeJournal, boolean actif) {
            this.codeJournal = codeJournal;
            this.libelleJournal = libelleJournal;
            this.typeJournal = typeJournal;
            this.actif = actif;
        }
        
        public String getCodeJournal() { return codeJournal; }
        public String getLibelleJournal() { return libelleJournal; }
        public String getTypeJournal() { return typeJournal; }
        public boolean isActif() { return actif; }
    }
}