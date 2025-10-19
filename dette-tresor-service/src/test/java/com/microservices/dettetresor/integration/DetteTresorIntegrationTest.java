package com.microservices.dettetresor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dettetresor.DetteTresorServiceApplication;
import com.microservices.dettetresor.dto.PretDTO;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.repository.PretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration complets pour le service Dette Trésor
 * Ces tests vérifient le comportement end-to-end de l'application
 */
@SpringBootTest(
    classes = DetteTresorServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
public class DetteTresorIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateLoan_Integration() throws Exception {
        // Given
        PretDTO pretDTO = createSamplePretDTO();

        // When & Then
        mockMvc.perform(post("/dette-tresor/prets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pretDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.numeroPret").value(pretDTO.getNumeroPret()))
            .andExpect(jsonPath("$.montantTotal").value(pretDTO.getMontantTotal()))
            .andExpect(jsonPath("$.tauxInteret").value(pretDTO.getTauxInteret()));

        // Verify in database
        Pret savedPret = pretRepository.findByNumeroPret(pretDTO.getNumeroPret())
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        assert savedPret.getMontantTotal().equals(pretDTO.getMontantTotal());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetLoanById_Integration() throws Exception {
        // Given
        Pret pret = createAndSaveSamplePret();

        // When & Then
        mockMvc.perform(get("/dette-tresor/prets/{id}", pret.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(pret.getId()))
            .andExpect(jsonPath("$.numeroPret").value(pret.getNumeroPret()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateLoan_Integration() throws Exception {
        // Given
        Pret pret = createAndSaveSamplePret();
        PretDTO updateDTO = createSamplePretDTO();
        updateDTO.setMontantTotal(new BigDecimal("2000000.00"));

        // When & Then
        mockMvc.perform(put("/dette-tresor/prets/{id}", pret.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.montantTotal").value("2000000.00"));

        // Verify in database
        Pret updatedPret = pretRepository.findById(pret.getId())
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        assert updatedPret.getMontantTotal().equals(new BigDecimal("2000000.00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllLoans_Integration() throws Exception {
        // Given
        createAndSaveSamplePret();
        createAndSaveSamplePret();

        // When & Then
        mockMvc.perform(get("/dette-tresor/prets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetActiveLoans_Integration() throws Exception {
        // Given
        Pret activePret = createAndSaveSamplePret();
        activePret.setSoldeCourant(new BigDecimal("500000.00"));
        pretRepository.save(activePret);

        // When & Then
        mockMvc.perform(get("/dette-tresor/prets/active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].numeroPret").value(activePret.getNumeroPret()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetLoansByOrganization_Integration() throws Exception {
        // Given
        Pret pret = createAndSaveSamplePret();

        // When & Then
        mockMvc.perform(get("/dette-tresor/prets/organisme/{organisme}", pret.getOrganismeBailleur()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].organismeBailleur").value(pret.getOrganismeBailleur()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteLoan_Integration() throws Exception {
        // Given
        Pret pret = createAndSaveSamplePret();

        // When & Then
        mockMvc.perform(delete("/dette-tresor/prets/{id}", pret.getId()))
            .andExpect(status().isNoContent());

        // Verify deleted from database
        assert pretRepository.findById(pret.getId()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "DETTE_DU_TRESOR")
    void testAccessWithDetteRole_Integration() throws Exception {
        // Given
        PretDTO pretDTO = createSamplePretDTO();

        // When & Then - Should work with DETTE_DU_TRESOR role
        mockMvc.perform(post("/dette-tresor/prets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pretDTO)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedWithUserRole_Integration() throws Exception {
        // Given
        PretDTO pretDTO = createSamplePretDTO();

        // When & Then - Should be denied with USER role
        mockMvc.perform(post("/dette-tresor/prets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pretDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthorizedAccess_Integration() throws Exception {
        // When & Then - Should be denied without authentication
        mockMvc.perform(get("/dette-tresor/prets"))
            .andExpect(status().isUnauthorized());
    }

    // Helper methods
    private PretDTO createSamplePretDTO() {
        return PretDTO.builder()
            .numeroPret("PRET-TEST-001")
            .dateSignature(LocalDate.now())
            .organismeBailleur("Banque Mondiale")
            .objet("Projet d'infrastructure")
            .montantTotal(new BigDecimal("1000000.00"))
            .soldeCourant(new BigDecimal("1000000.00"))
            .devise("MAD")
            .duree(60)
            .tauxInteret(new BigDecimal("3.5"))
            .build();
    }

    private Pret createAndSaveSamplePret() {
        Pret pret = Pret.builder()
            .numeroPret("PRET-TEST-" + System.currentTimeMillis())
            .dateSignature(LocalDate.now())
            .organismeBailleur("Banque Mondiale")
            .objet("Projet d'infrastructure")
            .montantTotal(new BigDecimal("1000000.00"))
            .soldeCourant(new BigDecimal("1000000.00"))
            .devise("MAD")
            .duree(60)
            .tauxInteret(new BigDecimal("3.5"))
            .build();
        return pretRepository.save(pret);
    }
}
