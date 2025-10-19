package com.microservices.loginservice.client;

import com.microservices.loginservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);
    
    @GetMapping("/api/users/exists/username/{username}")
    Boolean checkUsernameExists(@PathVariable("username") String username);
    
    @GetMapping("/api/users/auth/username/{username}")
    Map<String, String> getUserForAuth(@PathVariable("username") String username);
}