package com.microservices.detteinterieur.config;

import com.microservices.detteinterieur.service.StatutMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatutMigrationInitializer {
    
    private final StatutMigrationService statutMigrationService;
    
    /**
     * Run the status migration after the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void migrateStatuts() {
        log.info("Starting equipment bond status migration process");
        
        try {
            long annuleCount = statutMigrationService.countAnnuleBonds();
            if (annuleCount > 0) {
                log.info("Found {} equipment bonds with ANNULE status that need migration", annuleCount);
                statutMigrationService.migrateAnnuleToRejete();
                log.info("Equipment bond status migration completed successfully");
            } else {
                log.info("No equipment bonds with ANNULE status found. Migration not needed.");
            }
        } catch (Exception e) {
            log.error("Error during equipment bond status migration: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
            // The temporary enum constant will handle existing records
        }
    }
}