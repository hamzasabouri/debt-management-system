package com.microservices.dettetresor.service.calculation;

import com.microservices.dettetresor.entity.*;
import com.microservices.dettetresor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceCalculationService {
    
    private final PretRepository pretRepository;
    private final EcheancierRepository echeancierRepository;
    private final OrdrePaiementRepository ordrePaiementRepository;
    private final AvisCreditRepository avisCreditRepository;
    private final AvisDebitRepository avisDebitRepository;
    
    /**
     * Calculate and update loan balance based on payments and credits
     */
    @Transactional
    public void calculateLoanBalance(Long pretId) {
        log.debug("Calculating balance for loan ID: {}", pretId);
        
        Pret pret = pretRepository.findById(pretId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + pretId));
        
        // Calculate total credits received
        BigDecimal totalCredits = calculateTotalCredits(pret);
        
        // Calculate total payments made
        BigDecimal totalPayments = calculateTotalPayments(pret);
        
        // Calculate total debits
        BigDecimal totalDebits = calculateTotalDebits(pret);
        
        // New balance = Total Amount - Total Credits + Total Debits + Accrued Interest
        BigDecimal accruedInterest = calculateAccruedInterest(pret);
        BigDecimal newBalance = pret.getMontantTotal()
            .subtract(totalCredits)
            .add(totalDebits)
            .add(accruedInterest);
        
        // Ensure balance is not negative
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        
        pret.setSoldeCourant(newBalance);
        pretRepository.save(pret);
        
        log.info("Updated balance for loan {}: {} {}", 
            pret.getNumeroPret(), newBalance, pret.getDevise());
        
        // Update payment schedule statuses
        updatePaymentScheduleStatuses(pret);
    }
    
    /**
     * Calculate total credits received for a loan
     */
    private BigDecimal calculateTotalCredits(Pret pret) {
        List<AvisCredit> credits = avisCreditRepository.findByPret(pret);
        return credits.stream()
            .map(AvisCredit::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total payments made for a loan
     */
    private BigDecimal calculateTotalPayments(Pret pret) {
        List<OrdrePaiement> payments = ordrePaiementRepository.findByPretAndStatut(pret, OrdrePaiement.StatutOrdrePaiement.PAYE);
        return payments.stream()
            .map(OrdrePaiement::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total debits for a loan
     */
    private BigDecimal calculateTotalDebits(Pret pret) {
        // Get all payment orders for this loan
        List<OrdrePaiement> paymentOrders = ordrePaiementRepository.findByPretAndStatut(
            pret, OrdrePaiement.StatutOrdrePaiement.PAYE);
        
        BigDecimal totalDebits = BigDecimal.ZERO;
        
        // For each payment order, get its debit advices and sum amounts
        for (OrdrePaiement ordre : paymentOrders) {
            List<AvisDebit> avisDebits = avisDebitRepository.findAllByOrdrePaiementId(ordre.getId());
            for (AvisDebit avisDebit : avisDebits) {
                totalDebits = totalDebits.add(avisDebit.getMontant());
            }
        }
        
        return totalDebits;
    }
    
    /**
     * Calculate accrued interest for a loan
     */
    private BigDecimal calculateAccruedInterest(Pret pret) {
        if (pret.getTauxInteret() == null || pret.getTauxInteret().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        LocalDate startDate = pret.getDateSignature();
        LocalDate currentDate = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(startDate, currentDate);
        
        // Simple interest calculation: Principal * Rate * Time / 365
        BigDecimal dailyRate = pret.getTauxInteret()
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        
        BigDecimal accruedInterest = pret.getSoldeCourant()
            .multiply(dailyRate)
            .multiply(BigDecimal.valueOf(daysBetween))
            .setScale(2, RoundingMode.HALF_UP);
        
        return accruedInterest;
    }
    
    /**
     * Update payment schedule statuses based on current date and payments
     */
    @Transactional
    public void updatePaymentScheduleStatuses(Pret pret) {
        log.debug("Updating payment schedule statuses for loan: {}", pret.getNumeroPret());
        
        List<Echeancier> schedules = echeancierRepository.findByPretOrderByDateEcheance(pret);
        LocalDate currentDate = LocalDate.now();
        
        for (Echeancier schedule : schedules) {
            Echeancier.StatutEcheance oldStatus = schedule.getStatut();
            Echeancier.StatutEcheance newStatus = determineScheduleStatus(schedule, currentDate);
            
            if (!oldStatus.equals(newStatus)) {
                schedule.setStatut(newStatus);
                echeancierRepository.save(schedule);
                log.debug("Updated schedule {} status from {} to {}", 
                    schedule.getId(), oldStatus, newStatus);
            }
        }
    }
    
    /**
     * Determine the appropriate status for a payment schedule
     */
    private Echeancier.StatutEcheance determineScheduleStatus(Echeancier schedule, LocalDate currentDate) {
        LocalDate dueDate = schedule.getDateEcheance();
        BigDecimal totalDue = schedule.getMontantTotal();
        
        // Calculate total payments made for this schedule
        BigDecimal totalPaid = calculatePaymentsForSchedule(schedule);
        
        // If fully paid
        if (totalPaid.compareTo(totalDue) >= 0) {
            return Echeancier.StatutEcheance.PAYE;
        }
        
        // If partially paid
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            return Echeancier.StatutEcheance.PARTIELLEMENT_PAYE;
        }
        
        // If overdue
        if (currentDate.isAfter(dueDate)) {
            return Echeancier.StatutEcheance.EN_RETARD;
        }
        
        // Default to planned
        return Echeancier.StatutEcheance.PREVU;
    }
    
    /**
     * Calculate total payments made for a specific schedule
     */
    private BigDecimal calculatePaymentsForSchedule(Echeancier schedule) {
        try {
            // Find payments that match this schedule's date and loan
            List<OrdrePaiement> payments = ordrePaiementRepository.findByPretAndDateEmissionAndStatut(
                schedule.getPret(), schedule.getDateEcheance(), OrdrePaiement.StatutOrdrePaiement.PAYE);
            
            return payments.stream()
                .map(OrdrePaiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            log.warn("Error calculating payments for schedule ID {}: {}", schedule.getId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate overdue amounts for all loans
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalOverdueAmount() {
        log.debug("Calculating total overdue amount");
        
        try {
            LocalDate currentDate = LocalDate.now();
            log.debug("Current date for overdue calculation: {}", currentDate);
            
            List<Echeancier> overdueSchedules = echeancierRepository
                .findByDateEcheanceBeforeAndStatutIn(
                    currentDate, 
                    List.of(Echeancier.StatutEcheance.PREVU, Echeancier.StatutEcheance.PARTIELLEMENT_PAYE)
                );
            
            log.debug("Found {} overdue schedules", overdueSchedules.size());
            
            BigDecimal totalOverdue = BigDecimal.ZERO;
            
            for (Echeancier schedule : overdueSchedules) {
                try {
                    BigDecimal totalDue = schedule.getMontantTotal();
                    BigDecimal totalPaid = calculatePaymentsForSchedule(schedule);
                    BigDecimal overdue = totalDue.subtract(totalPaid);
                    
                    if (overdue.compareTo(BigDecimal.ZERO) > 0) {
                        totalOverdue = totalOverdue.add(overdue);
                    }
                } catch (Exception e) {
                    log.warn("Error processing schedule ID {}: {}", schedule.getId(), e.getMessage());
                    // Continue with other schedules
                }
            }
            
            log.info("Total overdue amount calculated: {}", totalOverdue);
            return totalOverdue;
        } catch (Exception e) {
            log.error("Error in calculateTotalOverdueAmount: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate overdue amount: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process automatic payment from credit advice
     */
    @Transactional
    public void processAutomaticPayment(AvisCredit avisCredit) {
        log.info("Processing automatic payment from credit advice: {}", avisCredit.getNumeroAvis());
        
        Pret pret = avisCredit.getPret();
        
        // Find the next unpaid schedule
        List<Echeancier> unpaidSchedules = echeancierRepository.findByPretAndStatutInOrderByDateEcheance(
            pret, List.of(Echeancier.StatutEcheance.PREVU, Echeancier.StatutEcheance.PARTIELLEMENT_PAYE));
        
        BigDecimal remainingAmount = avisCredit.getMontant();
        
        for (Echeancier schedule : unpaidSchedules) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal totalDue = schedule.getMontantTotal();
            BigDecimal totalPaid = calculatePaymentsForSchedule(schedule);
            BigDecimal amountDue = totalDue.subtract(totalPaid);
            
            if (amountDue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paymentAmount = remainingAmount.min(amountDue);
                
                // Create automatic payment order
                createAutomaticPaymentOrder(pret, schedule, paymentAmount, avisCredit);
                
                remainingAmount = remainingAmount.subtract(paymentAmount);
            }
        }
        
        // Recalculate loan balance
        calculateLoanBalance(pret.getId());
    }
    
    /**
     * Create automatic payment order from credit advice
     */
    private void createAutomaticPaymentOrder(Pret pret, Echeancier schedule, 
                                           BigDecimal amount, AvisCredit avisCredit) {
        OrdrePaiement ordre = OrdrePaiement.builder()
            .numeroOrdre("AUTO-" + avisCredit.getNumeroAvis() + "-" + System.currentTimeMillis())
            .pret(pret)
            .dateEmission(schedule.getDateEcheance())
            .montant(amount)
            .devise(pret.getDevise())
            .echeance(schedule.getDateEcheance())
            .statut(OrdrePaiement.StatutOrdrePaiement.PAYE)
            .build();
        
        ordrePaiementRepository.save(ordre);
        log.info("Created automatic payment order: {} for amount: {}", 
            ordre.getNumeroOrdre(), amount);
    }
    
    /**
     * Scheduled task to update all loan balances daily
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    @Async
    public void scheduledBalanceUpdate() {
        log.info("Starting scheduled balance update for all loans");
        
        List<Pret> allLoans = pretRepository.findAll();
        int updatedCount = 0;
        
        for (Pret pret : allLoans) {
            try {
                calculateLoanBalance(pret.getId());
                updatedCount++;
            } catch (Exception e) {
                log.error("Error updating balance for loan {}: {}", 
                    pret.getNumeroPret(), e.getMessage());
            }
        }
        
        log.info("Completed scheduled balance update. Updated {} loans", updatedCount);
    }
    
    /**
     * Scheduled task to update payment schedule statuses
     */
    @Scheduled(cron = "0 30 1 * * ?") // Run daily at 1:30 AM
    @Async
    public void scheduledStatusUpdate() {
        log.info("Starting scheduled status update for all payment schedules");
        
        List<Pret> allLoans = pretRepository.findAll();
        int updatedCount = 0;
        
        for (Pret pret : allLoans) {
            try {
                updatePaymentScheduleStatuses(pret);
                updatedCount++;
            } catch (Exception e) {
                log.error("Error updating statuses for loan {}: {}", 
                    pret.getNumeroPret(), e.getMessage());
            }
        }
        
        log.info("Completed scheduled status update. Updated {} loans", updatedCount);
    }
    
    /**
     * Calculate loan statistics
     */
    @Transactional(readOnly = true)
    public LoanStatistics calculateLoanStatistics() {
        log.debug("Calculating loan statistics");
        
        List<Pret> allLoans = pretRepository.findAll();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal totalOverdue = calculateTotalOverdueAmount();
        int activeLoans = 0;
        
        for (Pret pret : allLoans) {
            totalAmount = totalAmount.add(pret.getMontantTotal());
            totalBalance = totalBalance.add(pret.getSoldeCourant());
            
            if (pret.getSoldeCourant().compareTo(BigDecimal.ZERO) > 0) {
                activeLoans++;
            }
        }
        
        return LoanStatistics.builder()
            .totalLoans(allLoans.size())
            .activeLoans(activeLoans)
            .totalAmount(totalAmount)
            .totalBalance(totalBalance)
            .totalOverdue(totalOverdue)
            .build();
    }
    
    /**
     * Loan statistics data class
     */
    @lombok.Data
    @lombok.Builder
    public static class LoanStatistics {
        private int totalLoans;
        private int activeLoans;
        private BigDecimal totalAmount;
        private BigDecimal totalBalance;
        private BigDecimal totalOverdue;
    }
}