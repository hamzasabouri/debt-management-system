package com.microservices.loginservice.controller;

import com.microservices.loginservice.config.JwtUtil;
import com.microservices.loginservice.dto.LoginRequest;
import com.microservices.loginservice.dto.LoginResponse;
import com.microservices.loginservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    @GetMapping("/test")
    public String test() {
        return "Login service is up!";
}

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            LoginResponse response = authenticationService.authenticate(loginRequest);
            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate token endpoint
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Token validation request received");
        
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Invalid authorization header"));
            }
            
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String username = jwtUtil.getUsernameFromToken(token);
            boolean isValid = authenticationService.validateToken(token, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);
                
                response.put("userId", userId);
                response.put("username", username);
                response.put("roles", roles);
                response.put("expiresAt", jwtUtil.getExpirationAsLocalDateTime(token));
            } else {
                response.put("message", "Invalid token");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Extract user information from token
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Get current user request received");
        
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
            }
            
            String token = authorizationHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (authenticationService.validateToken(token, username)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", userId);
                userInfo.put("username", username);
                userInfo.put("roles", roles);
                userInfo.put("expiresAt", jwtUtil.getExpirationAsLocalDateTime(token));
                
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
            }
        } catch (Exception e) {
            log.error("Get current user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}