package com.microservices.loginservice.config;

import com.microservices.loginservice.client.UserServiceClient;
import com.microservices.loginservice.dto.UserDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Configuration
@Profile("dev")
public class TestUserConfiguration {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Bean
    // Removed @Primary annotation to avoid conflict with Feign client
    public UserServiceClient mockUserServiceClient() {
        return new UserServiceClient() {
            @Override
            public UserDTO getUserByUsername(String username) {
                // Create a test role
                UserDTO.RoleDTO roleDTO = new UserDTO.RoleDTO();
                roleDTO.setId(1L);
                roleDTO.setNomRole("ROLE_USER");
                roleDTO.setDescription("Regular user role");

                // Create a test user
                UserDTO userDTO = new UserDTO();
                userDTO.setId(1L);
                userDTO.setUsername(username);
                userDTO.setPassword("$2a$10$N2ZSS6AU1cXBGn.xkO3QAe9OX6FCQlxYsa8mnDOuWaJBW2CwLLSby"); // encoded "test123"
                userDTO.setNomComplet("Test User");
                userDTO.setEmail(username + "@example.com");
                userDTO.setStatut("ACTIF");
                userDTO.setCreatedAt(LocalDateTime.now());
                
                List<UserDTO.RoleDTO> roles = new ArrayList<>();
                roles.add(roleDTO);
                userDTO.setRoles(roles);
                
                return userDTO;
            }

            @Override
            public Boolean checkUsernameExists(String username) {
                // For test, we'll say "test" user exists
                return "test".equals(username);
            }

            @Override
            public Map<String, String> getUserForAuth(String username) {
                // For test purposes
                Map<String, String> authInfo = new HashMap<>();
                authInfo.put("id", "1");
                authInfo.put("username", username);
                authInfo.put("passwordHash", "$2a$10$N2ZSS6AU1cXBGn.xkO3QAe9OX6FCQlxYsa8mnDOuWaJBW2CwLLSby"); // encoded "test123"
                authInfo.put("statut", "ACTIF");
                
                return authInfo;
            }
        };
    }
}