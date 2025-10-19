package com.microservices.userservice.service;

import com.microservices.userservice.dto.RoleDTO;

import java.util.List;

public interface RoleService {
    
    /**
     * Create a new role
     */
    RoleDTO createRole(RoleDTO roleDTO);
    
    /**
     * Update an existing role
     */
    RoleDTO updateRole(Long id, RoleDTO roleDTO);
    
    /**
     * Find role by ID
     */
    RoleDTO findById(Long id);
    
    /**
     * Find role by name
     */
    RoleDTO findByNomRole(String nomRole);
    
    /**
     * Find all roles
     */
    List<RoleDTO> findAllRoles();
    
    /**
     * Search roles by text
     */
    List<RoleDTO> searchRoles(String searchText);
    
    /**
     * Delete role
     */
    void deleteRole(Long id);
    
    /**
     * Check if role name exists
     */
    boolean existsByNomRole(String nomRole);
    
    /**
     * Initialize default roles
     */
    void initializeDefaultRoles();
}