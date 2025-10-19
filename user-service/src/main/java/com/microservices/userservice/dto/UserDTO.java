package com.microservices.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservices.userservice.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Only for create/update operations
    
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String nomComplet;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;
    
    private User.UserStatus statut;
    
    private LocalDateTime createdAt;
    
    private List<RoleDTO> roles;
    
    // Helper constructor for response without password
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nomComplet(user.getNomComplet())
                .email(user.getEmail())
                .statut(user.getStatut())
                .createdAt(user.getCreatedAt())
                .build();
    }
}