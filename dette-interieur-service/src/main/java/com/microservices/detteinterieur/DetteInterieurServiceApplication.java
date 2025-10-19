package com.microservices.detteinterieur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

/**
 * Main application class for Dette Intérieur Service
 * 
 * This microservice manages the internal debt operations including:
 * - Adjudications (auctions)
 * - Commissions (Maroclear and BAM)
 * - Bons d'équipement (equipment bonds)
 * - Intérêts sur les dépôts (deposit interests)
 * 
 * The service provides comprehensive CRUD operations with role-based security,
 * financial calculations, reporting, and integration with external systems.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@Slf4j
public class DetteInterieurServiceApplication {
    
    public static void main(String[] args) {
        try {
            log.info("==========================================");
            log.info("Starting Dette Intérieur Service...");
            log.info("==========================================");
            
            SpringApplication app = new SpringApplication(DetteInterieurServiceApplication.class);
            
            // Set default profiles if none specified
            String activeProfiles = System.getProperty("spring.profiles.active");
            if (activeProfiles == null || activeProfiles.isEmpty()) {
                app.setAdditionalProfiles("dev");
                log.info("No active profile specified, using 'dev' profile");
            }
            
            app.run(args);
            
            log.info("==========================================");
            log.info("Dette Intérieur Service started successfully!");
            log.info("Available modules:");
            log.info("- Adjudications (Auction Management)");
            log.info("- Commissions (Maroclear & BAM)");
            log.info("- Bons d'Équipement (Equipment Bonds)");
            log.info("- Intérêts sur Dépôts (Deposit Interests)");
            log.info("==========================================");
            
        } catch (Exception e) {
            log.error("Failed to start Dette Intérieur Service: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}