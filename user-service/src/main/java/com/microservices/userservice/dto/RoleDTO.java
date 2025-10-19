package com.microservices.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservices.userservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    
    private Long id;
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String nomRole;
    
    private String description;
    
    // Helper method to convert from entity
    public static RoleDTO fromEntity(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .nomRole(role.getNomRole())
                .description(role.getDescription())
                .build();
    }
    
    // Helper method to convert to entity
    public Role toEntity() {
        return Role.builder()
                .id(this.id)
                .nomRole(this.nomRole)
                .description(this.description)
                .build();
    }
}