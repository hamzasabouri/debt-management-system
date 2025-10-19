package com.microservices.dettetresor.util;

import com.microservices.dettetresor.service.DatabaseCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Utility class to clean up duplicate records in the database on application startup
 * This can be enabled by setting the application property 'app.cleanup-duplicates=true'
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupUtil implements CommandLineRunner {
    
    private final DatabaseCleanupService databaseCleanupService;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if cleanup should be performed on startup
        String cleanupProperty = System.getProperty("app.cleanup-duplicates", "false");
        if ("true".equalsIgnoreCase(cleanupProperty)) {
            log.info("Starting database cleanup for duplicate settlement letters...");
            try {
                databaseCleanupService.cleanupDuplicateSettlementLetters();
                log.info("Database cleanup completed successfully");
            } catch (Exception e) {
                log.error("Error during database cleanup: {}", e.getMessage(), e);
            }
        } else {
            log.info("Database cleanup on startup is disabled. To enable, set -Dapp.cleanup-duplicates=true");
        }
    }
}