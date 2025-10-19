package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.AvisCreditDTO;
import com.microservices.dettetresor.entity.AvisCredit;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.entity.AvisCredit.TypeAvisCredit;
import com.microservices.dettetresor.exception.ResourceNotFoundException;
import com.microservices.dettetresor.repository.AvisCreditRepository;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.AvisCreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvisCreditServiceImpl implements AvisCreditService {

    private final AvisCreditRepository avisCreditRepository;
    private final PretRepository pretRepository;

    @Override
    @Transactional
    public AvisCreditDTO createAvisCredit(AvisCreditDTO avisCreditDTO) {
        // Check if pret exists
        Pret pret = pretRepository.findById(avisCreditDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Pret not found with id: " + avisCreditDTO.getPretId()));
        
        // Check if avis credit with same number already exists
        if (avisCreditRepository.existsByNumeroAvis(avisCreditDTO.getNumeroAvis())) {
            throw new IllegalStateException("Avis credit with number " + avisCreditDTO.getNumeroAvis() + " already exists");
        }
        
        AvisCredit avisCredit = AvisCredit.builder()
                .numeroAvis(avisCreditDTO.getNumeroAvis())
                .dateReception(avisCreditDTO.getDateReception())
                .montant(avisCreditDTO.getMontant())
                .devise(avisCreditDTO.getDevise())
                .emetteur(avisCreditDTO.getEmetteur())
                .typeAvis(avisCreditDTO.getTypeAvis())
                .pret(pret)
                .build();
        
        AvisCredit savedAvisCredit = avisCreditRepository.save(avisCredit);
        return convertToDto(savedAvisCredit);
    }

    @Override
    @Transactional
    public AvisCreditDTO updateAvisCredit(Long id, AvisCreditDTO avisCreditDTO) {
        AvisCredit existingAvisCredit = avisCreditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AvisCredit not found with id: " + id));
        
        // Check if pret exists
        Pret pret = pretRepository.findById(avisCreditDTO.getPretId())
                .orElseThrow(() -> new ResourceNotFoundException("Pret not found with id: " + avisCreditDTO.getPretId()));
        
        // Check if another avis credit with the same number exists (excluding current one)
        if (avisCreditRepository.existsByNumeroAvisAndIdNot(avisCreditDTO.getNumeroAvis(), id)) {
            throw new IllegalStateException("Another avis credit with number " + avisCreditDTO.getNumeroAvis() + " already exists");
        }
        
        // Map fields explicitly to avoid issues with ModelMapper and relationships
        existingAvisCredit.setNumeroAvis(avisCreditDTO.getNumeroAvis());
        existingAvisCredit.setDateReception(avisCreditDTO.getDateReception());
        existingAvisCredit.setMontant(avisCreditDTO.getMontant());
        existingAvisCredit.setDevise(avisCreditDTO.getDevise());
        existingAvisCredit.setTypeAvis(avisCreditDTO.getTypeAvis());
        existingAvisCredit.setPret(pret);
        
        AvisCredit updatedAvisCredit = avisCreditRepository.save(existingAvisCredit);
        return convertToDto(updatedAvisCredit);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisCreditDTO getAvisCreditById(Long id) {
        AvisCredit avisCredit = avisCreditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AvisCredit not found with id: " + id));
        return convertToDto(avisCredit);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisCreditDTO getAvisCreditByNumero(String numeroAvis) {
        AvisCredit avisCredit = avisCreditRepository.findByNumeroAvis(numeroAvis)
                .orElseThrow(() -> new ResourceNotFoundException("AvisCredit not found with number: " + numeroAvis));
        return convertToDto(avisCredit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisCreditDTO> getAllAvisCredits() {
        return avisCreditRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisCreditDTO> getAvisCreditsByPretId(Long pretId) {
        if (!pretRepository.existsById(pretId)) {
            throw new ResourceNotFoundException("Pret not found with id: " + pretId);
        }
        
        return avisCreditRepository.findByPretId(pretId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisCreditDTO> searchAvisCredits(Map<String, String> searchParams) {
        log.info("Searching avis credits with parameters: {}", searchParams);
        
        // If no search parameters, return all
        if (searchParams == null || searchParams.isEmpty()) {
            return getAllAvisCredits();
        }
        
        // For simplicity, we'll implement a basic search by numeroAvis
        // In a real implementation, you might want to search by multiple fields
        String numeroAvis = searchParams.get("numeroAvis");
        if (numeroAvis != null && !numeroAvis.isEmpty()) {
            return avisCreditRepository.findByNumeroAvisContainingIgnoreCase(numeroAvis).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        
        // Default to returning all if no specific search criteria
        return getAllAvisCredits();
    }

    @Override
    @Transactional
    public void deleteAvisCredit(Long id) {
        if (!avisCreditRepository.existsById(id)) {
            throw new ResourceNotFoundException("AvisCredit not found with id: " + id);
        }
        avisCreditRepository.deleteById(id);
    }
    
    private AvisCreditDTO convertToDto(AvisCredit avisCredit) {
        return AvisCreditDTO.builder()
                .id(avisCredit.getId())
                .pretId(avisCredit.getPret().getId())
                .numeroAvis(avisCredit.getNumeroAvis())
                .dateReception(avisCredit.getDateReception())
                .montant(avisCredit.getMontant())
                .devise(avisCredit.getDevise())
                .emetteur(avisCredit.getEmetteur())
                .typeAvis(avisCredit.getTypeAvis())
                .createdAt(avisCredit.getCreatedAt())
                .build();
    }
}