package com.microservices.detteinterieur.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for external system integrations
 * Sets up RestTemplate and HTTP client configurations for BAM, Comptabilite, and DTFE
 */
@Configuration
@Slf4j
public class IntegrationConfig {
    
    @Value("${integration.http.connection-timeout:30000}")
    private int connectionTimeout;
    
    @Value("${integration.http.read-timeout:60000}")
    private int readTimeout;
    
    @Value("${integration.http.max-connections:50}")
    private int maxConnections;
    
    @Value("${integration.http.max-connections-per-route:20}")
    private int maxConnectionsPerRoute;
    
    /**
     * Configure RestTemplate for external integrations
     * @return configured RestTemplate with timeouts and connection pooling
     */
    @Bean(name = "integrationRestTemplate")
    public RestTemplate restTemplate() {
        log.info("Configuring RestTemplate for external integrations");
        
        ClientHttpRequestFactory factory = createHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);
        
        log.info("RestTemplate configured with connection timeout: {}ms, read timeout: {}ms", 
                connectionTimeout, readTimeout);
        
        return restTemplate;
    }
    
    /**
     * Create HTTP request factory with connection pooling and timeouts
     */
    private ClientHttpRequestFactory createHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // Set timeouts
        factory.setConnectTimeout(connectionTimeout);
        factory.setConnectionRequestTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        log.info("HTTP client factory configured with timeouts - Connection: {}ms, Read: {}ms", 
                connectionTimeout, readTimeout);
        
        return factory;
    }
    
    /**
     * Configuration properties validation
     */
    @Bean
    public IntegrationConfigValidator integrationConfigValidator(
            @Value("${integration.bam.base-url:#{null}}") String bamBaseUrl,
            @Value("${integration.bam.api-key:#{null}}") String bamApiKey,
            @Value("${integration.comptabilite.base-url:#{null}}") String comptabiliteBaseUrl,
            @Value("${integration.comptabilite.api-key:#{null}}") String comptabiliteApiKey,
            @Value("${integration.dtfe.base-url:#{null}}") String dtfeBaseUrl,
            @Value("${integration.dtfe.api-key:#{null}}") String dtfeApiKey) {
        
        return new IntegrationConfigValidator(
                bamBaseUrl, bamApiKey,
                comptabiliteBaseUrl, comptabiliteApiKey,
                dtfeBaseUrl, dtfeApiKey);
    }
    
    /**
     * Validator for integration configuration
     */
    public static class IntegrationConfigValidator {
        
        public IntegrationConfigValidator(String bamBaseUrl, String bamApiKey,
                String comptabiliteBaseUrl, String comptabiliteApiKey,
                String dtfeBaseUrl, String dtfeApiKey) {
            
            validateIntegrationConfig("BAM", bamBaseUrl, bamApiKey);
            validateIntegrationConfig("Comptabilite", comptabiliteBaseUrl, comptabiliteApiKey);
            validateIntegrationConfig("DTFE", dtfeBaseUrl, dtfeApiKey);
            
            log.info("Integration configuration validation completed");
        }
        
        private void validateIntegrationConfig(String systemName, String baseUrl, String apiKey) {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                log.warn("{} integration: Base URL not configured", systemName);
            } else {
                log.info("{} integration: Base URL configured ({})", systemName, maskUrl(baseUrl));
            }
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.warn("{} integration: API key not configured", systemName);
            } else {
                log.info("{} integration: API key configured ({})", systemName, maskApiKey(apiKey));
            }
        }
        
        private String maskUrl(String url) {
            if (url == null || url.length() <= 10) {
                return url;
            }
            return url.substring(0, Math.min(url.length(), 20)) + "...";
        }
        
        private String maskApiKey(String apiKey) {
            if (apiKey == null || apiKey.length() <= 4) {
                return "***";
            }
            return apiKey.substring(0, 4) + "***";
        }
    }
}