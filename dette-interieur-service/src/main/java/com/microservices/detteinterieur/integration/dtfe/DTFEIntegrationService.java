package com.microservices.detteinterieur.integration.dtfe;

import com.microservices.detteinterieur.dto.dtfe.OperationTresorDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for DTFE (Direction du Trésor et des Finances Extérieures) integration service
 * Handles communication with treasury for debt management and financial operations
 */
public interface DTFEIntegrationService {
    
    /**
     * Send treasury operation to DTFE
     * @param operationDto Treasury operation data
     * @return Response from DTFE with processing status
     */
    OperationTresorDto envoyerOperationTresor(OperationTresorDto operationDto);
    
    /**
     * Check status of treasury operation at DTFE
     * @param numeroOperation Operation number
     * @return Updated operation status
     */
    Optional<OperationTresorDto> verifierStatutOperation(String numeroOperation);
    
    /**
     * Get treasury operations for a period
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of treasury operations
     */
    List<OperationTresorDto> getOperationsParPeriode(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get treasury operations by type
     * @param typeOperation Operation type
     * @param dateDebut Start date
     * @param dateFin End date
     * @return List of operations by type
     */
    List<OperationTresorDto> getOperationsParType(String typeOperation, LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Get pending treasury operations (awaiting validation)
     * @return List of pending operations
     */
    List<OperationTresorDto> getOperationsEnAttente();
    
    /**
     * Get validated treasury operations
     * @return List of validated operations
     */
    List<OperationTresorDto> getOperationsValidees();
    
    /**
     * Get rejected treasury operations
     * @return List of rejected operations
     */
    List<OperationTresorDto> getOperationsREJETEs();
    
    /**
     * Request validation for treasury operation
     * @param numeroOperation Operation number to validate
     * @param observations Validation observations
     * @return Validation result
     */
    OperationTresorDto demanderValidation(String numeroOperation, String observations);
    
    /**
     * Cancel treasury operation at DTFE
     * @param numeroOperation Operation number to cancel
     * @param motifAnnulation Cancellation reason
     * @return Cancellation confirmation
     */
    boolean annulerOperation(String numeroOperation, String motifAnnulation);
    
    /**
     * Get debt portfolio summary from DTFE
     * @param dateReference Reference date for the portfolio
     * @return Portfolio summary data
     */
    PortefeuilleDetteDto getPortefeuilleDetteInterieure(LocalDate dateReference);
    
    /**
     * Get treasury account balances from DTFE
     * @param dateReference Reference date
     * @return Account balances
     */
    List<SoldeTresorDto> getSoldesCompteTresor(LocalDate dateReference);
    
    /**
     * Submit monthly debt report to DTFE
     * @param mois Month (1-12)
     * @param annee Year
     * @param rapportDto Report data
     * @return Submission confirmation
     */
    boolean soumettreRapportMensuel(int mois, int annee, RapportDetteDto rapportDto);
    
    /**
     * Get debt statistics from DTFE
     * @param dateDebut Start date
     * @param dateFin End date
     * @return Debt statistics
     */
    StatistiquesDetteDto getStatistiquesDetteInterieure(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Test connectivity with DTFE system
     * @return true if connection is successful, false otherwise
     */
    boolean testerConnexion();
    
    /**
     * Get DTFE system status
     * @return System status information
     */
    String getStatutSystemeDTFE();
    
    /**
     * Portfolio summary DTO
     */
    public static class PortefeuilleDetteDto {
        private LocalDate dateReference;
        private String devise;
        private java.math.BigDecimal encoursTotalAdjudications;
        private java.math.BigDecimal encoursTotalBonsEquipement;
        private java.math.BigDecimal encoursCollectivitesLocales;
        private java.math.BigDecimal encoursDepotsTreesor;
        private java.math.BigDecimal totalIntereêtsCourus;
        private java.math.BigDecimal totalCommissionsPayees;
        private int nombreOperationsActives;
        
        // Constructor and getters
        public PortefeuilleDetteDto(LocalDate dateReference, String devise, 
                java.math.BigDecimal encoursTotalAdjudications, java.math.BigDecimal encoursTotalBonsEquipement,
                java.math.BigDecimal encoursCollectivitesLocales, java.math.BigDecimal encoursDepotsTreesor,
                java.math.BigDecimal totalIntereêtsCourus, java.math.BigDecimal totalCommissionsPayees, 
                int nombreOperationsActives) {
            this.dateReference = dateReference;
            this.devise = devise;
            this.encoursTotalAdjudications = encoursTotalAdjudications;
            this.encoursTotalBonsEquipement = encoursTotalBonsEquipement;
            this.encoursCollectivitesLocales = encoursCollectivitesLocales;
            this.encoursDepotsTreesor = encoursDepotsTreesor;
            this.totalIntereêtsCourus = totalIntereêtsCourus;
            this.totalCommissionsPayees = totalCommissionsPayees;
            this.nombreOperationsActives = nombreOperationsActives;
        }
        
        public LocalDate getDateReference() { return dateReference; }
        public String getDevise() { return devise; }
        public java.math.BigDecimal getEncoursTotalAdjudications() { return encoursTotalAdjudications; }
        public java.math.BigDecimal getEncoursTotalBonsEquipement() { return encoursTotalBonsEquipement; }
        public java.math.BigDecimal getEncoursCollectivitesLocales() { return encoursCollectivitesLocales; }
        public java.math.BigDecimal getEncoursDepotsTreesor() { return encoursDepotsTreesor; }
        public java.math.BigDecimal getTotalIntereêtsCourus() { return totalIntereêtsCourus; }
        public java.math.BigDecimal getTotalCommissionsPayees() { return totalCommissionsPayees; }
        public int getNombreOperationsActives() { return nombreOperationsActives; }
    }
    
    /**
     * Treasury account balance DTO
     */
    public static class SoldeTresorDto {
        private String numeroCompte;
        private String libelleCompte;
        private String devise;
        private java.math.BigDecimal solde;
        private LocalDate dateSolde;
        private String typeCompte;
        
        public SoldeTresorDto(String numeroCompte, String libelleCompte, String devise, 
                java.math.BigDecimal solde, LocalDate dateSolde, String typeCompte) {
            this.numeroCompte = numeroCompte;
            this.libelleCompte = libelleCompte;
            this.devise = devise;
            this.solde = solde;
            this.dateSolde = dateSolde;
            this.typeCompte = typeCompte;
        }
        
        public String getNumeroCompte() { return numeroCompte; }
        public String getLibelleCompte() { return libelleCompte; }
        public String getDevise() { return devise; }
        public java.math.BigDecimal getSolde() { return solde; }
        public LocalDate getDateSolde() { return dateSolde; }
        public String getTypeCompte() { return typeCompte; }
    }
    
    /**
     * Monthly debt report DTO
     */
    public static class RapportDetteDto {
        private int mois;
        private int annee;
        private String devise;
        private java.math.BigDecimal nouvelleDette;
        private java.math.BigDecimal remboursements;
        private java.math.BigDecimal interetsPaiesyés;
        private java.math.BigDecimal commissionsPayees;
        private java.math.BigDecimal soldeFinPeriode;
        private String observations;
        
        public RapportDetteDto(int mois, int annee, String devise, java.math.BigDecimal nouvelleDette,
                java.math.BigDecimal remboursements, java.math.BigDecimal interetsPaiesyés,
                java.math.BigDecimal commissionsPayees, java.math.BigDecimal soldeFinPeriode, String observations) {
            this.mois = mois;
            this.annee = annee;
            this.devise = devise;
            this.nouvelleDette = nouvelleDette;
            this.remboursements = remboursements;
            this.interetsPaiesyés = interetsPaiesyés;
            this.commissionsPayees = commissionsPayees;
            this.soldeFinPeriode = soldeFinPeriode;
            this.observations = observations;
        }
        
        public int getMois() { return mois; }
        public int getAnnee() { return annee; }
        public String getDevise() { return devise; }
        public java.math.BigDecimal getNouvelleDette() { return nouvelleDette; }
        public java.math.BigDecimal getRemboursements() { return remboursements; }
        public java.math.BigDecimal getInteretsPaiesyés() { return interetsPaiesyés; }
        public java.math.BigDecimal getCommissionsPayees() { return commissionsPayees; }
        public java.math.BigDecimal getSoldeFinPeriode() { return soldeFinPeriode; }
        public String getObservations() { return observations; }
    }
    
    /**
     * Debt statistics DTO
     */
    public static class StatistiquesDetteDto {
        private LocalDate periodeDebut;
        private LocalDate periodeFin;
        private String devise;
        private int nombreOperations;
        private java.math.BigDecimal montantTotal;
        private java.math.BigDecimal montantMoyen;
        private java.math.BigDecimal tauxMoyenPondere;
        private int dureeMoyenneJours;
        
        public StatistiquesDetteDto(LocalDate periodeDebut, LocalDate periodeFin, String devise,
                int nombreOperations, java.math.BigDecimal montantTotal, java.math.BigDecimal montantMoyen,
                java.math.BigDecimal tauxMoyenPondere, int dureeMoyenneJours) {
            this.periodeDebut = periodeDebut;
            this.periodeFin = periodeFin;
            this.devise = devise;
            this.nombreOperations = nombreOperations;
            this.montantTotal = montantTotal;
            this.montantMoyen = montantMoyen;
            this.tauxMoyenPondere = tauxMoyenPondere;
            this.dureeMoyenneJours = dureeMoyenneJours;
        }
        
        public LocalDate getPeriodeDebut() { return periodeDebut; }
        public LocalDate getPeriodeFin() { return periodeFin; }
        public String getDevise() { return devise; }
        public int getNombreOperations() { return nombreOperations; }
        public java.math.BigDecimal getMontantTotal() { return montantTotal; }
        public java.math.BigDecimal getMontantMoyen() { return montantMoyen; }
        public java.math.BigDecimal getTauxMoyenPondere() { return tauxMoyenPondere; }
        public int getDureeMoyenneJours() { return dureeMoyenneJours; }
    }
}