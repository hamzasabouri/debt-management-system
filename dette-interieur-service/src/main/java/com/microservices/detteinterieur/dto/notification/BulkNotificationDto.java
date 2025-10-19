package com.microservices.detteinterieur.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bulk notification operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkNotificationDto {
    
    @JsonProperty("notification_ids")
    @NotNull(message = "Notification IDs are required")
    private java.util.List<Long> notificationIds;
    
    @JsonProperty("action")
    @NotBlank(message = "Action is required")
    private String action; // MARK_READ, MARK_PROCESSED, DELETE
}