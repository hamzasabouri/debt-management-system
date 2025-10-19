package com.microservices.userservice.service;

import com.microservices.userservice.dto.RoleDTO;
import com.microservices.userservice.entity.Role;
import com.microservices.userservice.repository.RoleRepository;
import com.microservices.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Creating role with name: {}", roleDTO.getNomRole());
        
        // Check if role name already exists
        if (roleRepository.existsByNomRole(roleDTO.getNomRole())) {
            throw new RuntimeException("Role name already exists: " + roleDTO.getNomRole());
        }
        
        Role role = Role.builder()
                .nomRole(roleDTO.getNomRole())
                .description(roleDTO.getDescription())
                .build();
        
        Role savedRole = roleRepository.save(role);
        
        log.info("Role created successfully with ID: {}", savedRole.getId());
        return RoleDTO.fromEntity(savedRole);
    }
    
    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        log.info("Updating role with ID: {}", id);
        
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        
        // Check if role name is being changed and if it already exists
        if (!existingRole.getNomRole().equals(roleDTO.getNomRole()) && 
            roleRepository.existsByNomRole(roleDTO.getNomRole())) {
            throw new RuntimeException("Role name already exists: " + roleDTO.getNomRole());
        }
        
        existingRole.setNomRole(roleDTO.getNomRole());
        existingRole.setDescription(roleDTO.getDescription());
        
        Role updatedRole = roleRepository.save(existingRole);
        
        log.info("Role updated successfully with ID: {}", updatedRole.getId());
        return RoleDTO.fromEntity(updatedRole);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoleDTO findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        return RoleDTO.fromEntity(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoleDTO findByNomRole(String nomRole) {
        Role role = roleRepository.findByNomRole(nomRole)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + nomRole));
        return RoleDTO.fromEntity(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> findAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> searchRoles(String searchText) {
        return roleRepository.searchRoles(searchText)
                .stream()
                .map(RoleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);
        
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with ID: " + id);
        }
        
        // Delete user-role associations first
        userRoleRepository.deleteByRoleId(id);
        
        // Delete role
        roleRepository.deleteById(id);
        
        log.info("Role deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNomRole(String nomRole) {
        return roleRepository.existsByNomRole(nomRole);
    }
    
    @Override
    public void initializeDefaultRoles() {
        log.info("Initializing default roles");
        
        createRoleIfNotExists(Role.ADMIN, "Administrator with full system access");
        createRoleIfNotExists(Role.DETTE_INTERIEUR, "Debt interior management role");
        createRoleIfNotExists(Role.DETTE_DU_TRESOR, "Treasury debt management role");
        createRoleIfNotExists(Role.MEDA, "MEDA system access role");
        
        log.info("Default roles initialization completed");
    }
    
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByNomRole(roleName)) {
            Role role = Role.builder()
                    .nomRole(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created default role: {}", roleName);
        }
    }
}