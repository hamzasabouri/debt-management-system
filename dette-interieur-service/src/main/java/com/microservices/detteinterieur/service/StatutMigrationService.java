package com.microservices.detteinterieur.service;

import com.microservices.detteinterieur.entity.BonEquipement;
import com.microservices.detteinterieur.repository.BonEquipementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatutMigrationService {
    
    private final BonEquipementRepository bonEquipementRepository;
    
    /**
     * Migrate all equipment bonds with status "ANNULE" to "REJETE"
     * This should be run after the application starts and before normal operations
     */
    @Transactional
    public void migrateAnnuleToRejete() {
        log.info("Starting migration of ANNULE status to REJETE");
        
        try {
            // Find all equipment bonds with ANNULE status (temporary enum value)
            List<BonEquipement> annuleBonds = bonEquipementRepository.findByStatut(BonEquipement.StatutBon.ANNULE);
            log.info("Found {} equipment bonds with ANNULE status", annuleBonds.size());
            
            // Update each bond to use REJETE status
            for (BonEquipement bond : annuleBonds) {
                bond.setStatut(BonEquipement.StatutBon.REJETE);
                bonEquipementRepository.save(bond);
                log.debug("Updated bond {} from ANNULE to REJETE", bond.getNumeroBon());
            }
            
            log.info("Successfully migrated {} equipment bonds from ANNULE to REJETE", annuleBonds.size());
        } catch (Exception e) {
            log.error("Error during migration of ANNULE to REJETE: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to migrate equipment bond statuses", e);
        }
    }
    
    /**
     * Get count of equipment bonds with ANNULE status
     */
    @Transactional(readOnly = true)
    public long countAnnuleBonds() {
        try {
            return bonEquipementRepository.countByStatut(BonEquipement.StatutBon.ANNULE);
        } catch (Exception e) {
            log.error("Error counting ANNULE bonds: {}", e.getMessage(), e);
            return 0;
        }
    }
}