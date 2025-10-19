package com.microservices.detteinterieur.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class SwaggerConfig {
    
    @Value("${server.port:8086}")
    private String serverPort;
    
    @Value("${eureka.instance.hostname:localhost}")
    private String hostname;
    
    @Bean
    public OpenAPI detteInterieurOpenAPI() {
        log.info("Configuring OpenAPI documentation for Dette Intérieur Service");
        
        return new OpenAPI()
                .info(new Info()
                        .title("Dette Intérieur Service API")
                        .description("API pour la gestion de la dette intérieure du Trésor")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support Technique")
                                .email("support@tresor.gov.ma")
                                .url("https://www.tresor.gov.ma"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.tresor.gov.ma/license")))
                .servers(List.of(
                        new Server()
                                .url("http://" + hostname + ":" + serverPort)
                                .description("Dette Intérieur Service - Local"),
                        new Server()
                                .url("http://localhost:8080/dette-interieur")
                                .description("Dette Intérieur Service - Via Gateway")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtenu via le service d'authentification")));
    }
}