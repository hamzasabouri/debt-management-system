package com.microservices.dettetresor.service.integration;

import com.microservices.dettetresor.dto.integration.DtfePaymentDto;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.repository.OrdrePaiementRepository;
import com.microservices.dettetresor.repository.PretRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DtfeIntegrationService {
    
    private final RestTemplate restTemplate;
    private final OrdrePaiementRepository ordrePaiementRepository;
    private final PretRepository pretRepository;
    
    @Value("${integration.dtfe.base-url:http://dtfe-api.finances.gov.ma/api/v1}")
    private String dtfeBaseUrl;
    
    @Value("${integration.dtfe.api-key:#{null}}")
    private String dtfeApiKey;
    
    @Value("${integration.dtfe.timeout:30000}")
    private int timeoutMs;
    
    /**
     * Send payment order to DTFE system
     */
    public void sendPaymentOrderToDtfe(OrdrePaiement ordrePaiement) {
        log.info("Sending payment order to DTFE: {}", ordrePaiement.getNumeroOrdre());
        
        try {
            DtfePaymentDto paymentDto = convertToPaymentDto(ordrePaiement);
            String url = dtfeBaseUrl + "/payments";
            HttpHeaders headers = createDtfeHeaders();
            HttpEntity<DtfePaymentDto> entity = new HttpEntity<>(paymentDto, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || 
                response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully sent payment order {} to DTFE", ordrePaiement.getNumeroOrdre());
                updatePaymentOrderStatus(ordrePaiement, OrdrePaiement.StatutOrdrePaiement.PRIS_EN_CHARGE);
            } else {
                log.warn("DTFE returned unexpected status for payment order {}: {}", 
                    ordrePaiement.getNumeroOrdre(), response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error sending payment order {} to DTFE: {}", 
                ordrePaiement.getNumeroOrdre(), e.getMessage());
            throw new RuntimeException("DTFE communication error", e);
        }
    }
    
    /**
     * Check payment status in DTFE system
     */
    public String checkPaymentStatus(String paymentOrderNumber) {
        log.debug("Checking payment status in DTFE for order: {}", paymentOrderNumber);
        
        try {
            String url = dtfeBaseUrl + "/payments/" + paymentOrderNumber + "/status";
            HttpHeaders headers = createDtfeHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String status = response.getBody();
                log.debug("Payment status for order {}: {}", paymentOrderNumber, status);
                return status;
            } else {
                log.warn("DTFE returned non-OK status for payment check {}: {}", 
                    paymentOrderNumber, response.getStatusCode());
                return "UNKNOWN";
            }
        } catch (RestClientException e) {
            log.error("Error checking payment status in DTFE for order {}: {}", 
                paymentOrderNumber, e.getMessage());
            return "ERROR";
        }
    }
    
    /**
     * Fetch payment confirmation from DTFE
     */
    public DtfePaymentDto fetchPaymentConfirmation(String paymentOrderNumber) {
        log.debug("Fetching payment confirmation from DTFE for order: {}", paymentOrderNumber);
        
        try {
            String url = dtfeBaseUrl + "/payments/" + paymentOrderNumber;
            HttpHeaders headers = createDtfeHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<DtfePaymentDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, DtfePaymentDto.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Successfully fetched payment confirmation from DTFE for order: {}", 
                    paymentOrderNumber);
                return response.getBody();
            } else {
                log.warn("DTFE returned non-OK status for payment confirmation {}: {}", 
                    paymentOrderNumber, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error fetching payment confirmation from DTFE for order {}: {}", 
                paymentOrderNumber, e.getMessage());
            throw new RuntimeException("DTFE communication error", e);
        }
    }
    
    /**
     * Synchronize payment statuses with DTFE
     */
    public void synchronizePaymentStatuses() {
        log.info("Starting payment status synchronization with DTFE");
        
        try {
            List<OrdrePaiement> pendingOrders = ordrePaiementRepository
                .findByStatutIn(List.of(OrdrePaiement.StatutOrdrePaiement.PRIS_EN_CHARGE, 
                                       OrdrePaiement.StatutOrdrePaiement.EN_ATTENTE));
            
            for (OrdrePaiement ordre : pendingOrders) {
                String status = checkPaymentStatus(ordre.getNumeroOrdre());
                if (!status.equals("UNKNOWN") && !status.equals("ERROR")) {
                    updatePaymentOrderStatus(ordre, mapDtfeStatusToLocal(status));
                }
                
                // Small delay to avoid overwhelming the DTFE system
                Thread.sleep(100);
            }
            
            log.info("Completed payment status synchronization with DTFE. Processed {} orders", 
                pendingOrders.size());
        } catch (Exception e) {
            log.error("Error during payment status synchronization with DTFE: {}", e.getMessage());
            throw new RuntimeException("DTFE synchronization failed", e);
        }
    }
    
    /**
     * Get treasury balance from DTFE
     */
    public String getTreasuryBalance(String accountNumber) {
        log.debug("Fetching treasury balance from DTFE for account: {}", accountNumber);
        
        try {
            String url = dtfeBaseUrl + "/treasury/balance/" + accountNumber;
            HttpHeaders headers = createDtfeHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Successfully fetched treasury balance from DTFE for account: {}", 
                    accountNumber);
                return response.getBody();
            } else {
                log.warn("DTFE returned non-OK status for balance check {}: {}", 
                    accountNumber, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error fetching treasury balance from DTFE for account {}: {}", 
                accountNumber, e.getMessage());
            throw new RuntimeException("DTFE communication error", e);
        }
    }
    
    /**
     * Send budget notification to DTFE
     */
    public void sendBudgetNotification(String loanReference, String budgetLine, String amount) {
        log.info("Sending budget notification to DTFE for loan: {}", loanReference);
        
        try {
            String url = dtfeBaseUrl + "/budget/notification";
            HttpHeaders headers = createDtfeHeaders();
            
            String requestBody = String.format(
                "{\"loan_reference\":\"%s\",\"budget_line\":\"%s\",\"amount\":\"%s\"}", 
                loanReference, budgetLine, amount);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK || 
                response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Successfully sent budget notification to DTFE for loan: {}", loanReference);
            } else {
                log.warn("DTFE returned unexpected status for budget notification {}: {}", 
                    loanReference, response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error sending budget notification to DTFE for loan {}: {}", 
                loanReference, e.getMessage());
            throw new RuntimeException("DTFE communication error", e);
        }
    }
    
    /**
     * Convert OrdrePaiement to DTFE payment DTO
     */
    private DtfePaymentDto convertToPaymentDto(OrdrePaiement ordrePaiement) {
        return DtfePaymentDto.builder()
            .paymentOrderNumber(ordrePaiement.getNumeroOrdre())
            .loanReference(ordrePaiement.getPret().getNumeroPret())
            .paymentDate(ordrePaiement.getDateEmission())
            .amount(ordrePaiement.getMontant())
            .currency(ordrePaiement.getDevise())
            .paymentType("PAYMENT_ORDER")
            .beneficiary("Treasury")
            .status(ordrePaiement.getStatut().name())
            .treasuryAccount("MAIN_TREASURY")
            .referenceDocument(ordrePaiement.getNumeroOrdre())
            .build();
    }
    
    /**
     * Update payment order status
     */
    private void updatePaymentOrderStatus(OrdrePaiement ordrePaiement, OrdrePaiement.StatutOrdrePaiement newStatus) {
        ordrePaiement.setStatut(newStatus);
        ordrePaiementRepository.save(ordrePaiement);
        log.debug("Updated payment order {} status to: {}", 
            ordrePaiement.getNumeroOrdre(), newStatus);
    }
    
    /**
     * Map DTFE status to local enum
     */
    private OrdrePaiement.StatutOrdrePaiement mapDtfeStatusToLocal(String dtfeStatus) {
        return switch (dtfeStatus.toUpperCase()) {
            case "SENT_TO_DTFE", "PROCESSING" -> OrdrePaiement.StatutOrdrePaiement.PRIS_EN_CHARGE;
            case "EXECUTED", "PAID" -> OrdrePaiement.StatutOrdrePaiement.PAYE;
            case "CANCELLED" -> OrdrePaiement.StatutOrdrePaiement.ANNULE;
            default -> OrdrePaiement.StatutOrdrePaiement.EN_ATTENTE;
        };
    }
    
    /**
     * Create HTTP headers for DTFE requests
     */
    private HttpHeaders createDtfeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        
        if (dtfeApiKey != null && !dtfeApiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + dtfeApiKey);
        }
        
        headers.set("User-Agent", "Dette-Tresor-Service/1.0");
        return headers;
    }
    
    /**
     * Test DTFE connectivity
     */
    public boolean testDtfeConnectivity() {
        try {
            String url = dtfeBaseUrl + "/health";
            HttpHeaders headers = createDtfeHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            boolean isConnected = response.getStatusCode() == HttpStatus.OK;
            log.info("DTFE connectivity test: {}", isConnected ? "SUCCESS" : "FAILED");
            return isConnected;
        } catch (Exception e) {
            log.error("DTFE connectivity test failed: {}", e.getMessage());
            return false;
        }
    }
}