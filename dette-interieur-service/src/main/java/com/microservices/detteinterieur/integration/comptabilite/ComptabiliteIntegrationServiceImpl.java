package com.microservices.detteinterieur.integration.comptabilite;

import com.microservices.detteinterieur.dto.comptabilite.EcritureComptableDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of Comptabilite integration service
 * Handles HTTP communication with accounting system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComptabiliteIntegrationServiceImpl implements ComptabiliteIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${integration.comptabilite.base-url}")
    private String comptabiliteBaseUrl;
    
    @Value("${integration.comptabilite.api-key}")
    private String comptabiliteApiKey;
    
    @Value("${integration.comptabilite.timeout:30000}")
    private int comptabiliteTimeout;
    
    @Override
    public EcritureComptableDto envoyerEcritureComptable(EcritureComptableDto ecritureDto) {
        log.info("Sending accounting entry to Comptabilite: {}", ecritureDto.getNumeroEcriture());
        
        try {
            // Validate before sending
            ValidationResult validation = validerEcriture(ecritureDto);
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Invalid accounting entry: " + validation.getErrors());
            }
            
            // Set default values
            ecritureDto.setDateSaisie(LocalDateTime.now());
            ecritureDto.setStatutComptabilisation("EN_ATTENTE");
            
            HttpHeaders headers = createHeaders();
            HttpEntity<EcritureComptableDto> request = new HttpEntity<>(ecritureDto, headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures";
            
            ResponseEntity<EcritureComptableDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                EcritureComptableDto.class
            );
            
            EcritureComptableDto result = response.getBody();
            if (result != null) {
                result.setDateComptabilisation(LocalDateTime.now());
                log.info("Accounting entry sent successfully: {}", result.getNumeroEcriture());
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error sending accounting entry: {}", e.getMessage());
            return handleAccountingEntryError(ecritureDto, "CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error sending accounting entry: {}", e.getMessage());
            return handleAccountingEntryError(ecritureDto, "SERVER_ERROR", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Connection error sending accounting entry: {}", e.getMessage());
            return handleAccountingEntryError(ecritureDto, "CONNECTION_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending accounting entry: {}", e.getMessage());
            return handleAccountingEntryError(ecritureDto, "UNKNOWN_ERROR", e.getMessage());
        }
    }
    
    @Override
    public Optional<EcritureComptableDto> verifierStatutEcriture(String numeroEcriture) {
        log.info("Checking accounting entry status: {}", numeroEcriture);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures/" + numeroEcriture + "/status";
            
            ResponseEntity<EcritureComptableDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                EcritureComptableDto.class
            );
            
            return Optional.ofNullable(response.getBody());
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Accounting entry not found: {}", numeroEcriture);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error checking accounting entry status: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<EcritureComptableDto> getEcrituresParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting accounting entries for period: {} to {}", dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/ecritures?dateDebut=%s&dateFin=%s", 
                    comptabiliteBaseUrl, dateDebut, dateFin);
            
            ResponseEntity<EcritureComptableDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                EcritureComptableDto[].class
            );
            
            EcritureComptableDto[] entries = response.getBody();
            return entries != null ? List.of(entries) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting accounting entries: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EcritureComptableDto> getEcrituresParJournal(String journal, LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting accounting entries for journal {} from {} to {}", journal, dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/ecritures/journal/%s?dateDebut=%s&dateFin=%s", 
                    comptabiliteBaseUrl, journal, dateDebut, dateFin);
            
            ResponseEntity<EcritureComptableDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                EcritureComptableDto[].class
            );
            
            EcritureComptableDto[] entries = response.getBody();
            return entries != null ? List.of(entries) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting accounting entries for journal: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EcritureComptableDto> getEcrituresEnAttente() {
        log.info("Getting pending accounting entries");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures/pending";
            
            ResponseEntity<EcritureComptableDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                EcritureComptableDto[].class
            );
            
            EcritureComptableDto[] entries = response.getBody();
            return entries != null ? List.of(entries) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting pending accounting entries: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EcritureComptableDto> getEcrituresREJETEs() {
        log.info("Getting rejected accounting entries");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures/rejected";
            
            ResponseEntity<EcritureComptableDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                EcritureComptableDto[].class
            );
            
            EcritureComptableDto[] entries = response.getBody();
            return entries != null ? List.of(entries) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting rejected accounting entries: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public EcritureComptableDto retenterEnvoiEcriture(String numeroEcriture) {
        log.info("Retrying accounting entry: {}", numeroEcriture);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures/" + numeroEcriture + "/retry";
            
            ResponseEntity<EcritureComptableDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                EcritureComptableDto.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error retrying accounting entry: {}", e.getMessage());
            return EcritureComptableDto.builder()
                    .numeroEcriture(numeroEcriture)
                    .statutComptabilisation("REJETE")
                    .codeErreur("RETRY_FAILED")
                    .messageErreur(e.getMessage())
                    .dateComptabilisation(LocalDateTime.now())
                    .build();
        }
    }
    
    @Override
    public boolean annulerEcriture(String numeroEcriture, String motifAnnulation) {
        log.info("Cancelling accounting entry: {} with reason: {}", numeroEcriture, motifAnnulation);
        
        try {
            HttpHeaders headers = createHeaders();
            
            class CancellationRequest {
                public final String numeroEcriture;
                public final String motifAnnulation;
                public final LocalDateTime dateAnnulation;
                
                public CancellationRequest(String numeroEcriture, String motifAnnulation) {
                    this.numeroEcriture = numeroEcriture;
                    this.motifAnnulation = motifAnnulation;
                    this.dateAnnulation = LocalDateTime.now();
                }
            }
            
            CancellationRequest cancellationRequest = new CancellationRequest(numeroEcriture, motifAnnulation);
            
            HttpEntity<Object> request = new HttpEntity<>(cancellationRequest, headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/ecritures/" + numeroEcriture + "/cancel";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.DELETE, 
                request, 
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Accounting entry cancellation: {} - Success: {}", numeroEcriture, success);
            return success;
            
        } catch (Exception e) {
            log.error("Error cancelling accounting entry: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public ValidationResult validerEcriture(EcritureComptableDto ecritureDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Basic validation
        if (ecritureDto == null) {
            errors.add("Accounting entry cannot be null");
            return new ValidationResult(false, errors, warnings);
        }
        
        if (!ecritureDto.isValid()) {
            errors.add("Required fields are missing");
        }
        
        if (!ecritureDto.isEquilibree()) {
            errors.add("Accounting entry is not balanced (debits != credits)");
        }
        
        // Validate accounting lines
        if (ecritureDto.getLignesComptables() != null) {
            for (int i = 0; i < ecritureDto.getLignesComptables().size(); i++) {
                EcritureComptableDto.LigneComptableDto ligne = ecritureDto.getLignesComptables().get(i);
                
                if (ligne.getCompte() == null || ligne.getCompte().trim().isEmpty()) {
                    errors.add("Line " + (i + 1) + ": Account is required");
                }
                
                BigDecimal debit = ligne.getMontantDebit() != null ? ligne.getMontantDebit() : BigDecimal.ZERO;
                BigDecimal credit = ligne.getMontantCredit() != null ? ligne.getMontantCredit() : BigDecimal.ZERO;
                
                if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
                    errors.add("Line " + (i + 1) + ": Either debit or credit amount must be specified");
                }
                
                if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {
                    errors.add("Line " + (i + 1) + ": Cannot have both debit and credit amounts");
                }
            }
        }
        
        // Add warnings for best practices
        if (ecritureDto.getCommentaire() == null || ecritureDto.getCommentaire().trim().isEmpty()) {
            warnings.add("Consider adding a comment for better traceability");
        }
        
        if (ecritureDto.getPiecesJustificatives() == null || ecritureDto.getPiecesJustificatives().isEmpty()) {
            warnings.add("Consider adding supporting documents");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    @Override
    public List<CompteComptableDto> getPlanComptable() {
        log.info("Getting chart of accounts from Comptabilite");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/comptes";
            
            ResponseEntity<CompteComptableDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                CompteComptableDto[].class
            );
            
            CompteComptableDto[] accounts = response.getBody();
            return accounts != null ? List.of(accounts) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting chart of accounts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CentreCoutDto> getCentresCout() {
        log.info("Getting cost centers from Comptabilite");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/centres-cout";
            
            ResponseEntity<CentreCoutDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                CentreCoutDto[].class
            );
            
            CentreCoutDto[] costCenters = response.getBody();
            return costCenters != null ? List.of(costCenters) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting cost centers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<JournalDto> getJournauxDisponibles() {
        log.info("Getting available journals from Comptabilite");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/journaux";
            
            ResponseEntity<JournalDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                JournalDto[].class
            );
            
            JournalDto[] journals = response.getBody();
            return journals != null ? List.of(journals) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting available journals: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean testerConnexion() {
        log.info("Testing connection to Comptabilite system");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/health";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            boolean connected = response.getStatusCode().is2xxSuccessful();
            log.info("Comptabilite connection test result: {}", connected);
            return connected;
            
        } catch (Exception e) {
            log.error("Comptabilite connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getStatutSystemeComptabilite() {
        log.info("Getting Comptabilite system status");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = comptabiliteBaseUrl + "/api/status";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting Comptabilite system status: {}", e.getMessage());
            return "UNKNOWN - Error: " + e.getMessage();
        }
    }
    
    /**
     * Create HTTP headers with authentication and content type
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", comptabiliteApiKey);
        headers.set("User-Agent", "DetteInterieurService/1.0.0");
        return headers;
    }
    
    /**
     * Handle accounting entry errors
     */
    private EcritureComptableDto handleAccountingEntryError(EcritureComptableDto ecritureDto, String errorCode, String errorMessage) {
        ecritureDto.setStatutComptabilisation("REJETE");
        ecritureDto.setCodeErreur(errorCode);
        ecritureDto.setMessageErreur(errorMessage);
        ecritureDto.setDateComptabilisation(LocalDateTime.now());
        return ecritureDto;
    }
}