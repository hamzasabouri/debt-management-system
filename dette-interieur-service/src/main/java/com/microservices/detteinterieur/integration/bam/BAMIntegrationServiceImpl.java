package com.microservices.detteinterieur.integration.bam;

import com.microservices.detteinterieur.dto.bam.AvisCreditBAMDto;
import com.microservices.detteinterieur.dto.bam.AvisDebitBAMDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BAM integration service
 * Handles HTTP communication with Bank Al-Maghrib systems
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BAMIntegrationServiceImpl implements BAMIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${integration.bam.base-url}")
    private String bamBaseUrl;
    
    @Value("${integration.bam.api-key}")
    private String bamApiKey;
    
    @Value("${integration.bam.timeout:30000}")
    private int bamTimeout;
    
    @Value("${integration.bam.retry-attempts:3}")
    private int retryAttempts;
    
    @Override
    public AvisCreditBAMDto envoyerAvisCredit(AvisCreditBAMDto avisCreditDto) {
        log.info("Sending credit notice to BAM: {}", avisCreditDto.getNumeroAvis());
        
        try {
            // Validate DTO before sending
            if (!avisCreditDto.isValid()) {
                throw new IllegalArgumentException("Invalid credit notice data");
            }
            
            // Set default values
            avisCreditDto.setDateCreation(LocalDateTime.now());
            avisCreditDto.setStatutTraitement("EN_ATTENTE");
            
            HttpHeaders headers = createHeaders();
            HttpEntity<AvisCreditBAMDto> request = new HttpEntity<>(avisCreditDto, headers);
            
            String endpoint = bamBaseUrl + "/api/avis-credit";
            
            ResponseEntity<AvisCreditBAMDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                AvisCreditBAMDto.class
            );
            
            AvisCreditBAMDto result = response.getBody();
            if (result != null) {
                result.setDateTraitement(LocalDateTime.now());
                log.info("Credit notice sent successfully to BAM: {}", result.getNumeroAvis());
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error sending credit notice to BAM: {}", e.getMessage());
            return handleCreditNoticeError(avisCreditDto, "CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error sending credit notice to BAM: {}", e.getMessage());
            return handleCreditNoticeError(avisCreditDto, "SERVER_ERROR", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Connection error sending credit notice to BAM: {}", e.getMessage());
            return handleCreditNoticeError(avisCreditDto, "CONNECTION_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending credit notice to BAM: {}", e.getMessage());
            return handleCreditNoticeError(avisCreditDto, "UNKNOWN_ERROR", e.getMessage());
        }
    }
    
    @Override
    public AvisDebitBAMDto recevoirAvisDebit(AvisDebitBAMDto avisDebitDto) {
        log.info("Processing debit notice received from BAM: {}", avisDebitDto.getNumeroAvis());
        
        try {
            // Validate received data
            if (!avisDebitDto.isValid()) {
                throw new IllegalArgumentException("Invalid debit notice data received from BAM");
            }
            
            // Set reception timestamp
            avisDebitDto.setDateReception(LocalDateTime.now());
            avisDebitDto.setStatutTraitement("RECU");
            
            // Process the debit notice internally
            // This would typically involve creating internal records, updating balances, etc.
            
            avisDebitDto.setDateTraitement(LocalDateTime.now());
            avisDebitDto.setStatutTraitement("TRAITE");
            
            log.info("Debit notice processed successfully: {}", avisDebitDto.getNumeroAvis());
            return avisDebitDto;
            
        } catch (Exception e) {
            log.error("Error processing debit notice from BAM: {}", e.getMessage());
            avisDebitDto.setStatutTraitement("REJETE");
            avisDebitDto.setCodeErreur("PROCESSING_ERROR");
            avisDebitDto.setMessageErreur(e.getMessage());
            avisDebitDto.setDateTraitement(LocalDateTime.now());
            return avisDebitDto;
        }
    }
    
    @Override
    public Optional<AvisCreditBAMDto> verifierStatutAvisCredit(String numeroAvis) {
        log.info("Checking credit notice status at BAM: {}", numeroAvis);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/avis-credit/" + numeroAvis + "/status";
            
            ResponseEntity<AvisCreditBAMDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                AvisCreditBAMDto.class
            );
            
            return Optional.ofNullable(response.getBody());
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Credit notice not found at BAM: {}", numeroAvis);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error checking credit notice status at BAM: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<AvisCreditBAMDto> getAvisCreditParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting credit notices from BAM for period: {} to {}", dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/avis-credit?dateDebut=%s&dateFin=%s", 
                    bamBaseUrl, dateDebut, dateFin);
            
            ResponseEntity<AvisCreditBAMDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                AvisCreditBAMDto[].class
            );
            
            AvisCreditBAMDto[] notices = response.getBody();
            return notices != null ? List.of(notices) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting credit notices from BAM: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<AvisDebitBAMDto> getAvisDebitParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting debit notices from BAM for period: {} to {}", dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/avis-debit?dateDebut=%s&dateFin=%s", 
                    bamBaseUrl, dateDebut, dateFin);
            
            ResponseEntity<AvisDebitBAMDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                AvisDebitBAMDto[].class
            );
            
            AvisDebitBAMDto[] notices = response.getBody();
            return notices != null ? List.of(notices) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting debit notices from BAM: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<AvisCreditBAMDto> getAvisCreditEnAttente() {
        log.info("Getting pending credit notices from BAM");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/avis-credit/pending";
            
            ResponseEntity<AvisCreditBAMDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                AvisCreditBAMDto[].class
            );
            
            AvisCreditBAMDto[] notices = response.getBody();
            return notices != null ? List.of(notices) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting pending credit notices from BAM: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<AvisDebitBAMDto> getAvisDebitNonTraites() {
        log.info("Getting unprocessed debit notices from BAM");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/avis-debit/unprocessed";
            
            ResponseEntity<AvisDebitBAMDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                AvisDebitBAMDto[].class
            );
            
            AvisDebitBAMDto[] notices = response.getBody();
            return notices != null ? List.of(notices) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting unprocessed debit notices from BAM: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public AvisCreditBAMDto retenterEnvoiAvisCredit(String numeroAvis) {
        log.info("Retrying credit notice send to BAM: {}", numeroAvis);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/avis-credit/" + numeroAvis + "/retry";
            
            ResponseEntity<AvisCreditBAMDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                AvisCreditBAMDto.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error retrying credit notice send to BAM: {}", e.getMessage());
            // Return error status
            return AvisCreditBAMDto.builder()
                    .numeroAvis(numeroAvis)
                    .statutTraitement("REJETE")
                    .codeErreur("RETRY_FAILED")
                    .messageErreur(e.getMessage())
                    .dateTraitement(LocalDateTime.now())
                    .build();
        }
    }
    
    @Override
    public boolean annulerAvisCredit(String numeroAvis, String motifAnnulation) {
        log.info("Cancelling credit notice at BAM: {} with reason: {}", numeroAvis, motifAnnulation);
        
        try {
            HttpHeaders headers = createHeaders();
            
            // Create cancellation request
            class CancellationRequest {
                public final String numeroAvis;
                public final String motifAnnulation;
                public final LocalDateTime dateAnnulation;
                
                public CancellationRequest(String numeroAvis, String motifAnnulation) {
                    this.numeroAvis = numeroAvis;
                    this.motifAnnulation = motifAnnulation;
                    this.dateAnnulation = LocalDateTime.now();
                }
            }
            
            CancellationRequest cancellationRequest = new CancellationRequest(numeroAvis, motifAnnulation);
            
            HttpEntity<Object> request = new HttpEntity<>(cancellationRequest, headers);
            
            String endpoint = bamBaseUrl + "/api/avis-credit/" + numeroAvis + "/cancel";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.DELETE, 
                request, 
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Credit notice cancellation at BAM: {} - Success: {}", numeroAvis, success);
            return success;
            
        } catch (Exception e) {
            log.error("Error cancelling credit notice at BAM: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean testerConnexion() {
        log.info("Testing connection to BAM system");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/health";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            boolean connected = response.getStatusCode().is2xxSuccessful();
            log.info("BAM connection test result: {}", connected);
            return connected;
            
        } catch (Exception e) {
            log.error("BAM connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getStatutSystemeBAM() {
        log.info("Getting BAM system status");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = bamBaseUrl + "/api/status";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting BAM system status: {}", e.getMessage());
            return "UNKNOWN - Error: " + e.getMessage();
        }
    }
    
    /**
     * Create HTTP headers with authentication and content type
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", bamApiKey);
        headers.set("User-Agent", "DetteInterieurService/1.0.0");
        return headers;
    }
    
    /**
     * Handle credit notice errors
     */
    private AvisCreditBAMDto handleCreditNoticeError(AvisCreditBAMDto avisCreditDto, String errorCode, String errorMessage) {
        avisCreditDto.setStatutTraitement("REJETE");
        avisCreditDto.setCodeErreur(errorCode);
        avisCreditDto.setMessageErreur(errorMessage);
        avisCreditDto.setDateTraitement(LocalDateTime.now());
        return avisCreditDto;
    }
}