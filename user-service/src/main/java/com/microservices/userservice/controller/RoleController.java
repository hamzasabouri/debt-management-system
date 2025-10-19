package com.microservices.userservice.controller;

import com.microservices.userservice.dto.RoleDTO;
import com.microservices.userservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Create a new role
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("Creating new role with name: {}", roleDTO.getNomRole());
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing role
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        log.info("Updating role with ID: {}", id);
        RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }
    
    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Retrieving role with ID: {}", id);
        RoleDTO role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }
    
    /**
     * Get role by name
     */
    @GetMapping("/name/{nomRole}")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String nomRole) {
        log.info("Retrieving role with name: {}", nomRole);
        RoleDTO role = roleService.findByNomRole(nomRole);
        return ResponseEntity.ok(role);
    }
    
    /**
     * Get all roles
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Retrieving all roles");
        List<RoleDTO> roles = roleService.findAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Search roles
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoleDTO>> searchRoles(@RequestParam String searchText) {
        log.info("Searching roles with text: {}", searchText);
        List<RoleDTO> roles = roleService.searchRoles(searchText);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Deleting role with ID: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check if role name exists
     */
    @GetMapping("/exists/{nomRole}")
    public ResponseEntity<Boolean> checkRoleExists(@PathVariable String nomRole) {
        boolean exists = roleService.existsByNomRole(nomRole);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Initialize default roles
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultRoles() {
        log.info("Initializing default roles");
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok().build();
    }
}