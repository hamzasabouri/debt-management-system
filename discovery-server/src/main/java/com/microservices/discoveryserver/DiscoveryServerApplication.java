package com.microservices.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Server Application (Eureka Server)
 * Provides service discovery and registration for all microservices
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}