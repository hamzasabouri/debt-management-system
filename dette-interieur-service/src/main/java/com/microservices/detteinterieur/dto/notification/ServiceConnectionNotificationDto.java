package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for service connection notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceConnectionNotificationDto {
    
    @JsonProperty("service_name")
    @NotBlank(message = "Service name is required")
    private String serviceName;
    
    @JsonProperty("connection_status")
    @NotBlank(message = "Connection status is required")
    private String connectionStatus; // CONNECTED, DISCONNECTED, ERROR
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("error_message")
    private String errorMessage;
}