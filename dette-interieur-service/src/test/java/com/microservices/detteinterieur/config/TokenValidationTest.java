package com.microservices.detteinterieur.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TokenValidationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void testTokenRoleValidation() {
        // This is the token from the user's error report
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwidXNlcklkIjo4LCJ1c2VybmFtZSI6InRlc3QiLCJyb2xlcyI6WyJkZXR0ZSBpbnRlcmlldXIiXSwiaWF0IjoxNzU2Nzc0MzE1LCJleHAiOjE3NTY4NjA3MTV9.hPuZBDSAjTli6D-SjpQcNwuPq30MFTfBavXmtqQZg2VFdBhZt6on3N8YWMzBXSxnQDK-mfZ_Of9V9OZkFozA8Q";
        
        // Test token validation
        boolean isTokenValid = jwtUtils.validateToken(token);
        System.out.println("Is token valid: " + isTokenValid);
        
        // Test username extraction
        String username = jwtUtils.extractUsername(token);
        System.out.println("Extracted username: " + username);
        
        // Test user ID extraction
        Long userId = jwtUtils.extractUserId(token);
        System.out.println("Extracted user ID: " + userId);
        
        // Test role extraction
        List<String> roles = jwtUtils.extractRoles(token);
        System.out.println("Extracted roles: " + roles);
        
        // Test authorities extraction
        Collection<? extends GrantedAuthority> authorities = jwtUtils.extractAuthorities(token);
        System.out.println("Extracted authorities: " + authorities);
        
        // Test if user can access dette interieur module
        boolean canAccess = jwtUtils.canAccessDetteInterieur(token);
        System.out.println("Can access dette interieur module: " + canAccess);
        
        // Test specific role checks
        boolean isAdmin = jwtUtils.isAdmin(token);
        System.out.println("Is admin: " + isAdmin);
        
        boolean isDetteInterieurUser = jwtUtils.isDetteInterieurUser(token);
        System.out.println("Is dette interieur user: " + isDetteInterieurUser);
        
        // Test hasRole with different formats
        boolean hasDetteInterieurRole1 = jwtUtils.hasRole(token, "dette interieur");
        System.out.println("Has 'dette interieur' role: " + hasDetteInterieurRole1);
        
        boolean hasDetteInterieurRole2 = jwtUtils.hasRole(token, "dette_interieur");
        System.out.println("Has 'dette_interieur' role: " + hasDetteInterieurRole2);
        
        boolean hasDetteInterieurRole3 = jwtUtils.hasRole(token, "DETTE_INTERIEUR");
        System.out.println("Has 'DETTE_INTERIEUR' role: " + hasDetteInterieurRole3);
        
        // Verify that the user should have access
        assertTrue(isTokenValid, "Token should be valid");
        assertEquals("test", username, "Username should be 'test'");
        assertEquals(Long.valueOf(8), userId, "User ID should be 8");
        assertTrue(canAccess, "User should be able to access Dette Intérieur module");
        assertTrue(isDetteInterieurUser, "User should be identified as dette interieur user");
        assertTrue(hasDetteInterieurRole3, "User should have DETTE_INTERIEUR role");
    }
}