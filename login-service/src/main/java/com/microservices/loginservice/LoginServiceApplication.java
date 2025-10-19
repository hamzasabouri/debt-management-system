package com.microservices.loginservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LoginServiceApplication {

    public static void main(String[] args) {
        // Enable dev profile for testing
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(LoginServiceApplication.class, args);
    }
}