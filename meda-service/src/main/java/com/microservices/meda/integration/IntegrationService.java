package com.microservices.meda.integration;

import com.microservices.meda.entity.Avance;
import com.microservices.meda.entity.PieceJustificative;
import com.microservices.meda.dto.AvanceBAMDTO;
import com.microservices.meda.dto.PieceJustificativeComptabiliteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntegrationService {
    
    private final BAMIntegrationClient bamIntegrationClient;
    private final ComptabiliteIntegrationClient comptabiliteIntegrationClient;
    
    @Autowired
    public IntegrationService(
            @Autowired(required = false) BAMIntegrationClient bamIntegrationClient,
            @Autowired(required = false) ComptabiliteIntegrationClient comptabiliteIntegrationClient) {
        this.bamIntegrationClient = bamIntegrationClient;
        this.comptabiliteIntegrationClient = comptabiliteIntegrationClient;
        
        log.info("Integration Service initialized with BAM integration: {}", (bamIntegrationClient != null));
        log.info("Integration Service initialized with Comptabilite integration: {}", (comptabiliteIntegrationClient != null));
    }
    
    /**
     * Send acknowledgment to BAM for received advance
     */
    @ConditionalOnProperty(name = "meda.integration.bam.enabled", havingValue = "true")
    public void sendAdvanceAcknowledgmentToBAM(Avance avance) {
        if (bamIntegrationClient == null) {
            log.warn("BAM integration is disabled or client not available. Skipping advance acknowledgment for: {}", avance.getNumeroAvance());
            return;
        }
        
        try {
            log.info("Sending advance acknowledgment to BAM for: {}", avance.getNumeroAvance());
            
            AvanceBAMDTO avanceBAM = AvanceBAMDTO.builder()
                    .numeroAvance(avance.getNumeroAvance())
                    .projetId(avance.getProjet().getId())
                    .typeAvance(avance.getTypeAvance().name())
                    .dateReception(avance.getDateReception())
                    .montant(avance.getMontant())
                    .devise(avance.getDevise())
                    .emetteur(avance.getEmetteur())
                    .referenceBAM("ACK-" + avance.getNumeroAvance())
                    .typeOperationBAM("CREDIT_NOTICE_RECEIVED")
                    .build();
            
            bamIntegrationClient.acknowledgeAvanceReception(avanceBAM);
            log.info("Advance acknowledgment sent successfully to BAM");
            
        } catch (Exception e) {
            log.error("Error sending advance acknowledgment to BAM: {}", e.getMessage());
            // In production, you might want to implement retry logic or dead letter queue
        }
    }
    
    /**
     * Send acknowledgment to Comptabilite for received supporting document
     */
    @ConditionalOnProperty(name = "meda.integration.comptabilite.enabled", havingValue = "true")
    public void sendPieceJustificativeAcknowledgmentToComptabilite(PieceJustificative piece) {
        if (comptabiliteIntegrationClient == null) {
            log.warn("Comptabilite integration is disabled or client not available. Skipping document acknowledgment for: {}", piece.getNumeroPiece());
            return;
        }
        
        try {
            log.info("Sending document acknowledgment to Comptabilite for: {}", piece.getNumeroPiece());
            
            PieceJustificativeComptabiliteDTO pieceComptabilite = PieceJustificativeComptabiliteDTO.builder()
                    .numeroPiece(piece.getNumeroPiece())
                    .projetId(piece.getProjet().getId())
                    .typePiece(piece.getTypePiece().name())
                    .dateExecution(piece.getDateExecution())
                    .montant(piece.getMontant())
                    .devise(piece.getDevise())
                    .emetteur(piece.getEmetteur())
                    .referenceComptable("ACK-" + piece.getNumeroPiece())
                    .compteDebite("MEDA_ACCOUNT")
                    .centreBeneficiaire(piece.getProjet().getNomProjet())
                    .build();
            
            comptabiliteIntegrationClient.acknowledgePieceJustificativeReception(pieceComptabilite);
            log.info("Document acknowledgment sent successfully to Comptabilite");
            
        } catch (Exception e) {
            log.error("Error sending document acknowledgment to Comptabilite: {}", e.getMessage());
            // In production, you might want to implement retry logic or dead letter queue
        }
    }
    
    /**
     * Send status update to BAM when advance status changes
     */
    @ConditionalOnProperty(name = "meda.integration.bam.enabled", havingValue = "true")
    public void sendAdvanceStatusUpdateToBAM(Avance avance) {
        if (bamIntegrationClient == null) {
            log.warn("BAM integration is disabled or client not available. Skipping status update for: {}", avance.getNumeroAvance());
            return;
        }
        
        try {
            log.info("Sending advance status update to BAM for: {} - Status: {}", 
                    avance.getNumeroAvance(), avance.getStatut());
            
            // Create status update DTO
            AvanceBAMDTO statusUpdate = AvanceBAMDTO.builder()
                    .numeroAvance(avance.getNumeroAvance())
                    .projetId(avance.getProjet().getId())
                    .typeAvance(avance.getTypeAvance().name())
                    .dateReception(avance.getDateReception())
                    .montant(avance.getMontant())
                    .devise(avance.getDevise())
                    .emetteur(avance.getEmetteur())
                    .referenceBAM("STATUS-" + avance.getNumeroAvance())
                    .typeOperationBAM("STATUS_UPDATE_" + avance.getStatut().name())
                    .build();
            
            // In a real implementation, you would have a different endpoint for status updates
            // bamIntegrationClient.updateAdvanceStatus(statusUpdate);
            log.info("Advance status update sent successfully to BAM");
            
        } catch (Exception e) {
            log.error("Error sending advance status update to BAM: {}", e.getMessage());
        }
    }
    
    /**
     * Send status update to Comptabilite when document status changes
     */
    @ConditionalOnProperty(name = "meda.integration.comptabilite.enabled", havingValue = "true")
    public void sendDocumentStatusUpdateToComptabilite(PieceJustificative piece) {
        if (comptabiliteIntegrationClient == null) {
            log.warn("Comptabilite integration is disabled or client not available. Skipping status update for: {}", piece.getNumeroPiece());
            return;
        }
        
        try {
            log.info("Sending document status update to Comptabilite for: {} - Status: {}", 
                    piece.getNumeroPiece(), piece.getStatut());
            
            // Create status update DTO
            PieceJustificativeComptabiliteDTO statusUpdate = PieceJustificativeComptabiliteDTO.builder()
                    .numeroPiece(piece.getNumeroPiece())
                    .projetId(piece.getProjet().getId())
                    .typePiece(piece.getTypePiece().name())
                    .dateExecution(piece.getDateExecution())
                    .montant(piece.getMontant())
                    .devise(piece.getDevise())
                    .emetteur(piece.getEmetteur())
                    .referenceComptable("STATUS-" + piece.getNumeroPiece())
                    .compteDebite("MEDA_ACCOUNT")
                    .centreBeneficiaire(piece.getProjet().getNomProjet())
                    .build();
            
            // In a real implementation, you would have a different endpoint for status updates
            // comptabiliteIntegrationClient.updateDocumentStatus(statusUpdate);
            log.info("Document status update sent successfully to Comptabilite");
            
        } catch (Exception e) {
            log.error("Error sending document status update to Comptabilite: {}", e.getMessage());
        }
    }
}