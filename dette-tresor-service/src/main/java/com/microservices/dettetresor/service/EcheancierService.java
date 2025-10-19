package com.microservices.dettetresor.service;

import com.microservices.dettetresor.dto.EcheancierDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EcheancierService {
    
    /**
     * Create a new payment schedule
     */
    EcheancierDTO create(EcheancierDTO echeancierDTO);
    
    /**
     * Update an existing payment schedule
     */
    EcheancierDTO update(Long id, EcheancierDTO echeancierDTO);
    
    /**
     * Delete a payment schedule
     */
    void delete(Long id);
    
    /**
     * Find all payment schedules
     */
    List<EcheancierDTO> findAll();
    
    /**
     * Find payment schedules by loan ID
     */
    List<EcheancierDTO> findByLoanId(Long loanId);
    
    /**
     * Find payment schedule by ID
     */
    EcheancierDTO findById(Long id);
    
    /**
     * Find payment schedules by status
     */
    List<EcheancierDTO> findByStatus(String status);
    
    /**
     * Find overdue payment schedules
     */
    List<EcheancierDTO> findOverdueEcheanciers();
    
    /**
     * Find payment schedules due within date range
     */
    List<EcheancierDTO> findEcheanciersDueBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Update payment schedule status
     */
    EcheancierDTO updateStatus(Long id, String status);
    
    /**
     * Search payment schedules by criteria
     */
    List<EcheancierDTO> searchEcheanciers(Map<String, String> filters);
}