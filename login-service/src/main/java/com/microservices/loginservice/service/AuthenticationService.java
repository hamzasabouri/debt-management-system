package com.microservices.loginservice.service;

import com.microservices.loginservice.dto.LoginRequest;
import com.microservices.loginservice.dto.LoginResponse;

public interface AuthenticationService {
    
    /**
     * Authenticate user and generate JWT token
     */
    LoginResponse authenticate(LoginRequest loginRequest);
    
    /**
     * Validate JWT token
     */
    boolean validateToken(String token, String username);
    
    /**
     * Extract username from token
     */
    String extractUsername(String token);
}