package com.microservices.userservice.repository;

import com.microservices.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByNomRole(String nomRole);
    
    /**
     * Check if role name exists
     */
    boolean existsByNomRole(String nomRole);
    
    /**
     * Find roles containing text in name or description
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.nomRole) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Role> searchRoles(@Param("searchText") String searchText);
    
    /**
     * Find roles by user ID
     */
    @Query("SELECT r FROM Role r JOIN r.userRoles ur WHERE ur.user.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
}