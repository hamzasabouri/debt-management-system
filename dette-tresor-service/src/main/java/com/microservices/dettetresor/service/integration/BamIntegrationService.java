package com.microservices.dettetresor.service.integration;

import com.microservices.dettetresor.dto.integration.BamLoanDto;
import com.microservices.dettetresor.entity.Pret;
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
public class BamIntegrationService {
    
    private final RestTemplate restTemplate;
    private final PretRepository pretRepository;
    
    @Value("${integration.bam.base-url:http://bam-api.gov.ma/api/v1}")
    private String bamBaseUrl;
    
    @Value("${integration.bam.api-key:#{null}}")
    private String bamApiKey;
    
    @Value("${integration.bam.timeout:30000}")
    private int timeoutMs;
    
    /**
     * Synchronize loan data with BAM system
     */
    public void synchronizeLoanData(String loanNumber) {
        log.info("Starting loan synchronization with BAM for loan: {}", loanNumber);
        
        try {
            BamLoanDto bamLoan = fetchLoanFromBam(loanNumber);
            if (bamLoan != null) {
                updateLocalLoanFromBam(bamLoan);
                log.info("Successfully synchronized loan {} with BAM", loanNumber);
            }
        } catch (Exception e) {
            log.error("Failed to synchronize loan {} with BAM: {}", loanNumber, e.getMessage());
            throw new RuntimeException("BAM synchronization failed", e);
        }
    }
    
    /**
     * Fetch loan data from BAM system
     */
    public BamLoanDto fetchLoanFromBam(String loanNumber) {
        log.debug("Fetching loan data from BAM for loan: {}", loanNumber);
        
        try {
            String url = bamBaseUrl + "/loans/" + loanNumber;
            HttpHeaders headers = createBamHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BamLoanDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, BamLoanDto.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Successfully fetched loan data from BAM for loan: {}", loanNumber);
                return response.getBody();
            } else {
                log.warn("BAM returned non-OK status for loan {}: {}", loanNumber, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Error communicating with BAM for loan {}: {}", loanNumber, e.getMessage());
            throw new RuntimeException("BAM communication error", e);
        }
    }
    
    /**
     * Send loan data to BAM system
     */
    public void sendLoanToBam(Pret pret) {
        log.info("Sending loan data to BAM for loan: {}", pret.getNumeroPret());
        
        try {
            BamLoanDto bamLoan = convertToBAMDto(pret);
            String url = bamBaseUrl + "/loans";
            HttpHeaders headers = createBamHeaders();
            HttpEntity<BamLoanDto> entity = new HttpEntity<>(bamLoan, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || 
                response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully sent loan {} to BAM", pret.getNumeroPret());
            } else {
                log.warn("BAM returned unexpected status for loan {}: {}", 
                    pret.getNumeroPret(), response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error sending loan {} to BAM: {}", pret.getNumeroPret(), e.getMessage());
            throw new RuntimeException("BAM communication error", e);
        }
    }
    
    /**
     * Get all loans from BAM for a specific period
     */
    public List<BamLoanDto> fetchAllLoansFromBam(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching all loans from BAM from {} to {}", fromDate, toDate);
        
        try {
            String url = bamBaseUrl + "/loans?from=" + fromDate + "&to=" + toDate;
            HttpHeaders headers = createBamHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BamLoanDto[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, BamLoanDto[].class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} loans from BAM", response.getBody().length);
                return List.of(response.getBody());
            }
            
            return List.of();
        } catch (RestClientException e) {
            log.error("Error fetching loans from BAM: {}", e.getMessage());
            throw new RuntimeException("BAM communication error", e);
        }
    }
    
    /**
     * Update local loan data from BAM data
     */
    private void updateLocalLoanFromBam(BamLoanDto bamLoan) {
        Optional<Pret> pretOpt = pretRepository.findByNumeroPret(bamLoan.getLoanNumber());
        
        if (pretOpt.isPresent()) {
            Pret pret = pretOpt.get();
            pret.setSoldeCourant(bamLoan.getCurrentBalance());
            // Update other fields as needed
            pretRepository.save(pret);
            log.debug("Updated local loan {} with BAM data", bamLoan.getLoanNumber());
        } else {
            log.warn("Local loan {} not found for BAM update", bamLoan.getLoanNumber());
        }
    }
    
    /**
     * Convert Pret entity to BAM DTO
     */
    private BamLoanDto convertToBAMDto(Pret pret) {
        return BamLoanDto.builder()
            .loanNumber(pret.getNumeroPret())
            .signatureDate(pret.getDateSignature())
            .lenderOrganization(pret.getOrganismeBailleur())
            .loanObject(pret.getObjet())
            .totalAmount(pret.getMontantTotal())
            .currency(pret.getDevise())
            .durationMonths(pret.getDuree())
            .interestRate(pret.getTauxInteret())
            .currentBalance(pret.getSoldeCourant())
            .status("ACTIVE")
            .lastUpdated(pret.getDateSignature())
            .build();
    }
    
    /**
     * Create HTTP headers for BAM requests
     */
    private HttpHeaders createBamHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        
        if (bamApiKey != null && !bamApiKey.isEmpty()) {
            headers.set("X-API-Key", bamApiKey);
        }
        
        headers.set("User-Agent", "Dette-Tresor-Service/1.0");
        return headers;
    }
    
    /**
     * Test BAM connectivity
     */
    public boolean testBamConnectivity() {
        try {
            String url = bamBaseUrl + "/health";
            HttpHeaders headers = createBamHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            boolean isConnected = response.getStatusCode() == HttpStatus.OK;
            log.info("BAM connectivity test: {}", isConnected ? "SUCCESS" : "FAILED");
            return isConnected;
        } catch (Exception e) {
            log.error("BAM connectivity test failed: {}", e.getMessage());
            return false;
        }
    }
}