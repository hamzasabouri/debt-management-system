package com.microservices.meda.service;

import com.microservices.meda.entity.PieceJustificative;
import com.microservices.meda.entity.Projet;
import com.microservices.meda.entity.PieceJustificative.TypePiece;
import com.microservices.meda.entity.PieceJustificative.StatutPiece;
import com.microservices.meda.repository.PieceJustificativeRepository;
import com.microservices.meda.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PieceJustificativeService {
    
    private final PieceJustificativeRepository pieceJustificativeRepository;
    private final ProjetRepository projetRepository;
    
    /**
     * Create a new supporting document
     */
    public PieceJustificative createPieceJustificative(PieceJustificative piece) {
        log.info("Creating new supporting document: {}", piece.getNumeroPiece());
        
        // Validate document number uniqueness
        if (pieceJustificativeRepository.existsByNumeroPiece(piece.getNumeroPiece())) {
            throw new IllegalArgumentException("Document number already exists: " + piece.getNumeroPiece());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(piece.getProjet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + piece.getProjet().getId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(piece.getDevise())) {
            throw new IllegalArgumentException("Document currency must match project currency");
        }
        
        // Validate available funds
        BigDecimal availableFunds = calculateAvailableFunds(projet.getId());
        if (piece.getMontant().compareTo(availableFunds) > 0) {
            throw new IllegalArgumentException("Insufficient funds. Available: " + availableFunds + ", Requested: " + piece.getMontant());
        }
        
        // Set the project reference
        piece.setProjet(projet);
        
        PieceJustificative savedPiece = pieceJustificativeRepository.save(piece);
        log.info("Supporting document created successfully with ID: {}", savedPiece.getId());
        return savedPiece;
    }
    
    /**
     * Update an existing supporting document
     */
    public PieceJustificative updatePieceJustificative(Long id, PieceJustificative piece) {
        log.info("Updating supporting document with ID: {}", id);
        
        PieceJustificative existingPiece = getPieceJustificativeById(id);
        
        // Validate document number uniqueness (excluding current document)
        if (pieceJustificativeRepository.existsByNumeroPieceAndIdNot(piece.getNumeroPiece(), id)) {
            throw new IllegalArgumentException("Document number already exists: " + piece.getNumeroPiece());
        }
        
        // Validate project exists
        Projet projet = projetRepository.findById(piece.getProjet().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + piece.getProjet().getId()));
        
        // Validate currency matches project currency
        if (!projet.getDevise().equals(piece.getDevise())) {
            throw new IllegalArgumentException("Document currency must match project currency");
        }
        
        // Update fields
        existingPiece.setProjet(projet);
        existingPiece.setNumeroPiece(piece.getNumeroPiece());
        existingPiece.setTypePiece(piece.getTypePiece());
        existingPiece.setDateExecution(piece.getDateExecution());
        existingPiece.setMontant(piece.getMontant());
        existingPiece.setDevise(piece.getDevise());
        existingPiece.setEmetteur(piece.getEmetteur());
        existingPiece.setStatut(piece.getStatut());
        existingPiece.setCommentaire(piece.getCommentaire());
        
        PieceJustificative updatedPiece = pieceJustificativeRepository.save(existingPiece);
        log.info("Supporting document updated successfully: {}", updatedPiece.getNumeroPiece());
        return updatedPiece;
    }
    
    /**
     * Get supporting document by ID
     */
    @Transactional(readOnly = true)
    public PieceJustificative getPieceJustificativeById(Long id) {
        return pieceJustificativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supporting document not found with ID: " + id));
    }
    
    /**
     * Get supporting document by number
     */
    @Transactional(readOnly = true)
    public Optional<PieceJustificative> getPieceJustificativeByNumber(String numeroPiece) {
        return pieceJustificativeRepository.findByNumeroPiece(numeroPiece);
    }
    
    /**
     * Get all supporting documents with pagination
     */
    @Transactional(readOnly = true)
    public Page<PieceJustificative> getAllPiecesJustificatives(Pageable pageable) {
        return pieceJustificativeRepository.findAll(pageable);
    }
    
    /**
     * Get all supporting documents
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getAllPiecesJustificatives() {
        return pieceJustificativeRepository.findAll();
    }
    
    /**
     * Delete supporting document
     */
    public void deletePieceJustificative(Long id) {
        log.info("Deleting supporting document with ID: {}", id);
        
        PieceJustificative piece = getPieceJustificativeById(id);
        
        // Business rule: Can only delete documents that are not validated
        if (piece.isValide()) {
            throw new IllegalArgumentException("Cannot delete validated supporting document");
        }
        
        pieceJustificativeRepository.delete(piece);
        log.info("Supporting document deleted successfully: {}", piece.getNumeroPiece());
    }
    
    /**
     * Get supporting documents by project ID
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByProjet(Long projetId) {
        return pieceJustificativeRepository.findByProjetIdOrderByDateExecution(projetId);
    }
    
    /**
     * Get supporting documents by project ID and type
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByProjetAndType(Long projetId, TypePiece typePiece) {
        return pieceJustificativeRepository.findByProjetIdAndTypePieceOrderByDateExecution(projetId, typePiece);
    }
    
    /**
     * Get supporting documents by project ID and status
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByProjetAndStatus(Long projetId, StatutPiece statut) {
        return pieceJustificativeRepository.findByProjetIdAndStatutOrderByDateExecution(projetId, statut);
    }
    
    /**
     * Get supporting documents by type
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByType(TypePiece typePiece) {
        return pieceJustificativeRepository.findByTypePieceOrderByDateExecution(typePiece);
    }
    
    /**
     * Get supporting documents by status
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByStatus(StatutPiece statut) {
        return pieceJustificativeRepository.findByStatutOrderByDateExecution(statut);
    }
    
    /**
     * Get supporting documents by currency
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByDevise(String devise) {
        return pieceJustificativeRepository.findByDeviseOrderByDateExecution(devise);
    }
    
    /**
     * Get supporting documents by issuer
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByEmetteur(String emetteur) {
        return pieceJustificativeRepository.findByEmetteurIgnoreCaseOrderByDateExecution(emetteur);
    }
    
    /**
     * Get supporting documents by execution date range
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByDateRange(LocalDate startDate, LocalDate endDate) {
        return pieceJustificativeRepository.findByExecutionDateBetween(startDate, endDate);
    }
    
    /**
     * Get supporting documents by amount range
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return pieceJustificativeRepository.findByAmountRange(minAmount, maxAmount);
    }
    
    /**
     * Search supporting documents
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> searchPiecesJustificatives(String searchTerm) {
        return pieceJustificativeRepository.searchDocuments(searchTerm);
    }
    
    /**
     * Get recent supporting documents
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getRecentPiecesJustificatives(int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        return pieceJustificativeRepository.findRecentDocuments(fromDate);
    }
    
    /**
     * Get supporting documents pending validation
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesPendingValidation() {
        return pieceJustificativeRepository.findDocumentsPendingValidation();
    }
    
    /**
     * Get rejected supporting documents
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getRejectedPiecesJustificatives() {
        return pieceJustificativeRepository.findRejectedDocuments();
    }
    
    /**
     * Get largest validated expenses
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getLargestValidatedExpenses() {
        return pieceJustificativeRepository.findLargestValidatedExpenses();
    }
    
    /**
     * Get supporting documents with comments
     */
    @Transactional(readOnly = true)
    public List<PieceJustificative> getPiecesJustificativesWithComments() {
        return pieceJustificativeRepository.findDocumentsWithComments();
    }
    
    /**
     * Get total validated expenses by project
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalValidatedExpensesByProjet(Long projetId) {
        return pieceJustificativeRepository.getTotalValidatedExpensesByProject(projetId);
    }
    
    /**
     * Get total expenses by project and type
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByProjetAndType(Long projetId, TypePiece typePiece) {
        return pieceJustificativeRepository.getTotalExpensesByProjectAndType(projetId, typePiece);
    }
    
    /**
     * Get total expenses by project and status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesByProjetAndStatus(Long projetId, StatutPiece statut) {
        return pieceJustificativeRepository.getTotalExpensesByProjectAndStatus(projetId, statut);
    }
    
    /**
     * Get documents statistics by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDocumentsStatisticsByType() {
        return pieceJustificativeRepository.getDocumentsStatisticsByType();
    }
    
    /**
     * Get documents statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDocumentsStatisticsByStatus() {
        return pieceJustificativeRepository.getDocumentsStatisticsByStatus();
    }
    
    /**
     * Get documents statistics by currency
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDocumentsStatisticsByCurrency() {
        return pieceJustificativeRepository.getDocumentsStatisticsByCurrency();
    }
    
    /**
     * Count documents by project and type
     */
    @Transactional(readOnly = true)
    public List<Object[]> countDocumentsByProjetAndType(Long projetId) {
        return pieceJustificativeRepository.countDocumentsByProjectAndType(projetId);
    }
    
    /**
     * Count documents by project and status
     */
    @Transactional(readOnly = true)
    public List<Object[]> countDocumentsByProjetAndStatus(Long projetId) {
        return pieceJustificativeRepository.countDocumentsByProjectAndStatus(projetId);
    }
    
    /**
     * Get monthly validated expenses summary
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyValidatedExpensesSummary() {
        return pieceJustificativeRepository.getMonthlyValidatedExpensesSummary();
    }
    
    /**
     * Get project expense utilization
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProjectExpenseUtilization() {
        return pieceJustificativeRepository.getProjectExpenseUtilization();
    }
    
    /**
     * Validate supporting document
     */
    public PieceJustificative validatePieceJustificative(Long id, String commentaire) {
        log.info("Validating supporting document with ID: {}", id);
        
        PieceJustificative piece = getPieceJustificativeById(id);
        
        // Validate status transition
        if (!piece.isPrisEnCharge()) {
            throw new IllegalArgumentException("Can only validate documents that are 'PRIS_EN_CHARGE'");
        }
        
        piece.setStatut(StatutPiece.VALIDE);
        piece.setCommentaire(commentaire);
        
        PieceJustificative validatedPiece = pieceJustificativeRepository.save(piece);
        log.info("Supporting document validated successfully: {}", validatedPiece.getNumeroPiece());
        return validatedPiece;
    }
    
    /**
     * Reject supporting document
     */
    public PieceJustificative rejectPieceJustificative(Long id, String commentaire) {
        log.info("Rejecting supporting document with ID: {}", id);
        
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required when rejecting a document");
        }
        
        PieceJustificative piece = getPieceJustificativeById(id);
        
        // Validate status transition
        if (!piece.isPrisEnCharge()) {
            throw new IllegalArgumentException("Can only reject documents that are 'PRIS_EN_CHARGE'");
        }
        
        piece.setStatut(StatutPiece.REJETE);
        piece.setCommentaire(commentaire);
        
        PieceJustificative rejectedPiece = pieceJustificativeRepository.save(piece);
        log.info("Supporting document rejected successfully: {}", rejectedPiece.getNumeroPiece());
        return rejectedPiece;
    }
    
    /**
     * Process supporting document from Comptabilite
     */
    public PieceJustificative processPieceJustificativeFromComptabilite(PieceJustificative piece) {
        log.info("Processing supporting document from Comptabilite: {}", piece.getNumeroPiece());
        
        // Set initial status for Comptabilite documents
        piece.setStatut(StatutPiece.PRIS_EN_CHARGE);
        
        return createPieceJustificative(piece);
    }
    
    /**
     * Calculate available funds for a project
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAvailableFunds(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projetId));
        
        BigDecimal totalAdvances = projet.getTotalAvances();
        BigDecimal totalValidatedExpenses = getTotalValidatedExpensesByProjet(projetId);
        
        return totalAdvances.subtract(totalValidatedExpenses);
    }
    
    /**
     * Validate supporting document business rules
     */
    private void validatePieceJustificativeBusinessRules(PieceJustificative piece) {
        // Additional business validation can be added here
        if (piece.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Document amount must be positive");
        }
        
        if (piece.getDateExecution().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Execution date cannot be in the future");
        }
        
        // Add more business rules as needed
    }
}