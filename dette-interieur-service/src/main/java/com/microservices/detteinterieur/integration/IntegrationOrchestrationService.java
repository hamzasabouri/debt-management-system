package com.microservices.detteinterieur.integration;

import com.microservices.detteinterieur.dto.bam.AvisCreditBAMDto;
import com.microservices.detteinterieur.dto.comptabilite.EcritureComptableDto;
import com.microservices.detteinterieur.dto.dtfe.OperationTresorDto;
import com.microservices.detteinterieur.entity.*;
import com.microservices.detteinterieur.integration.bam.BAMIntegrationService;
import com.microservices.detteinterieur.integration.comptabilite.ComptabiliteIntegrationService;
import com.microservices.detteinterieur.integration.dtfe.DTFEIntegrationService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Orchestration service for coordinating integrations between BAM, Comptabilite, and DTFE
 * Handles complex business processes that require multiple system interactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationOrchestrationService {
    
    private final BAMIntegrationService bamIntegrationService;
    private final ComptabiliteIntegrationService comptabiliteIntegrationService;
    private final DTFEIntegrationService dtfeIntegrationService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    /**
     * Process adjudication completion - coordinates all systems
     */
    @Transactional
    public ProcessingResult processAdjudicationCompletion(Adjudication adjudication) {
        log.info("Processing adjudication completion: {}", adjudication.getNumeroAdjud());
        
        ProcessingResult result = ProcessingResult.builder()
                .numeroOperation(adjudication.getNumeroAdjud())
                .typeOperation("ADJUDICATION_COMPLETION")
                .dateTraitement(LocalDateTime.now())
                .success(true)
                .messages(new ArrayList<>())
                .build();
        
        try {
            // Send to all systems in parallel
            CompletableFuture<Boolean> bamTask = CompletableFuture.supplyAsync(() -> 
                sendAdjudicationToBAM(adjudication), executorService);
            CompletableFuture<Boolean> comptaTask = CompletableFuture.supplyAsync(() -> 
                sendAdjudicationToComptabilite(adjudication), executorService);
            CompletableFuture<Boolean> dtfeTask = CompletableFuture.supplyAsync(() -> 
                sendAdjudicationToDTFE(adjudication), executorService);
            
            // Wait and compile results
            result.setSuccess(bamTask.get() && comptaTask.get() && dtfeTask.get());
            result.getMessages().add("Adjudication processed for all systems");
            
        } catch (Exception e) {
            log.error("Error processing adjudication: {}", e.getMessage());
            result.setSuccess(false);
            result.getMessages().add("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Process commission payment
     */
    @Transactional
    public ProcessingResult processCommissionPayment(Commission commission) {
        log.info("Processing commission payment: {}", commission.getNumeroReference());
        
        ProcessingResult result = ProcessingResult.builder()
                .numeroOperation(commission.getNumeroReference())
                .typeOperation("COMMISSION_PAYMENT")
                .dateTraitement(LocalDateTime.now())
                .success(true)
                .messages(new ArrayList<>())
                .build();
        
        try {
            boolean bamSuccess = sendCommissionToBAM(commission);
            boolean comptaSuccess = sendCommissionToComptabilite(commission);
            boolean dtfeSuccess = sendCommissionToDTFE(commission);
            
            result.setSuccess(bamSuccess && comptaSuccess && dtfeSuccess);
            result.getMessages().add("Commission processed for all systems");
            
        } catch (Exception e) {
            log.error("Error processing commission: {}", e.getMessage());
            result.setSuccess(false);
            result.getMessages().add("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check systems health
     */
    public SystemHealthResult checkSystemsHealth() {
        return SystemHealthResult.builder()
                .dateVerification(LocalDateTime.now())
                .bamConnected(bamIntegrationService.testerConnexion())
                .comptabiliteConnected(comptabiliteIntegrationService.testerConnexion())
                .dtfeConnected(dtfeIntegrationService.testerConnexion())
                .bamStatus(bamIntegrationService.getStatutSystemeBAM())
                .comptabiliteStatus(comptabiliteIntegrationService.getStatutSystemeComptabilite())
                .dtfeStatus(dtfeIntegrationService.getStatutSystemeDTFE())
                .build();
    }
    
    // Helper methods
    private boolean sendAdjudicationToBAM(Adjudication adjudication) {
        try {
            AvisCreditBAMDto avisCredit = AvisCreditBAMDto.builder()
                    .numeroAvis("AC-ADJ-" + adjudication.getNumeroAdjud())
                    .typeOperation("ADJUDICATION")
                    .referenceOperation(adjudication.getNumeroAdjud())
                    .dateOperation(adjudication.getDateAdjud())
                    .montant(adjudication.getMontantTotal())
                    .devise("MAD")
                    .compteBeneficiaire("COMPTE_TRESOR")
                    .nomBeneficiaire("TRESOR PUBLIC")
                    .motifCredit("Adjudication dette intérieure")
                    .dateValeur(adjudication.getDateAdjud())
                    .build();
            
            AvisCreditBAMDto result = bamIntegrationService.envoyerAvisCredit(avisCredit);
            return result != null && result.isTraite();
        } catch (Exception e) {
            log.error("Error sending adjudication to BAM: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendAdjudicationToComptabilite(Adjudication adjudication) {
        try {
            List<EcritureComptableDto.LigneComptableDto> lignes = new ArrayList<>();
            lignes.add(EcritureComptableDto.LigneComptableDto.builder()
                    .compte("512100").libelleCompte("Encaissement adjudication")
                    .montantDebit(adjudication.getMontantTotal()).build());
            lignes.add(EcritureComptableDto.LigneComptableDto.builder()
                    .compte("451100").libelleCompte("Dette intérieure")
                    .montantCredit(adjudication.getMontantTotal()).build());
            
            EcritureComptableDto ecriture = EcritureComptableDto.builder()
                    .numeroEcriture("ECR-ADJ-" + adjudication.getNumeroAdjud())
                    .dateEcriture(adjudication.getDateAdjud())
                    .libelleEcriture("Adjudication dette intérieure")
                    .montantTotal(adjudication.getMontantTotal())
                    .lignesComptables(lignes)
                    .build();
            
            EcritureComptableDto result = comptabiliteIntegrationService.envoyerEcritureComptable(ecriture);
            return result != null && "TRAITE".equals(result.getStatutComptabilisation());
        } catch (Exception e) {
            log.error("Error sending adjudication to Comptabilite: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendAdjudicationToDTFE(Adjudication adjudication) {
        try {
            OperationTresorDto operation = OperationTresorDto.builder()
                    .numeroOperation("OP-ADJ-" + adjudication.getNumeroAdjud())
                    .typeOperation("ADJUDICATION")
                    .dateOperation(adjudication.getDateAdjud())
                    .montantOperation(adjudication.getMontantTotal())
                    .devise("MAD")
                    .description("Adjudication dette intérieure")
                    .build();
            
            OperationTresorDto result = dtfeIntegrationService.envoyerOperationTresor(operation);
            return result != null && "VALIDEE".equals(result.getStatutValidation());
        } catch (Exception e) {
            log.error("Error sending adjudication to DTFE: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendCommissionToBAM(Commission commission) {
        try {
            AvisCreditBAMDto avisCredit = AvisCreditBAMDto.builder()
                    .numeroAvis("AC-COM-" + commission.getNumeroReference())
                    .typeOperation("COMMISSION")
                    .referenceOperation(commission.getNumeroReference())
                    .dateOperation(commission.getCreatedAt().toLocalDate())
                    .montant(commission.getMontant())
                    .devise("MAD")
                    .compteBeneficiaire("COMPTE_" + commission.getTypeCommission().name())
                    .nomBeneficiaire(commission.getTypeCommission().getDescription().toUpperCase())
                    .motifCredit("Commission " + commission.getTypeCommission().getDescription())
                    .dateValeur(commission.getDatePaiement() != null ? commission.getDatePaiement().toLocalDate() : commission.getCreatedAt().toLocalDate())
                    .build();
            
            AvisCreditBAMDto result = bamIntegrationService.envoyerAvisCredit(avisCredit);
            return result != null && result.isTraite();
        } catch (Exception e) {
            log.error("Error sending commission to BAM: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendCommissionToComptabilite(Commission commission) {
        try {
            List<EcritureComptableDto.LigneComptableDto> lignes = new ArrayList<>();
            lignes.add(EcritureComptableDto.LigneComptableDto.builder()
                    .compte("661200").libelleCompte("Commission " + commission.getTypeCommission().getDescription())
                    .montantDebit(commission.getMontant()).build());
            lignes.add(EcritureComptableDto.LigneComptableDto.builder()
                    .compte("512100").libelleCompte("Décaissement commission")
                    .montantCredit(commission.getMontant()).build());
            
            EcritureComptableDto ecriture = EcritureComptableDto.builder()
                    .numeroEcriture("ECR-COM-" + commission.getNumeroReference())
                    .dateEcriture(commission.getCreatedAt().toLocalDate())
                    .libelleEcriture("Commission " + commission.getTypeCommission().getDescription())
                    .montantTotal(commission.getMontant())
                    .lignesComptables(lignes)
                    .build();
            
            EcritureComptableDto result = comptabiliteIntegrationService.envoyerEcritureComptable(ecriture);
            return result != null && "TRAITE".equals(result.getStatutComptabilisation());
        } catch (Exception e) {
            log.error("Error sending commission to Comptabilite: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendCommissionToDTFE(Commission commission) {
        try {
            OperationTresorDto operation = OperationTresorDto.builder()
                    .numeroOperation("OP-COM-" + commission.getNumeroReference())
                    .typeOperation("COMMISSION")
                    .dateOperation(commission.getCreatedAt().toLocalDate())
                    .montantOperation(commission.getMontant())
                    .devise("MAD")
                    .description("Commission " + commission.getTypeCommission().getDescription())
                    .build();
            
            OperationTresorDto result = dtfeIntegrationService.envoyerOperationTresor(operation);
            return result != null && "VALIDEE".equals(result.getStatutValidation());
        } catch (Exception e) {
            log.error("Error sending commission to DTFE: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Processing result DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingResult {
        private String numeroOperation;
        private String typeOperation;
        private LocalDateTime dateTraitement;
        private boolean success;
        private List<String> messages;
    }
    
    /**
     * System health result DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthResult {
        private LocalDateTime dateVerification;
        private boolean bamConnected;
        private boolean comptabiliteConnected;
        private boolean dtfeConnected;
        private String bamStatus;
        private String comptabiliteStatus;
        private String dtfeStatus;
        
        public boolean isOverallHealthy() {
            return bamConnected && comptabiliteConnected && dtfeConnected;
        }
    }
}