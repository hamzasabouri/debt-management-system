package com.microservices.dettetresor.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8086}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Development Server"),
                new Server()
                    .url("http://localhost:8080")
                    .description("Gateway Server")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }
    
    private Info apiInfo() {
        return new Info()
            .title("Dette du Trésor Service API")
            .description("""
                Microservice for managing external debt and treasury operations.
                
                **Features:**
                - External loan management (Pret)
                - Payment schedules (Echeancier)
                - Credit advice processing (AvisCredit)
                - Payment orders (OrdrePaiement)
                - Settlement letters (LettreReglement)
                - Debit advice (AvisDebit)
                - PDF generation for settlement letters
                - Integration with BAM (Bank Al-Maghrib)
                - Integration with DTFE (Direction du Trésor et des Finances Extérieures)
                - Automatic balance calculations
                - Interest calculations and penalties
                - Role-based security (Admin, Dette du Tresor)
                
                **Authentication:**
                Use JWT token in Authorization header: `Bearer <token>`
                
                **Roles:**
                - `admin`: Full access to all operations
                - `dette du tresor`: Access to debt management operations
                - `dette interieur`: Read-only access to statistics
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("Treasury IT Team")
                .email("treasury-it@finances.gov.ma")
                .url("https://finances.gov.ma"))
            .license(new License()
                .name("Ministry of Economy and Finance")
                .url("https://finances.gov.ma"));
    }
    
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
}