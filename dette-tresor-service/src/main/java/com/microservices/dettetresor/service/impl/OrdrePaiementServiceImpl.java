package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.OrdrePaiementDTO;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.entity.LettreReglement;
import com.microservices.dettetresor.entity.AvisDebit;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.repository.OrdrePaiementRepository;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.repository.LettreReglementRepository;
import com.microservices.dettetresor.repository.AvisDebitRepository;
import com.microservices.dettetresor.service.DatabaseCleanupService;
import com.microservices.dettetresor.service.OrdrePaiementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdrePaiementServiceImpl implements OrdrePaiementService {

    private static final Logger log = LoggerFactory.getLogger(OrdrePaiementServiceImpl.class);

    private final OrdrePaiementRepository ordrePaiementRepository;
    private final PretRepository pretRepository;
    private final LettreReglementRepository lettreReglementRepository;
    private final AvisDebitRepository avisDebitRepository;
    private final ModelMapper modelMapper;
    private final DatabaseCleanupService databaseCleanupService;

    @Override
    @Transactional
    public OrdrePaiementDTO createOrdrePaiement(OrdrePaiementDTO ordrePaiementDTO) {
        // Vérifier si le prêt existe
        Pret pret = pretRepository.findById(ordrePaiementDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + ordrePaiementDTO.getPretId()));
        
        // Vérifier si la lettre de règlement existe si elle est spécifiée
        LettreReglement lettreReglement = null;
        if (ordrePaiementDTO.getLettreReglementId() != null) {
            lettreReglement = lettreReglementRepository.findById(ordrePaiementDTO.getLettreReglementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + ordrePaiementDTO.getLettreReglementId()));
        }
        
        // Vérifier si un ordre de paiement avec le même numéro existe déjà
        if (ordrePaiementRepository.existsByNumeroOrdre(ordrePaiementDTO.getNumeroOrdre())) {
            throw new IllegalStateException("Un ordre de paiement avec le numéro " + ordrePaiementDTO.getNumeroOrdre() + " existe déjà");
        }
        
        OrdrePaiement ordrePaiement = modelMapper.map(ordrePaiementDTO, OrdrePaiement.class);
        ordrePaiement.setPret(pret);
        ordrePaiement.setLettreReglement(lettreReglement);
        
        OrdrePaiement savedOrdre = ordrePaiementRepository.save(ordrePaiement);
        return convertToDto(savedOrdre);
    }

    @Override
    @Transactional
    public OrdrePaiementDTO updateOrdrePaiement(Long id, OrdrePaiementDTO ordrePaiementDTO) {
        OrdrePaiement existingOrdre = ordrePaiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + id));
        
        // Vérifier si le prêt existe
        Pret pret = pretRepository.findById(ordrePaiementDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + ordrePaiementDTO.getPretId()));
        
        // Vérifier si la lettre de règlement existe si elle est spécifiée
        LettreReglement lettreReglement = null;
        if (ordrePaiementDTO.getLettreReglementId() != null) {
            lettreReglement = lettreReglementRepository.findById(ordrePaiementDTO.getLettreReglementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + ordrePaiementDTO.getLettreReglementId()));
        }
        
        // Vérifier si un autre ordre avec le même numéro existe (sauf celui en cours de modification)
        if (ordrePaiementRepository.existsByNumeroOrdreAndIdNot(ordrePaiementDTO.getNumeroOrdre(), id)) {
            throw new IllegalStateException("Un autre ordre de paiement avec le numéro " + ordrePaiementDTO.getNumeroOrdre() + " existe déjà");
        }
        
        // Map fields explicitly to avoid issues with ModelMapper and relationships
        existingOrdre.setNumeroOrdre(ordrePaiementDTO.getNumeroOrdre());
        existingOrdre.setDateEmission(ordrePaiementDTO.getDateEmission());
        existingOrdre.setMontant(ordrePaiementDTO.getMontant());
        existingOrdre.setDevise(ordrePaiementDTO.getDevise());
        existingOrdre.setStatut(ordrePaiementDTO.getStatut());
        existingOrdre.setPret(pret);
        existingOrdre.setLettreReglement(lettreReglement);
        
        OrdrePaiement updatedOrdre = ordrePaiementRepository.save(existingOrdre);
        return convertToDto(updatedOrdre);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdrePaiementDTO getOrdrePaiementById(Long id) {
        try {
            OrdrePaiement ordre = ordrePaiementRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + id));
            return convertToDto(ordre);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            // Handle case where multiple records with the same ID exist
            log.error("Multiple records found with the same ID: {}", id, e);
            // Try to get the first record with this ID using a custom query
            List<OrdrePaiement> ordres = ordrePaiementRepository.findAllById(List.of(id));
            if (!ordres.isEmpty()) {
                return convertToDto(ordres.get(0));
            } else {
                throw new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + id);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrdrePaiementDTO getOrdrePaiementByNumero(String numeroOrdre) {
        OrdrePaiement ordre = ordrePaiementRepository.findByNumeroOrdre(numeroOrdre)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement non trouvé avec le numéro : " + numeroOrdre));
        return convertToDto(ordre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdrePaiementDTO> getAllOrdrePaiements() {
        try {
            return ordrePaiementRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IncorrectResultSizeDataAccessException e) {
            log.error("Data inconsistency detected: {}", e.getMessage());
            // Try to clean up duplicates and retry
            try {
                databaseCleanupService.cleanupAllDuplicates();
                return ordrePaiementRepository.findAll().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            } catch (Exception cleanupException) {
                log.error("Failed to cleanup duplicates: {}", cleanupException.getMessage(), cleanupException);
                throw new RuntimeException("Data inconsistency detected and automatic cleanup failed. Please contact system administrator.", e);
            }
        } catch (Exception e) {
            log.error("Error fetching all payment orders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch payment orders: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdrePaiementDTO> getOrdrePaiementsByPretId(Long pretId) {
        if (!pretRepository.existsById(pretId)) {
            throw new ResourceNotFoundException("Prêt non trouvé avec l'ID : " + pretId);
        }
        
        return ordrePaiementRepository.findByPretIdOrderByDateEmission(pretId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdrePaiementDTO> getOrdrePaiementsByLettreReglementId(Long lettreReglementId) {
        if (!lettreReglementRepository.existsById(lettreReglementId)) {
            throw new ResourceNotFoundException("Lettre de règlement non trouvée avec l'ID : " + lettreReglementId);
        }
        
        return ordrePaiementRepository.findByLettreReglementId(lettreReglementId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrdrePaiement(Long id) {
        if (!ordrePaiementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ordre de paiement non trouvé avec l'ID : " + id);
        }
        ordrePaiementRepository.deleteById(id);
    }
    
    private OrdrePaiementDTO convertToDto(OrdrePaiement ordrePaiement) {
        try {
            // Use the builder pattern to avoid ModelMapper issues with bidirectional relationships
            return OrdrePaiementDTO.builder()
                    .id(ordrePaiement.getId())
                    .pretId(ordrePaiement.getPret().getId())
                    .lettreReglementId(ordrePaiement.getLettreReglement() != null ? ordrePaiement.getLettreReglement().getId() : null)
                    .numeroOrdre(ordrePaiement.getNumeroOrdre())
                    .dateEmission(ordrePaiement.getDateEmission())
                    .montant(ordrePaiement.getMontant())
                    .devise(ordrePaiement.getDevise())
                    .echeance(ordrePaiement.getEcheance())
                    .statut(ordrePaiement.getStatut())
                    .createdAt(ordrePaiement.getCreatedAt())
                    .build();
        } catch (IncorrectResultSizeDataAccessException e) {
            log.error("Data inconsistency in LettreReglement with ID: {}", 
                ordrePaiement.getLettreReglement() != null ? ordrePaiement.getLettreReglement().getId() : "null");
            // Return DTO with null lettreReglementId to avoid breaking the application
            return OrdrePaiementDTO.builder()
                    .id(ordrePaiement.getId())
                    .pretId(ordrePaiement.getPret().getId())
                    .lettreReglementId(null) // Set to null to avoid data inconsistency issues
                    .numeroOrdre(ordrePaiement.getNumeroOrdre())
                    .dateEmission(ordrePaiement.getDateEmission())
                    .montant(ordrePaiement.getMontant())
                    .devise(ordrePaiement.getDevise())
                    .echeance(ordrePaiement.getEcheance())
                    .statut(ordrePaiement.getStatut())
                    .createdAt(ordrePaiement.getCreatedAt())
                    .build();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrdrePaiementDTO> searchOrdrePaiements(Map<String, String> searchParams) {
        List<OrdrePaiement> results = new ArrayList<>();
        
        // Check for numero_ordre search (frontend field name)
        if (searchParams.containsKey("numero_ordre")) {
            String numeroOrdre = searchParams.get("numero_ordre");
            ordrePaiementRepository.findByNumeroOrdre(numeroOrdre)
                .ifPresent(results::add);
            return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        // Check for devise search
        if (searchParams.containsKey("devise")) {
            String devise = searchParams.get("devise");
            return ordrePaiementRepository.findByDeviseOrderByDateEmission(devise).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        // Check for statut search
        if (searchParams.containsKey("statut")) {
            try {
                String statutStr = searchParams.get("statut");
                OrdrePaiement.StatutOrdrePaiement statut = OrdrePaiement.StatutOrdrePaiement.valueOf(statutStr);
                return ordrePaiementRepository.findByStatutOrderByDateEmission(statut).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + searchParams.get("statut"));
            }
        }
        
        // Mapping of frontend field names to entity field names
        if (searchParams.containsKey("date_emission")) {
            // Handle date search if needed
            // This would require a custom repository method
        }
        
        if (searchParams.containsKey("pret_id")) {
            Long pretId = Long.parseLong(searchParams.get("pret_id"));
            return getOrdrePaiementsByPretId(pretId);
        }
        
        // If no search parameters match, return all orders
        return getAllOrdrePaiements();
    }
}