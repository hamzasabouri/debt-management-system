package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.LettreReglementDTO;
import com.microservices.dettetresor.entity.LettreReglement;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.repository.LettreReglementRepository;
import com.microservices.dettetresor.repository.OrdrePaiementRepository;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.DatabaseCleanupService;
import com.microservices.dettetresor.service.LettreReglementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LettreReglementServiceImpl implements LettreReglementService {

    private static final Logger log = LoggerFactory.getLogger(LettreReglementServiceImpl.class);

    private final LettreReglementRepository lettreReglementRepository;
    private final PretRepository pretRepository;
    private final OrdrePaiementRepository ordrePaiementRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;
    private final DatabaseCleanupService databaseCleanupService;

    @Override
    @Transactional
    public LettreReglementDTO createLettreReglement(LettreReglementDTO lettreReglementDTO) {
        // Vérifier si le prêt existe
        Pret pret = pretRepository.findById(lettreReglementDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + lettreReglementDTO.getPretId()));
        
        // Vérifier si une lettre de règlement avec le même numéro existe déjà
        if (lettreReglementRepository.existsByNumeroLettre(lettreReglementDTO.getNumeroLettre())) {
            throw new IllegalStateException("Une lettre de règlement avec le numéro " + lettreReglementDTO.getNumeroLettre() + " existe déjà");
        }
        
        // Create entity explicitly to avoid issues with ModelMapper and relationships
        LettreReglement lettreReglement = new LettreReglement();
        lettreReglement.setNumeroLettre(lettreReglementDTO.getNumeroLettre());
        lettreReglement.setDateTransmission(lettreReglementDTO.getDateTransmission());
        lettreReglement.setMontant(lettreReglementDTO.getMontant());
        lettreReglement.setDevise(lettreReglementDTO.getDevise());
        lettreReglement.setCompteTresor(lettreReglementDTO.getCompteTresor());
        lettreReglement.setCheminFichierPdf(lettreReglementDTO.getCheminFichierPdf());
        lettreReglement.setPret(pret);
        
        // Set the payment order if provided
        if (lettreReglementDTO.getOrdrePaiementId() != null) {
            OrdrePaiement ordrePaiement = ordrePaiementRepository.findById(lettreReglementDTO.getOrdrePaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + lettreReglementDTO.getOrdrePaiementId()));
            lettreReglement.setOrdrePaiement(ordrePaiement);
        }
        
        LettreReglement savedLettre = lettreReglementRepository.save(lettreReglement);
        return convertToDto(savedLettre);
    }

    @Override
    @Transactional
    public LettreReglementDTO updateLettreReglement(Long id, LettreReglementDTO lettreReglementDTO) {
        try {
            LettreReglement existingLettre = lettreReglementRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + id));
            
            // Vérifier si le prêt existe
            Pret pret = pretRepository.findById(lettreReglementDTO.getPretId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + lettreReglementDTO.getPretId()));
            
            // Vérifier si une autre lettre avec le même numéro existe (sauf celle en cours de modification)
            if (lettreReglementRepository.existsByNumeroLettreAndIdNot(lettreReglementDTO.getNumeroLettre(), id)) {
                throw new IllegalStateException("Une autre lettre de règlement avec le numéro " + lettreReglementDTO.getNumeroLettre() + " existe déjà");
            }
            
            // Map fields explicitly to avoid issues with ModelMapper and relationships
            existingLettre.setNumeroLettre(lettreReglementDTO.getNumeroLettre());
            existingLettre.setDateTransmission(lettreReglementDTO.getDateTransmission());
            existingLettre.setMontant(lettreReglementDTO.getMontant());
            existingLettre.setDevise(lettreReglementDTO.getDevise());
            existingLettre.setCompteTresor(lettreReglementDTO.getCompteTresor());
            existingLettre.setCheminFichierPdf(lettreReglementDTO.getCheminFichierPdf());
            existingLettre.setPret(pret);
            
            // Set the payment order if provided
            if (lettreReglementDTO.getOrdrePaiementId() != null) {
                OrdrePaiement ordrePaiement = ordrePaiementRepository.findById(lettreReglementDTO.getOrdrePaiementId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + lettreReglementDTO.getOrdrePaiementId()));
                existingLettre.setOrdrePaiement(ordrePaiement);
            } else {
                existingLettre.setOrdrePaiement(null);
            }
            
            LettreReglement updatedLettre = lettreReglementRepository.save(existingLettre);
            // Refresh the entity to ensure all relationships are properly loaded
            entityManager.refresh(updatedLettre);
            return convertToDto(updatedLettre);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Handle case where multiple records with the same ID exist
            log.error("Multiple records found with the same ID: {}", id, e);
            // Try to clean up duplicates and retry
            try {
                databaseCleanupService.cleanupDuplicateSettlementLetters();
                // Retry the update operation
                LettreReglement existingLettre = lettreReglementRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + id));
                
                // Vérifier si le prêt existe
                Pret pret = pretRepository.findById(lettreReglementDTO.getPretId())
                        .orElseThrow(() -> new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + lettreReglementDTO.getPretId()));
                
                // Vérifier si une autre lettre avec le même numéro existe (sauf celle en cours de modification)
                if (lettreReglementRepository.existsByNumeroLettreAndIdNot(lettreReglementDTO.getNumeroLettre(), id)) {
                    throw new IllegalStateException("Une autre lettre de règlement avec le numéro " + lettreReglementDTO.getNumeroLettre() + " existe déjà");
                }
                
                // Map fields explicitly to avoid issues with ModelMapper and relationships
                existingLettre.setNumeroLettre(lettreReglementDTO.getNumeroLettre());
                existingLettre.setDateTransmission(lettreReglementDTO.getDateTransmission());
                existingLettre.setMontant(lettreReglementDTO.getMontant());
                existingLettre.setDevise(lettreReglementDTO.getDevise());
                existingLettre.setCompteTresor(lettreReglementDTO.getCompteTresor());
                existingLettre.setCheminFichierPdf(lettreReglementDTO.getCheminFichierPdf());
                existingLettre.setPret(pret);
                
                // Set the payment order if provided
                if (lettreReglementDTO.getOrdrePaiementId() != null) {
                    OrdrePaiement ordrePaiement = ordrePaiementRepository.findById(lettreReglementDTO.getOrdrePaiementId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + lettreReglementDTO.getOrdrePaiementId()));
                    existingLettre.setOrdrePaiement(ordrePaiement);
                } else {
                    existingLettre.setOrdrePaiement(null);
                }
                
                LettreReglement updatedLettre = lettreReglementRepository.save(existingLettre);
                // Refresh the entity to ensure all relationships are properly loaded
                entityManager.refresh(updatedLettre);
                return convertToDto(updatedLettre);
            } catch (Exception cleanupException) {
                log.error("Failed to cleanup duplicates and retry update: {}", cleanupException.getMessage(), cleanupException);
                throw new RuntimeException("Data inconsistency detected and automatic cleanup failed. Please contact system administrator.", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LettreReglementDTO getLettreReglementById(Long id) {
        try {
            LettreReglement lettre = lettreReglementRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + id));
            return convertToDto(lettre);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Handle case where multiple records with the same ID exist
            log.error("Multiple records found with the same ID: {}", id, e);
            // Try to get the first record with this ID using a custom query
            List<LettreReglement> lettres = lettreReglementRepository.findAllById(List.of(id));
            if (!lettres.isEmpty()) {
                return convertToDto(lettres.get(0));
            } else {
                throw new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + id);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LettreReglementDTO getLettreReglementByNumero(String numeroLettre) {
        LettreReglement lettre = lettreReglementRepository.findByNumeroLettre(numeroLettre)
                .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec le numéro : " + numeroLettre));
        return convertToDto(lettre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LettreReglementDTO> getAllLettreReglements() {
        return lettreReglementRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LettreReglementDTO> getLettreReglementsByPretId(Long pretId) {
        if (!pretRepository.existsById(pretId)) {
            throw new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + pretId);
        }
        
        return lettreReglementRepository.findByPretId(pretId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LettreReglementDTO> searchLettreReglements(Map<String, String> filters) {
        try {
            // Use the new search method for better flexibility
            String numeroLettre = filters.get("numero_lettre");
            String devise = filters.get("devise");
            String compteTresor = filters.get("compte_tresor");
            Long pretId = null;
            
            if (filters.containsKey("pret_id")) {
                try {
                    pretId = Long.parseLong(filters.get("pret_id"));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid pret_id format: " + filters.get("pret_id"));
                }
            }
            
            // Perform the search
            List<LettreReglement> results = lettreReglementRepository.searchByCriteria(
                numeroLettre, devise, compteTresor, pretId);
            
            return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Handle case where multiple records with the same ID exist
            log.error("Data inconsistency detected during search: {}", e.getMessage());
            // Try to clean up duplicates and retry
            try {
                databaseCleanupService.cleanupDuplicateSettlementLetters();
                // Retry the search operation
                String numeroLettre = filters.get("numero_lettre");
                String devise = filters.get("devise");
                String compteTresor = filters.get("compte_tresor");
                Long pretId = null;
                
                if (filters.containsKey("pret_id")) {
                    try {
                        pretId = Long.parseLong(filters.get("pret_id"));
                    } catch (NumberFormatException e2) {
                        throw new IllegalArgumentException("Invalid pret_id format: " + filters.get("pret_id"));
                    }
                }
                
                // Perform the search
                List<LettreReglement> results = lettreReglementRepository.searchByCriteria(
                    numeroLettre, devise, compteTresor, pretId);
                
                return results.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            } catch (Exception cleanupException) {
                log.error("Failed to cleanup duplicates and retry search: {}", cleanupException.getMessage(), cleanupException);
                throw new RuntimeException("Data inconsistency detected during search and automatic cleanup failed. Please contact system administrator.", e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteLettreReglement(Long id) {
        if (!lettreReglementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + id);
        }
        lettreReglementRepository.deleteById(id);
    }
    
    private LettreReglementDTO convertToDto(LettreReglement lettreReglement) {
        // Use the builder pattern to avoid ModelMapper issues with bidirectional relationships
        return LettreReglementDTO.builder()
                .id(lettreReglement.getId())
                .pretId(lettreReglement.getPret().getId())
                .ordrePaiementId(lettreReglement.getOrdrePaiement() != null ? lettreReglement.getOrdrePaiement().getId() : null)
                .numeroLettre(lettreReglement.getNumeroLettre())
                .dateTransmission(lettreReglement.getDateTransmission())
                .montant(lettreReglement.getMontant())
                .devise(lettreReglement.getDevise())
                .compteTresor(lettreReglement.getCompteTresor())
                .cheminFichierPdf(lettreReglement.getCheminFichierPdf())
                .createdAt(lettreReglement.getCreatedAt())
                .build();
    }
}