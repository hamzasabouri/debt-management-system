package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.EcheancierDTO;
import com.microservices.dettetresor.entity.Echeancier;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.entity.Echeancier.StatutEcheance;
import com.microservices.dettetresor.repository.EcheancierRepository;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.EcheancierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EcheancierServiceImpl implements EcheancierService {
    
    private final EcheancierRepository echeancierRepository;
    private final PretRepository pretRepository;
    
    @Override
    public EcheancierDTO create(EcheancierDTO echeancierDTO) {
        log.info("Creating new payment schedule for loan ID: {}", echeancierDTO.getPretId());
        
        // Check if loan exists
        Pret pret = pretRepository.findById(echeancierDTO.getPretId())
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + echeancierDTO.getPretId()));
        
        // Convert DTO to entity
        Echeancier echeancier = Echeancier.builder()
                .pret(pret)
                .numeroEcheance(echeancierDTO.getNumeroEcheance())
                .dateEcheance(echeancierDTO.getDateEcheance())
                .capital(echeancierDTO.getCapital())
                .interet(echeancierDTO.getInteret())
                .commission(echeancierDTO.getCommission())
                .montantTotal(echeancierDTO.getMontantTotal())
                .statut(echeancierDTO.getStatut() != null ? echeancierDTO.getStatut() : StatutEcheance.PREVU)
                .build();
        
        // Save entity
        Echeancier savedEcheancier = echeancierRepository.save(echeancier);
        
        // Convert back to DTO
        return EcheancierDTO.fromEntity(savedEcheancier);
    }
    
    @Override
    public EcheancierDTO update(Long id, EcheancierDTO echeancierDTO) {
        log.info("Updating payment schedule ID: {} with data: {}", id, echeancierDTO);
        
        try {
            // Check if echeancier exists
            Echeancier existingEcheancier = echeancierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Payment schedule not found with ID: " + id));
            
            log.info("Found existing payment schedule: {}", existingEcheancier);
            
            // Check if loan exists
            Pret pret = pretRepository.findById(echeancierDTO.getPretId())
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + echeancierDTO.getPretId()));
            
            log.info("Found loan: {}", pret);
            
            // Update entity with DTO values
            existingEcheancier.setPret(pret);
            existingEcheancier.setNumeroEcheance(echeancierDTO.getNumeroEcheance());
            existingEcheancier.setDateEcheance(echeancierDTO.getDateEcheance());
            existingEcheancier.setCapital(echeancierDTO.getCapital());
            existingEcheancier.setInteret(echeancierDTO.getInteret());
            existingEcheancier.setCommission(echeancierDTO.getCommission());
            existingEcheancier.setMontantTotal(echeancierDTO.getMontantTotal());
            existingEcheancier.setStatut(echeancierDTO.getStatut() != null ? echeancierDTO.getStatut() : StatutEcheance.PREVU);
            
            log.info("Updated entity values, saving to database");
            
            // Save entity
            Echeancier updatedEcheancier = echeancierRepository.save(existingEcheancier);
            
            log.info("Successfully updated payment schedule: {}", updatedEcheancier);
            
            // Convert back to DTO
            return EcheancierDTO.fromEntity(updatedEcheancier);
        } catch (Exception e) {
            log.error("Error updating payment schedule ID: {}", id, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Long id) {
        log.info("Deleting payment schedule ID: {}", id);
        
        // Check if echeancier exists
        if (!echeancierRepository.existsById(id)) {
            throw new RuntimeException("Payment schedule not found with ID: " + id);
        }
        
        // Delete entity
        echeancierRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> findAll() {
        log.info("Finding all payment schedules");
        List<Echeancier> echeanciers = echeancierRepository.findAll();
        return echeanciers.stream()
                .map(EcheancierDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> findByLoanId(Long loanId) {
        log.info("Finding payment schedules for loan ID: {}", loanId);
        List<Echeancier> echeanciers = echeancierRepository.findByPretIdOrderByDateEcheance(loanId);
        return echeanciers.stream()
                .map(EcheancierDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public EcheancierDTO findById(Long id) {
        log.info("Finding payment schedule by ID: {}", id);
        Echeancier echeancier = echeancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment schedule not found with ID: " + id));
        return EcheancierDTO.fromEntity(echeancier);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> findByStatus(String status) {
        log.info("Finding payment schedules by status: {}", status);
        try {
            StatutEcheance statut = StatutEcheance.valueOf(status.toUpperCase());
            List<Echeancier> echeanciers = echeancierRepository.findByStatutOrderByDateEcheance(statut);
            return echeanciers.stream()
                    .map(EcheancierDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> findOverdueEcheanciers() {
        log.info("Finding overdue payment schedules");
        LocalDate currentDate = LocalDate.now();
        List<StatutEcheance> unpaidStatuses = Arrays.asList(StatutEcheance.PREVU, StatutEcheance.EN_RETARD);
        List<Echeancier> echeanciers = echeancierRepository.findOverduePayments(currentDate, unpaidStatuses);
        return echeanciers.stream()
                .map(EcheancierDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> findEcheanciersDueBetween(LocalDate startDate, LocalDate endDate) {
        log.info("Finding payment schedules due between {} and {}", startDate, endDate);
        List<Echeancier> echeanciers = echeancierRepository.findPaymentsDueBetween(startDate, endDate);
        return echeanciers.stream()
                .map(EcheancierDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public EcheancierDTO updateStatus(Long id, String status) {
        log.info("Updating status of payment schedule ID: {} to {}", id, status);
        
        Echeancier echeancier = echeancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment schedule not found with ID: " + id));
        
        try {
            StatutEcheance statut = StatutEcheance.valueOf(status.toUpperCase());
            echeancier.setStatut(statut);
            Echeancier updatedEcheancier = echeancierRepository.save(echeancier);
            return EcheancierDTO.fromEntity(updatedEcheancier);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EcheancierDTO> searchEcheanciers(Map<String, String> filters) {
        log.info("Searching payment schedules with filters: {}", filters);
        
        // If no filters, return all
        if (filters == null || filters.isEmpty()) {
            log.info("No filters provided, returning all payment schedules");
            return findAll();
        }
        
        // Check for numeroEcheance search
        if (filters.containsKey("numeroEcheance") && filters.get("numeroEcheance") != null && !filters.get("numeroEcheance").trim().isEmpty()) {
            try {
                Integer numeroEcheance = Integer.valueOf(filters.get("numeroEcheance").trim());
                log.info("Searching by numeroEcheance: {}", numeroEcheance);
                List<Echeancier> echeanciers = echeancierRepository.findByNumeroEcheance(numeroEcheance);
                log.info("Found {} payment schedules with numeroEcheance: {}", echeanciers.size(), numeroEcheance);
                return echeanciers.stream()
                    .map(EcheancierDTO::fromEntity)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                log.warn("Invalid numeroEcheance format: {}", filters.get("numeroEcheance"));
                // If not a number, return empty list
                return new ArrayList<>();
            }
        }
        
        // Check for pretId search
        if (filters.containsKey("pretId") && filters.get("pretId") != null && !filters.get("pretId").trim().isEmpty()) {
            try {
                Long pretId = Long.valueOf(filters.get("pretId").trim());
                log.info("Searching by pretId: {}", pretId);
                return findByLoanId(pretId);
            } catch (NumberFormatException e) {
                log.warn("Invalid pretId format: {}", filters.get("pretId"));
                throw new RuntimeException("Invalid pretId format: " + filters.get("pretId"));
            }
        }
        
        // Check for statut search
        if (filters.containsKey("statut") && filters.get("statut") != null && !filters.get("statut").trim().isEmpty()) {
            log.info("Searching by statut: {}", filters.get("statut"));
            return findByStatus(filters.get("statut").trim());
        }
        
        log.info("No specific search criteria matched, returning all payment schedules");
        // Default to returning all if no specific search criteria matched
        return findAll();
    }
}