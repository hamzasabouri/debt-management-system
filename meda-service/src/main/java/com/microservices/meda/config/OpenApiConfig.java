package com.microservices.meda.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MEDA Program and Working Capital Service API",
                description = "API for managing MEDA program projects, advances, and supporting documents",
                version = "1.0.0",
                contact = @Contact(
                        name = "MEDA Service Team",
                        email = "meda@microservices.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Gateway Server"),
                @Server(url = "http://localhost:8085", description = "MEDA Service Direct")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}