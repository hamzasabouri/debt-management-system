package com.microservices.dettetresor.service.impl;

import com.microservices.dettetresor.dto.PretDTO;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.repository.PretRepository;
import com.microservices.dettetresor.service.PretService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PretServiceImpl implements PretService {
    
    private final PretRepository pretRepository;
    
    @Override
    public PretDTO createLoan(PretDTO pretDTO) {
        log.info("Creating loan with number: {}", pretDTO.getNumeroPret());
        
        if (pretRepository.existsByNumeroPret(pretDTO.getNumeroPret())) {
            throw new RuntimeException("Loan number already exists: " + pretDTO.getNumeroPret());
        }
        
        Pret pret = pretDTO.toEntity();
        // Initialize current balance with total amount
        if (pret.getSoldeCourant() == null) {
            pret.setSoldeCourant(pret.getMontantTotal());
        }
        
        Pret savedPret = pretRepository.save(pret);
        log.info("Loan created successfully with ID: {}", savedPret.getId());
        
        return PretDTO.fromEntity(savedPret);
    }
    
    @Override
    public PretDTO updateLoan(Long id, PretDTO pretDTO) {
        log.info("Updating loan with ID: {}", id);
        
        Pret existingPret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        
        // Check if loan number is being changed and if it already exists
        if (!existingPret.getNumeroPret().equals(pretDTO.getNumeroPret()) && 
            pretRepository.existsByNumeroPret(pretDTO.getNumeroPret())) {
            throw new RuntimeException("Loan number already exists: " + pretDTO.getNumeroPret());
        }
        
        // Update loan fields
        existingPret.setNumeroPret(pretDTO.getNumeroPret());
        existingPret.setDateSignature(pretDTO.getDateSignature());
        existingPret.setOrganismeBailleur(pretDTO.getOrganismeBailleur());
        existingPret.setObjet(pretDTO.getObjet());
        existingPret.setMontantTotal(pretDTO.getMontantTotal());
        existingPret.setSoldeCourant(pretDTO.getSoldeCourant());
        existingPret.setDevise(pretDTO.getDevise());
        existingPret.setDuree(pretDTO.getDuree());
        existingPret.setTauxInteret(pretDTO.getTauxInteret());
        
        Pret updatedPret = pretRepository.save(existingPret);
        log.info("Loan updated successfully with ID: {}", updatedPret.getId());
        
        return PretDTO.fromEntity(updatedPret);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PretDTO findById(Long id) {
        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        return PretDTO.fromEntity(pret);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PretDTO findByIdWithEcheanciers(Long id) {
        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        // This will trigger the lazy loading of Echeanciers
        pret.getEcheanciers().size(); // This forces the initialization of the collection
        return PretDTO.fromEntityWithEcheanciers(pret);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PretDTO findByLoanNumber(String numeroPret) {
        Pret pret = pretRepository.findByNumeroPret(numeroPret)
                .orElseThrow(() -> new RuntimeException("Loan not found with number: " + numeroPret));
        return PretDTO.fromEntity(pret);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findAllLoans() {
        return pretRepository.findAll()
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findActiveLoans() {
        return pretRepository.findActiveLoans()
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findFullyPaidLoans() {
        return pretRepository.findFullyPaidLoans()
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findByLendingOrganization(String organismeBailleur) {
        return pretRepository.findByOrganismeBailleurContainingIgnoreCase(organismeBailleur)
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findByCurrency(String devise) {
        return pretRepository.findByDevise(devise)
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> findBySignatureDateRange(LocalDate startDate, LocalDate endDate) {
        return pretRepository.findBySignatureDateBetween(startDate, endDate)
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PretDTO> searchLoans(String numeroPret, String organismeBailleur, String devise) {
        return pretRepository.searchLoans(numeroPret, organismeBailleur, devise)
                .stream()
                .map(PretDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public PretDTO updateCurrentBalance(Long id, BigDecimal newBalance) {
        log.info("Updating current balance for loan ID: {} to {}", id, newBalance);
        
        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Current balance cannot be negative");
        }
        
        if (newBalance.compareTo(pret.getMontantTotal()) > 0) {
            throw new RuntimeException("Current balance cannot exceed total loan amount");
        }
        
        pret.setSoldeCourant(newBalance);
        Pret updatedPret = pretRepository.save(pret);
        
        log.info("Current balance updated successfully for loan ID: {}", id);
        return PretDTO.fromEntity(updatedPret);
    }
    
    @Override
    public PretDTO recalculateCurrentBalance(Long id) {
        log.info("Recalculating current balance for loan ID: {}", id);
        
        Pret pret = pretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
        
        // Calculate paid amount from payment schedules
        BigDecimal totalPaid = pret.getEcheanciers().stream()
                .filter(e -> e.getStatut() == com.microservices.dettetresor.entity.Echeancier.StatutEcheance.PAYE)
                .map(com.microservices.dettetresor.entity.Echeancier::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal newBalance = pret.getMontantTotal().subtract(totalPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        
        pret.setSoldeCourant(newBalance);
        Pret updatedPret = pretRepository.save(pret);
        
        log.info("Current balance recalculated successfully for loan ID: {} - New balance: {}", id, newBalance);
        return PretDTO.fromEntity(updatedPret);
    }
    
    @Override
    public void deleteLoan(Long id) {
        log.info("Deleting loan with ID: {}", id);
        
        if (!pretRepository.existsById(id)) {
            throw new RuntimeException("Loan not found with ID: " + id);
        }
        
        pretRepository.deleteById(id);
        log.info("Loan deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTotalOutstandingDebtByCurrency() {
        return pretRepository.getTotalOutstandingDebtByCurrency();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoanNumber(String numeroPret) {
        return pretRepository.existsByNumeroPret(numeroPret);
    }
}