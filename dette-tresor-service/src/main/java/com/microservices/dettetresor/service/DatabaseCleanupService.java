package com.microservices.dettetresor.service;

import com.microservices.dettetresor.entity.LettreReglement;
import com.microservices.dettetresor.entity.OrdrePaiement;
import com.microservices.dettetresor.repository.LettreReglementRepository;
import com.microservices.dettetresor.repository.OrdrePaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupService {
    
    private final LettreReglementRepository lettreReglementRepository;
    private final OrdrePaiementRepository ordrePaiementRepository;
    
    /**
     * Identifies and removes duplicate settlement letters with the same ID
     * Keeps only the first occurrence and removes the rest
     */
    @Transactional
    public void cleanupDuplicateSettlementLetters() {
        try {
            List<Object[]> duplicateIds = lettreReglementRepository.findDuplicateIds();
            
            if (duplicateIds.isEmpty()) {
                log.info("No duplicate settlement letters found in the database");
                return;
            }
            
            log.warn("Found {} duplicate settlement letter IDs in the database", duplicateIds.size());
            
            for (Object[] duplicate : duplicateIds) {
                Long id = ((Number) duplicate[0]).longValue();
                int count = ((Number) duplicate[1]).intValue();
                
                log.warn("ID {} has {} duplicate records", id, count);
                
                // Get all records with this ID
                List<LettreReglement> records = lettreReglementRepository.findAllById(List.of(id));
                
                if (records.size() > 1) {
                    // Keep the first record and delete the rest
                    for (int i = 1; i < records.size(); i++) {
                        LettreReglement recordToDelete = records.get(i);
                        log.info("Deleting duplicate settlement letter with ID {} and creation time {}", 
                                recordToDelete.getId(), recordToDelete.getCreatedAt());
                        lettreReglementRepository.delete(recordToDelete);
                    }
                }
            }
            
            log.info("Database cleanup for settlement letters completed successfully");
        } catch (Exception e) {
            log.error("Error during settlement letter database cleanup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup duplicate settlement letters", e);
        }
    }
    
    /**
     * Identifies and removes duplicate payment orders with the same ID
     * Keeps only the first occurrence and removes the rest
     */
    @Transactional
    public void cleanupDuplicatePaymentOrders() {
        try {
            List<Object[]> duplicateIds = ordrePaiementRepository.findDuplicateIds();
            
            if (duplicateIds.isEmpty()) {
                log.info("No duplicate payment orders found in the database");
                return;
            }
            
            log.warn("Found {} duplicate payment order IDs in the database", duplicateIds.size());
            
            for (Object[] duplicate : duplicateIds) {
                Long id = ((Number) duplicate[0]).longValue();
                int count = ((Number) duplicate[1]).intValue();
                
                log.warn("ID {} has {} duplicate records", id, count);
                
                // Get all records with this ID
                List<OrdrePaiement> records = ordrePaiementRepository.findAllById(List.of(id));
                
                if (records.size() > 1) {
                    // Keep the first record and delete the rest
                    for (int i = 1; i < records.size(); i++) {
                        OrdrePaiement recordToDelete = records.get(i);
                        log.info("Deleting duplicate payment order with ID {} and creation time {}", 
                                recordToDelete.getId(), recordToDelete.getCreatedAt());
                        ordrePaiementRepository.delete(recordToDelete);
                    }
                }
            }
            
            log.info("Database cleanup for payment orders completed successfully");
        } catch (Exception e) {
            log.error("Error during payment order database cleanup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup duplicate payment orders", e);
        }
    }
    
    /**
     * Performs cleanup for both settlement letters and payment orders
     */
    @Transactional
    public void cleanupAllDuplicates() {
        log.info("Starting database cleanup for all duplicate records...");
        cleanupDuplicateSettlementLetters();
        cleanupDuplicatePaymentOrders();
        log.info("Database cleanup for all duplicate records completed");
    }
    
    /**
     * Checks if there are any duplicate settlement letters in the database
     */
    @Transactional(readOnly = true)
    public boolean hasDuplicateSettlementLetters() {
        try {
            List<Object[]> duplicateIds = lettreReglementRepository.findDuplicateIds();
            return !duplicateIds.isEmpty();
        } catch (Exception e) {
            log.error("Error checking for duplicate settlement letters: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if there are any duplicate payment orders in the database
     */
    @Transactional(readOnly = true)
    public boolean hasDuplicatePaymentOrders() {
        try {
            List<Object[]> duplicateIds = ordrePaiementRepository.findDuplicateIds();
            return !duplicateIds.isEmpty();
        } catch (Exception e) {
            log.error("Error checking for duplicate payment orders: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if there are any duplicate records in the database
     */
    @Transactional(readOnly = true)
    public boolean hasAnyDuplicates() {
        return hasDuplicateSettlementLetters() || hasDuplicatePaymentOrders();
    }
}