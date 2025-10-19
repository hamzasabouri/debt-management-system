package com.microservices.detteinterieur.integration.dtfe;

import com.microservices.detteinterieur.dto.dtfe.OperationTresorDto;
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
 * Implementation of DTFE integration service
 * Handles HTTP communication with treasury systems
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DTFEIntegrationServiceImpl implements DTFEIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${integration.dtfe.base-url}")
    private String dtfeBaseUrl;
    
    @Value("${integration.dtfe.api-key}")
    private String dtfeApiKey;
    
    @Value("${integration.dtfe.timeout:30000}")
    private int dtfeTimeout;
    
    @Override
    public OperationTresorDto envoyerOperationTresor(OperationTresorDto operationDto) {
        log.info("Sending treasury operation to DTFE: {}", operationDto.getNumeroOperation());
        
        try {
            // Validate before sending
            if (!operationDto.isValid()) {
                throw new IllegalArgumentException("Invalid treasury operation data");
            }
            
            // Set default values
            // operationDto.setDateSoumission(LocalDateTime.now());
            // operationDto.setStatutValidation("EN_ATTENTE");
            
            HttpHeaders headers = createHeaders();
            HttpEntity<OperationTresorDto> request = new HttpEntity<>(operationDto, headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor";
            
            ResponseEntity<OperationTresorDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                OperationTresorDto.class
            );
            
            OperationTresorDto result = response.getBody();
            if (result != null) {
                // result.setDateTraitement(LocalDateTime.now());
                log.info("Treasury operation sent successfully: {}", result.getNumeroOperation());
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error sending treasury operation: {}", e.getMessage());
            return handleOperationError(operationDto, "CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error sending treasury operation: {}", e.getMessage());
            return handleOperationError(operationDto, "SERVER_ERROR", e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Connection error sending treasury operation: {}", e.getMessage());
            return handleOperationError(operationDto, "CONNECTION_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending treasury operation: {}", e.getMessage());
            return handleOperationError(operationDto, "UNKNOWN_ERROR", e.getMessage());
        }
    }
    
    @Override
    public Optional<OperationTresorDto> verifierStatutOperation(String numeroOperation) {
        log.info("Checking treasury operation status: {}", numeroOperation);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/" + numeroOperation + "/status";
            
            ResponseEntity<OperationTresorDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto.class
            );
            
            return Optional.ofNullable(response.getBody());
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Treasury operation not found: {}", numeroOperation);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error checking treasury operation status: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<OperationTresorDto> getOperationsParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting treasury operations for period: {} to {}", dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/operations-tresor?dateDebut=%s&dateFin=%s", 
                    dtfeBaseUrl, dateDebut, dateFin);
            
            ResponseEntity<OperationTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto[].class
            );
            
            OperationTresorDto[] operations = response.getBody();
            return operations != null ? List.of(operations) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting treasury operations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<OperationTresorDto> getOperationsParType(String typeOperation, LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting treasury operations by type {} for period: {} to {}", typeOperation, dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/operations-tresor/type/%s?dateDebut=%s&dateFin=%s", 
                    dtfeBaseUrl, typeOperation, dateDebut, dateFin);
            
            ResponseEntity<OperationTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto[].class
            );
            
            OperationTresorDto[] operations = response.getBody();
            return operations != null ? List.of(operations) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting treasury operations by type: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<OperationTresorDto> getOperationsEnAttente() {
        log.info("Getting pending treasury operations");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/pending";
            
            ResponseEntity<OperationTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto[].class
            );
            
            OperationTresorDto[] operations = response.getBody();
            return operations != null ? List.of(operations) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting pending treasury operations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<OperationTresorDto> getOperationsValidees() {
        log.info("Getting validated treasury operations");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/validated";
            
            ResponseEntity<OperationTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto[].class
            );
            
            OperationTresorDto[] operations = response.getBody();
            return operations != null ? List.of(operations) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting validated treasury operations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<OperationTresorDto> getOperationsREJETEs() {
        log.info("Getting rejected treasury operations");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/rejected";
            
            ResponseEntity<OperationTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                OperationTresorDto[].class
            );
            
            OperationTresorDto[] operations = response.getBody();
            return operations != null ? List.of(operations) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting rejected treasury operations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public OperationTresorDto demanderValidation(String numeroOperation, String observations) {
        log.info("Requesting validation for treasury operation: {}", numeroOperation);
        
        try {
            HttpHeaders headers = createHeaders();
            
            class ValidationRequest {
                public final String numeroOperation;
                public final String observations;
                public final LocalDateTime dateDemandeValidation;
                
                public ValidationRequest(String numeroOperation, String observations) {
                    this.numeroOperation = numeroOperation;
                    this.observations = observations;
                    this.dateDemandeValidation = LocalDateTime.now();
                }
            }
            
            ValidationRequest validationRequest = new ValidationRequest(numeroOperation, observations);
            
            HttpEntity<Object> request = new HttpEntity<>(validationRequest, headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/" + numeroOperation + "/validate";
            
            ResponseEntity<OperationTresorDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                OperationTresorDto.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error requesting validation for treasury operation: {}", e.getMessage());
            return OperationTresorDto.builder()
                    .numeroOperation(numeroOperation)
                    .statutValidation("REJETE")
                    // .codeErreur("VALIDATION_REQUEST_FAILED")
                    // .messageErreur(e.getMessage())
                    // .dateTraitement(LocalDateTime.now())
                    .build();
        }
    }
    
    @Override
    public boolean annulerOperation(String numeroOperation, String motifAnnulation) {
        log.info("Cancelling treasury operation: {} with reason: {}", numeroOperation, motifAnnulation);
        
        try {
            HttpHeaders headers = createHeaders();
            
            class CancellationRequest {
                public final String numeroOperation;
                public final String motifAnnulation;
                public final LocalDateTime dateAnnulation;
                
                public CancellationRequest(String numeroOperation, String motifAnnulation) {
                    this.numeroOperation = numeroOperation;
                    this.motifAnnulation = motifAnnulation;
                    this.dateAnnulation = LocalDateTime.now();
                }
            }
            
            CancellationRequest cancellationRequest = new CancellationRequest(numeroOperation, motifAnnulation);
            
            HttpEntity<Object> request = new HttpEntity<>(cancellationRequest, headers);
            
            String endpoint = dtfeBaseUrl + "/api/operations-tresor/" + numeroOperation + "/cancel";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.DELETE, 
                request, 
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Treasury operation cancellation: {} - Success: {}", numeroOperation, success);
            return success;
            
        } catch (Exception e) {
            log.error("Error cancelling treasury operation: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public PortefeuilleDetteDto getPortefeuilleDetteInterieure(LocalDate dateReference) {
        log.info("Getting debt portfolio for date: {}", dateReference);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/portefeuille/dette-interieure?dateReference=" + dateReference;
            
            ResponseEntity<PortefeuilleDetteDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                PortefeuilleDetteDto.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting debt portfolio: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<SoldeTresorDto> getSoldesCompteTresor(LocalDate dateReference) {
        log.info("Getting treasury account balances for date: {}", dateReference);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/soldes-tresor?dateReference=" + dateReference;
            
            ResponseEntity<SoldeTresorDto[]> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                SoldeTresorDto[].class
            );
            
            SoldeTresorDto[] balances = response.getBody();
            return balances != null ? List.of(balances) : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error getting treasury account balances: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean soumettreRapportMensuel(int mois, int annee, RapportDetteDto rapportDto) {
        log.info("Submitting monthly debt report for {}/{}", mois, annee);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<RapportDetteDto> request = new HttpEntity<>(rapportDto, headers);
            
            String endpoint = String.format("%s/api/rapports/mensuel/%d/%d", dtfeBaseUrl, mois, annee);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Monthly debt report submission: {}/{} - Success: {}", mois, annee, success);
            return success;
            
        } catch (Exception e) {
            log.error("Error submitting monthly debt report: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public StatistiquesDetteDto getStatistiquesDetteInterieure(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Getting debt statistics for period: {} to {}", dateDebut, dateFin);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = String.format("%s/api/statistiques/dette-interieure?dateDebut=%s&dateFin=%s", 
                    dtfeBaseUrl, dateDebut, dateFin);
            
            ResponseEntity<StatistiquesDetteDto> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                StatistiquesDetteDto.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting debt statistics: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean testerConnexion() {
        log.info("Testing connection to DTFE system");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/health";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            boolean connected = response.getStatusCode().is2xxSuccessful();
            log.info("DTFE connection test result: {}", connected);
            return connected;
            
        } catch (Exception e) {
            log.error("DTFE connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getStatutSystemeDTFE() {
        log.info("Getting DTFE system status");
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            String endpoint = dtfeBaseUrl + "/api/status";
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting DTFE system status: {}", e.getMessage());
            return "UNKNOWN - Error: " + e.getMessage();
        }
    }
    
    /**
     * Create HTTP headers with authentication and content type
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", dtfeApiKey);
        headers.set("User-Agent", "DetteInterieurService/1.0.0");
        return headers;
    }
    
    /**
     * Handle treasury operation errors
     */
    private OperationTresorDto handleOperationError(OperationTresorDto operationDto, String errorCode, String errorMessage) {
        operationDto.setStatutValidation("REJETE");
        // operationDto.setCodeErreur(errorCode);
        // operationDto.setMessageErreur(errorMessage);
        // operationDto.setDateTraitement(LocalDateTime.now());
        return operationDto;
    }
}