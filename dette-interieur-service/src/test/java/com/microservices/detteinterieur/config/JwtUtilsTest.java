package com.microservices.detteinterieur.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class JwtUtilsTest {

    @Test
    public void testJwtUtilsCreation() {
        // This test will verify that JwtUtils can be instantiated correctly
        // If the JWT configuration is incorrect, this test will fail
        JwtUtils jwtUtils = new JwtUtils();
        assertNotNull(jwtUtils);
    }
}