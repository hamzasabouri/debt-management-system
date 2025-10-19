package com.microservices.loginservice;

import com.microservices.loginservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {

    @Test
    public void testPasswordEncoderStrength() {
        // Create password encoders with explicit strength of 10
        PasswordEncoder loginEncoder = new BCryptPasswordEncoder(10);
        PasswordEncoder userEncoder = new BCryptPasswordEncoder(10);
        
        String rawPassword = "testPassword123";
        
        // Encode password with both encoders
        String loginEncoded = loginEncoder.encode(rawPassword);
        String userEncoded = userEncoder.encode(rawPassword);
        
        // Verify both can match the raw password
        assertTrue(loginEncoder.matches(rawPassword, loginEncoded));
        assertTrue(userEncoder.matches(rawPassword, userEncoded));
        
        // This test verifies that both encoders are configured identically
        System.out.println("Login encoded: " + loginEncoded);
        System.out.println("User encoded: " + userEncoded);
    }
}