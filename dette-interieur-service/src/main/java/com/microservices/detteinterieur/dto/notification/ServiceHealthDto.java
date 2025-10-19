package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for service health status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHealthDto {
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("status")
    private String status; // CONNECTED, DISCONNECTED, ERROR
    
    @JsonProperty("last_seen")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSeen;
    
    @JsonProperty("error_count")
    private Long errorCount;
    
    @JsonProperty("connection_count")
    private Long connectionCount;
    
    // Explicit getters for Lombok
    public String getServiceName() {
        return serviceName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public Long getErrorCount() {
        return errorCount;
    }
    
    public Long getConnectionCount() {
        return connectionCount;
    }
    
    // Explicit builder method
    public static ServiceHealthDtoBuilder builder() {
        return new ServiceHealthDtoBuilder();
    }
    
    public static class ServiceHealthDtoBuilder {
        private String serviceName;
        private String status;
        private LocalDateTime lastSeen;
        private Long errorCount;
        private Long connectionCount;
        
        public ServiceHealthDtoBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public ServiceHealthDtoBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public ServiceHealthDtoBuilder lastSeen(LocalDateTime lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }
        
        public ServiceHealthDtoBuilder errorCount(Long errorCount) {
            this.errorCount = errorCount;
            return this;
        }
        
        public ServiceHealthDtoBuilder connectionCount(Long connectionCount) {
            this.connectionCount = connectionCount;
            return this;
        }
        
        public ServiceHealthDto build() {
            return new ServiceHealthDto(serviceName, status, lastSeen, errorCount, connectionCount);
        }
    }
}