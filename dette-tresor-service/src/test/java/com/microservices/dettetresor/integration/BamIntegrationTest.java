package com.microservices.dettetresor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dettetresor.DetteTresorServiceApplication;
import com.microservices.dettetresor.dto.integration.BamLoanDto;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.integration.BamIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le service BAM
 * Utilise MockBean pour simuler les appels externes
 */
@SpringBootTest(
    classes = DetteTresorServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
public class BamIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private BamIntegrationService bamIntegrationService;

    @MockBean
    private RestTemplate restTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void testBamIntegration_Success() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-001";
        BamLoanDto bamLoanDto = createSampleBamLoanDto(loanNumber);
        
        // Mock successful BAM response
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenReturn(new ResponseEntity<>(bamLoanDto, HttpStatus.OK));

        // When
        bamIntegrationService.synchronizeLoanData(loanNumber);

        // Then
        verify(restTemplate, times(1)).exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        );
    }

    @Test
    void testBamIntegration_ApiError() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-002";
        
        // Mock BAM API error
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenThrow(new RuntimeException("BAM API unavailable"));

        // When & Then
        try {
            bamIntegrationService.synchronizeLoanData(loanNumber);
        } catch (RuntimeException e) {
            // Expected exception
            assert e.getMessage().contains("BAM synchronization failed");
        }
    }

    @Test
    void testBamIntegration_Timeout() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-003";
        
        // Mock timeout
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenThrow(new org.springframework.web.client.ResourceAccessException("Connection timeout"));

        // When & Then
        try {
            bamIntegrationService.synchronizeLoanData(loanNumber);
        } catch (RuntimeException e) {
            // Expected exception
            assert e.getMessage().contains("BAM synchronization failed");
        }
    }

    @Test
    void testBamIntegration_InvalidResponse() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-004";
        
        // Mock invalid response
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // When
        bamIntegrationService.synchronizeLoanData(loanNumber);

        // Then - Should handle null response gracefully
        verify(restTemplate, times(1)).exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        );
    }

    @Test
    void testBamIntegration_UpdateExistingLoan() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-005";
        Pret existingPret = createAndSaveSamplePret(loanNumber);
        BamLoanDto bamLoanDto = createSampleBamLoanDto(loanNumber);
        bamLoanDto.setTotalAmount(new BigDecimal("2500000.00")); // Different amount
        
        // Mock successful BAM response
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenReturn(new ResponseEntity<>(bamLoanDto, HttpStatus.OK));

        // When
        bamIntegrationService.synchronizeLoanData(loanNumber);

        // Then
        Pret updatedPret = pretRepository.findByNumeroPret(loanNumber)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Verify the loan was updated with BAM data
        assert updatedPret.getMontantTotal().equals(new BigDecimal("2500000.00"));
    }

    @Test
    void testBamIntegration_CreateNewLoan() throws Exception {
        // Given
        String loanNumber = "BAM-LOAN-006";
        BamLoanDto bamLoanDto = createSampleBamLoanDto(loanNumber);
        
        // Mock successful BAM response
        when(restTemplate.exchange(
            anyString(), 
            eq(org.springframework.http.HttpMethod.GET), 
            any(), 
            eq(BamLoanDto.class)
        )).thenReturn(new ResponseEntity<>(bamLoanDto, HttpStatus.OK));

        // When
        bamIntegrationService.synchronizeLoanData(loanNumber);

        // Then
        Pret newPret = pretRepository.findByNumeroPret(loanNumber)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Verify the loan was created from BAM data
        assert newPret.getNumeroPret().equals(loanNumber);
        assert newPret.getOrganismeBailleur().equals("BAM Bank");
    }

    // Helper methods
    private BamLoanDto createSampleBamLoanDto(String loanNumber) {
        return BamLoanDto.builder()
            .loanNumber(loanNumber)
            .signatureDate(LocalDate.now())
            .lenderOrganization("BAM Bank")
            .loanObject("Infrastructure Project")
            .totalAmount(new BigDecimal("2000000.00"))
            .currentBalance(new BigDecimal("2000000.00"))
            .currency("MAD")
            .durationMonths(60)
            .interestRate(new BigDecimal("4.0"))
            .lastUpdated(LocalDate.now())
            .build();
    }

    private Pret createAndSaveSamplePret(String loanNumber) {
        Pret pret = Pret.builder()
            .numeroPret(loanNumber)
            .dateSignature(LocalDate.now())
            .organismeBailleur("BAM Bank")
            .objet("Infrastructure Project")
            .montantTotal(new BigDecimal("2000000.00"))
            .soldeCourant(new BigDecimal("2000000.00"))
            .devise("MAD")
            .duree(60)
            .tauxInteret(new BigDecimal("4.0"))
            .build();
        return pretRepository.save(pret);
    }
}
