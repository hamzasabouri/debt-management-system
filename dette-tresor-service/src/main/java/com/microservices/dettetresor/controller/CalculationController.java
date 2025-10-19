package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.service.calculation.BalanceCalculationService;
import com.microservices.dettetresor.service.calculation.InterestCalculationService;
import com.microservices.dettetresor.entity.Pret;
import com.microservices.dettetresor.repository.PretRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/calculations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calculation Management", description = "APIs for loan calculations, balance updates, and interest computations")
public class CalculationController {
    
    private final BalanceCalculationService balanceCalculationService;
    private final InterestCalculationService interestCalculationService;
    private final PretRepository pretRepository;
    
    // Balance Calculation Endpoints
    
    @PostMapping("/balance/recalculate/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Recalculate loan balance", 
               description = "Recalculates and updates the current balance for a specific loan")
    public ResponseEntity<Map<String, Object>> recalculateBalance(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId) {
        try {
            balanceCalculationService.calculateLoanBalance(pretId);
            
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Balance recalculated successfully");
            response.put("loanNumber", pret.getNumeroPret());
            response.put("newBalance", pret.getSoldeCourant());
            response.put("currency", pret.getDevise());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recalculating balance for loan {}: {}", pretId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to recalculate balance: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/balance/recalculate-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recalculate all loan balances", 
               description = "Recalculates and updates balances for all loans in the system")
    public ResponseEntity<Map<String, Object>> recalculateAllBalances() {
        try {
            List<Pret> allLoans = pretRepository.findAll();
            int successCount = 0;
            int errorCount = 0;
            
            for (Pret pret : allLoans) {
                try {
                    balanceCalculationService.calculateLoanBalance(pret.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("Error recalculating balance for loan {}: {}", 
                        pret.getNumeroPret(), e.getMessage());
                    errorCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "completed");
            response.put("totalLoans", allLoans.size());
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);
            response.put("message", String.format("Recalculated %d out of %d loans", 
                successCount, allLoans.size()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recalculating all balances: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to recalculate balances: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/balance/overdue-amount")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get total overdue amount", 
               description = "Calculates the total overdue amount across all loans")
    public ResponseEntity<Map<String, Object>> getTotalOverdueAmount() {
        try {
            log.debug("Starting calculation of total overdue amount");
            BigDecimal overdueAmount = balanceCalculationService.calculateTotalOverdueAmount();
            log.debug("Successfully calculated overdue amount: {}", overdueAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalOverdueAmount", overdueAmount);
            response.put("currency", "MAD"); // Default currency
            response.put("calculationDate", LocalDate.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating total overdue amount: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to calculate overdue amount: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/balance/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Get loan statistics", 
               description = "Retrieves comprehensive loan statistics including totals and overdue amounts")
    public ResponseEntity<BalanceCalculationService.LoanStatistics> getLoanStatistics() {
        try {
            BalanceCalculationService.LoanStatistics stats = 
                balanceCalculationService.calculateLoanStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error calculating loan statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Interest Calculation Endpoints
    
    @GetMapping("/interest/calculate/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Calculate interest for a loan", 
               description = "Calculates interest for a specific loan over a given period")
    public ResponseEntity<Map<String, Object>> calculateInterest(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId,
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            BigDecimal interest = interestCalculationService.calculateInterest(pret, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("loanNumber", pret.getNumeroPret());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("interestAmount", interest);
            response.put("currency", pret.getDevise());
            response.put("interestRate", pret.getTauxInteret());
            response.put("currentBalance", pret.getSoldeCourant());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating interest for loan {}: {}", pretId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/interest/compound/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Calculate compound interest", 
               description = "Calculates compound interest for a specific loan")
    public ResponseEntity<Map<String, Object>> calculateCompoundInterest(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId,
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Compounding frequency (times per year)") 
            @RequestParam(defaultValue = "12") int frequency) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            BigDecimal compoundInterest = interestCalculationService
                .calculateCompoundInterest(pret, startDate, endDate, frequency);
            
            Map<String, Object> response = new HashMap<>();
            response.put("loanNumber", pret.getNumeroPret());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("compoundInterest", compoundInterest);
            response.put("compoundingFrequency", frequency);
            response.put("currency", pret.getDevise());
            response.put("interestRate", pret.getTauxInteret());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating compound interest for loan {}: {}", pretId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/interest/schedule/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Generate payment schedule", 
               description = "Generates detailed payment schedule with interest calculations")
    public ResponseEntity<List<InterestCalculationService.PaymentScheduleItem>> generatePaymentSchedule(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            List<InterestCalculationService.PaymentScheduleItem> schedule = 
                interestCalculationService.generatePaymentSchedule(pret);
            
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error generating payment schedule for loan {}: {}", pretId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/interest/early-payment/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Calculate early payment savings", 
               description = "Calculates savings from making an early payment on a loan")
    public ResponseEntity<InterestCalculationService.EarlyPaymentCalculation> calculateEarlyPaymentSavings(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId,
            @Parameter(description = "Early payment amount") 
            @RequestParam BigDecimal paymentAmount,
            @Parameter(description = "Payment date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            InterestCalculationService.EarlyPaymentCalculation calculation = 
                interestCalculationService.calculateEarlyPaymentSavings(pret, paymentAmount, paymentDate);
            
            return ResponseEntity.ok(calculation);
        } catch (Exception e) {
            log.error("Error calculating early payment savings for loan {}: {}", pretId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/interest/grace-period/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Calculate grace period interest", 
               description = "Calculates interest during grace period for a loan")
    public ResponseEntity<Map<String, Object>> calculateGracePeriodInterest(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId,
            @Parameter(description = "Grace period in months") 
            @RequestParam int gracePeriodMonths) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            BigDecimal gracePeriodInterest = interestCalculationService
                .calculateGracePeriodInterest(pret, gracePeriodMonths);
            
            Map<String, Object> response = new HashMap<>();
            response.put("loanNumber", pret.getNumeroPret());
            response.put("gracePeriodMonths", gracePeriodMonths);
            response.put("gracePeriodInterest", gracePeriodInterest);
            response.put("currency", pret.getDevise());
            response.put("startDate", pret.getDateSignature());
            response.put("endDate", pret.getDateSignature().plusMonths(gracePeriodMonths));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating grace period interest for loan {}: {}", pretId, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Utility Endpoints
    
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Health check", description = "Check if the calculation service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "dette-tresor-calculations");
        response.put("timestamp", LocalDateTime.now().toString());
        
        try {
            // Test database connectivity by counting loans
            long loanCount = pretRepository.count();
            response.put("loanCount", loanCount);
            response.put("database", "connected");
        } catch (Exception e) {
            log.error("Database connectivity check failed: {}", e.getMessage());
            response.put("database", "disconnected");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/update-schedules/{pretId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR')")
    @Operation(summary = "Update payment schedule statuses", 
               description = "Updates the status of all payment schedules for a specific loan")
    public ResponseEntity<Map<String, String>> updatePaymentScheduleStatuses(
            @Parameter(description = "Loan ID") 
            @PathVariable Long pretId) {
        try {
            Pret pret = pretRepository.findById(pretId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
            
            balanceCalculationService.updatePaymentScheduleStatuses(pret);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment schedule statuses updated successfully");
            response.put("loanNumber", pret.getNumeroPret());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating payment schedule statuses for loan {}: {}", pretId, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to update statuses: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}