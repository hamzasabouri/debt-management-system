package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.AvisDebitDTO;
import com.microservices.dettetresor.entity.AvisDebit;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.repository.AvisDebitRepository;
import com.microservices.dettetresor.repository.OrdrePaiementRepository;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.AvisDebitService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvisDebitServiceImpl implements AvisDebitService {

    private final AvisDebitRepository avisDebitRepository;
    private final PretRepository pretRepository;
    private final OrdrePaiementRepository ordrePaiementRepository; // Added this
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AvisDebitDTO createAvisDebit(AvisDebitDTO avisDebitDTO) {
        // Check if pret exists
        Pret pret = pretRepository.findById(avisDebitDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Pret not found with id: " + avisDebitDTO.getPretId()));
        
        // Check if avis debit with same number already exists
        if (avisDebitRepository.existsByNumeroAvis(avisDebitDTO.getNumeroAvis())) {
            throw new IllegalStateException("Avis debit with number " + avisDebitDTO.getNumeroAvis() + " already exists");
        }
        
        AvisDebit avisDebit = modelMapper.map(avisDebitDTO, AvisDebit.class);
        avisDebit.setPret(pret);
        
        // Set the payment order if provided
        if (avisDebitDTO.getOrdrePaiementId() != null) {
            OrdrePaiement ordrePaiement = ordrePaiementRepository.findById(avisDebitDTO.getOrdrePaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement not found with id: " + avisDebitDTO.getOrdrePaiementId()));
            avisDebit.setOrdrePaiement(ordrePaiement);
        }
        
        AvisDebit savedAvisDebit = avisDebitRepository.save(avisDebit);
        return convertToDto(savedAvisDebit);
    }

    @Override
    @Transactional
    public AvisDebitDTO updateAvisDebit(Long id, AvisDebitDTO avisDebitDTO) {
        AvisDebit existingAvisDebit = avisDebitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AvisDebit not found with id: " + id));
        
        // Check if pret exists
        Pret pret = pretRepository.findById(avisDebitDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Pret not found with id: " + avisDebitDTO.getPretId()));
        
        // Check if another avis debit with the same number exists (excluding current one)
        if (avisDebitRepository.existsByNumeroAvisAndIdNot(avisDebitDTO.getNumeroAvis(), id)) {
            throw new IllegalStateException("Another avis debit with number " + avisDebitDTO.getNumeroAvis() + " already exists");
        }
        
        // Map fields explicitly to avoid issues with ModelMapper and relationships
        existingAvisDebit.setNumeroAvis(avisDebitDTO.getNumeroAvis());
        existingAvisDebit.setDateReception(avisDebitDTO.getDateReception());
        existingAvisDebit.setMontant(avisDebitDTO.getMontant());
        existingAvisDebit.setDevise(avisDebitDTO.getDevise());
        existingAvisDebit.setMotif(avisDebitDTO.getMotif());
        existingAvisDebit.setPret(pret);
        
        // Set the payment order if provided
        if (avisDebitDTO.getOrdrePaiementId() != null) {
            OrdrePaiement ordrePaiement = ordrePaiementRepository.findById(avisDebitDTO.getOrdrePaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de paiement not found with id: " + avisDebitDTO.getOrdrePaiementId()));
            existingAvisDebit.setOrdrePaiement(ordrePaiement);
        } else {
            existingAvisDebit.setOrdrePaiement(null);
        }
        
        AvisDebit updatedAvisDebit = avisDebitRepository.save(existingAvisDebit);
        return convertToDto(updatedAvisDebit);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDebitDTO getAvisDebitById(Long id) {
        AvisDebit avisDebit = avisDebitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AvisDebit not found with id: " + id));
        return convertToDto(avisDebit);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDebitDTO getAvisDebitByNumero(String numeroAvis) {
        AvisDebit avisDebit = avisDebitRepository.findByNumeroAvis(numeroAvis)
                .orElseThrow(() -> new ResourceNotFoundException("AvisDebit not found with number: " + numeroAvis));
        return convertToDto(avisDebit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDebitDTO> getAllAvisDebits() {
        return avisDebitRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDebitDTO> getAvisDebitsByPretId(Long pretId) {
        if (!pretRepository.existsById(pretId)) {
            throw new ResourceNotFoundException("Pret not found with id: " + pretId);
        }
        
        return avisDebitRepository.findByPretId(pretId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAvisDebit(Long id) {
        if (!avisDebitRepository.existsById(id)) {
            throw new ResourceNotFoundException("AvisDebit not found with id: " + id);
        }
        avisDebitRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AvisDebitDTO> searchAvisDebits(Map<String, String> filters) {
        List<AvisDebit> results = new ArrayList<>();
        
        // Check for numero_avis search
        if (filters.containsKey("numero_avis")) {
            String numeroAvis = filters.get("numero_avis");
            avisDebitRepository.findByNumeroAvis(numeroAvis)
                .ifPresent(results::add);
            return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        // Check for devise search
        if (filters.containsKey("devise")) {
            String devise = filters.get("devise");
            return avisDebitRepository.findByDevise(devise).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        // Check for motif search
        if (filters.containsKey("motif")) {
            String motif = filters.get("motif");
            return avisDebitRepository.findByMotif(motif).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        // Check for pret_id search
        if (filters.containsKey("pret_id")) {
            try {
                Long pretId = Long.parseLong(filters.get("pret_id"));
                return getAvisDebitsByPretId(pretId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid pret_id format: " + filters.get("pret_id"));
            }
        }
        
        // If no filters match, return all advices
        return getAllAvisDebits();
    }
    
    private AvisDebitDTO convertToDto(AvisDebit avisDebit) {
        // Use the builder pattern to avoid ModelMapper issues with bidirectional relationships
        return AvisDebitDTO.builder()
                .id(avisDebit.getId())
                .pretId(avisDebit.getPret().getId())
                .ordrePaiementId(avisDebit.getOrdrePaiement() != null ? avisDebit.getOrdrePaiement().getId() : null)
                .numeroAvis(avisDebit.getNumeroAvis())
                .dateReception(avisDebit.getDateReception())
                .montant(avisDebit.getMontant())
                .devise(avisDebit.getDevise())
                .motif(avisDebit.getMotif())
                .createdAt(avisDebit.getCreatedAt())
                .build();
    }
}