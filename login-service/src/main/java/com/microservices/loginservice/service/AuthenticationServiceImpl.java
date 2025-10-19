package com.microservices.loginservice.service;

import com.microservices.loginservice.client.UserServiceClient;
import com.microservices.loginservice.config.JwtUtil;
import com.microservices.loginservice.dto.LoginRequest;
import com.microservices.loginservice.dto.LoginResponse;
import com.microservices.loginservice.dto.UserDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        
        try {
            // Get user authentication info from user service (includes password hash and roles)
            Map<String, String> userAuthInfo = userServiceClient.getUserForAuth(loginRequest.getUsername());
            
            log.debug("Retrieved user auth info from user-service for user: {}", loginRequest.getUsername());
            
            // Check if user is active
            if (!"ACTIF".equals(userAuthInfo.get("statut"))) {
                log.warn("Account is inactive for user: {}", loginRequest.getUsername());
                throw new RuntimeException("Account is inactive");
            }
            
            // Verify password
            log.debug("Raw password from request: {}", loginRequest.getPassword());
            log.debug("Hashed password from user-service: {}", userAuthInfo.get("passwordHash"));
            
            boolean passwordMatch = passwordEncoder.matches(
                loginRequest.getPassword(), 
                userAuthInfo.get("passwordHash")
            );
            log.debug("Password match result: {}", passwordMatch);
            
            if (!passwordMatch) {
                log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
                throw new RuntimeException("Invalid credentials");
            }
            
            // Get full user info for token generation
            UserDTO user = userServiceClient.getUserByUsername(loginRequest.getUsername());
            
            // Extract roles from userAuthInfo
            List<String> roles = List.of();
            String rolesString = userAuthInfo.get("roles");
            if (rolesString != null && !rolesString.isEmpty()) {
                roles = Arrays.asList(rolesString.split(","));
            }
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
            
            // Build response
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNomComplet(user.getNomComplet());
            userInfo.setEmail(user.getEmail());
            userInfo.setStatut(user.getStatut());
            userInfo.setRoles(roles);
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setTokenType("Bearer");
            response.setExpiresAt(jwtUtil.getExpirationAsLocalDateTime(token));
            response.setUserInfo(userInfo);
            
            log.info("User authenticated successfully: {}", loginRequest.getUsername());
            return response;
            
        } catch (FeignException.NotFound e) {
            log.error("User not found: {}", loginRequest.getUsername());
            throw new RuntimeException("User not found");
        } catch (FeignException e) {
            log.error("Error communicating with user service: {}", e.getMessage());
            throw new RuntimeException("Authentication service temporarily unavailable");
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateToken(String token, String username) {
        try {
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String extractUsername(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("Username extraction failed: {}", e.getMessage());
            return null;
        }
    }
}