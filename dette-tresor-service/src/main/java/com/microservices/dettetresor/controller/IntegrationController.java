package com.microservices.dettetresor.controller;

import com.microservices.dettetresor.dto.integration.BamLoanDto;
import com.microservices.dettetresor.dto.integration.DtfePaymentDto;
import com.microservices.dettetresor.service.integration.BamIntegrationService;
import com.microservices.dettetresor.service.integration.DtfeIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dette-tresor/integration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Integration Management", description = "APIs for managing external integrations with BAM and DTFE")
public class IntegrationController {
    
    private final BamIntegrationService bamIntegrationService;
    private final DtfeIntegrationService dtfeIntegrationService;
    
    // BAM Integration Endpoints
    
    @PostMapping("/bam/sync/{loanNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Synchronize loan data with BAM", 
               description = "Synchronizes specific loan data with Bank Al-Maghrib system")
    public ResponseEntity<Map<String, String>> syncLoanWithBam(
            @Parameter(description = "Loan number to synchronize") 
            @PathVariable String loanNumber) {
        try {
            bamIntegrationService.synchronizeLoanData(loanNumber);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Loan synchronized with BAM successfully");
            response.put("loanNumber", loanNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error synchronizing loan {} with BAM: {}", loanNumber, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to synchronize with BAM: " + e.getMessage());
            response.put("loanNumber", loanNumber);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/bam/loan/{loanNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Fetch loan data from BAM", 
               description = "Retrieves loan information from Bank Al-Maghrib system")
    public ResponseEntity<BamLoanDto> fetchLoanFromBam(
            @Parameter(description = "Loan number to fetch") 
            @PathVariable String loanNumber) {
        try {
            BamLoanDto bamLoan = bamIntegrationService.fetchLoanFromBam(loanNumber);
            if (bamLoan != null) {
                return ResponseEntity.ok(bamLoan);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching loan {} from BAM: {}", loanNumber, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/bam/loans")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Fetch all loans from BAM for a period", 
               description = "Retrieves all loans from Bank Al-Maghrib system for specified date range")
    public ResponseEntity<List<BamLoanDto>> fetchAllLoansFromBam(
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam LocalDateTime fromDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam LocalDateTime toDate) {
        try {
            List<BamLoanDto> loans = bamIntegrationService.fetchAllLoansFromBam(fromDate, toDate);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            log.error("Error fetching loans from BAM: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/bam/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Test BAM connectivity", 
               description = "Tests connection to Bank Al-Maghrib system")
    public ResponseEntity<Map<String, String>> testBamConnectivity() {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isConnected = bamIntegrationService.testBamConnectivity();
            response.put("service", "BAM");
            response.put("status", isConnected ? "connected" : "disconnected");
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("service", "BAM");
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // DTFE Integration Endpoints
    
    @PostMapping("/dtfe/sync-payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Synchronize payment statuses with DTFE", 
               description = "Synchronizes all pending payment statuses with DTFE system")
    public ResponseEntity<Map<String, String>> syncPaymentsWithDtfe() {
        try {
            dtfeIntegrationService.synchronizePaymentStatuses();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment statuses synchronized with DTFE successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error synchronizing payments with DTFE: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to synchronize with DTFE: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/dtfe/payment/{paymentOrderNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Fetch payment confirmation from DTFE", 
               description = "Retrieves payment confirmation from DTFE system")
    public ResponseEntity<DtfePaymentDto> fetchPaymentFromDtfe(
            @Parameter(description = "Payment order number") 
            @PathVariable String paymentOrderNumber) {
        try {
            DtfePaymentDto payment = dtfeIntegrationService.fetchPaymentConfirmation(paymentOrderNumber);
            if (payment != null) {
                return ResponseEntity.ok(payment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching payment {} from DTFE: {}", paymentOrderNumber, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/dtfe/payment/{paymentOrderNumber}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Check payment status in DTFE", 
               description = "Checks payment status in DTFE system")
    public ResponseEntity<Map<String, String>> checkPaymentStatus(
            @Parameter(description = "Payment order number") 
            @PathVariable String paymentOrderNumber) {
        try {
            String status = dtfeIntegrationService.checkPaymentStatus(paymentOrderNumber);
            Map<String, String> response = new HashMap<>();
            response.put("paymentOrderNumber", paymentOrderNumber);
            response.put("status", status);
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking payment status {} in DTFE: {}", paymentOrderNumber, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("paymentOrderNumber", paymentOrderNumber);
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/dtfe/treasury/balance/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Get treasury balance from DTFE", 
               description = "Retrieves treasury account balance from DTFE system")
    public ResponseEntity<Map<String, String>> getTreasuryBalance(
            @Parameter(description = "Treasury account number") 
            @PathVariable String accountNumber) {
        try {
            String balance = dtfeIntegrationService.getTreasuryBalance(accountNumber);
            Map<String, String> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("balance", balance);
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching treasury balance {} from DTFE: {}", accountNumber, e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/dtfe/budget/notification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Send budget notification to DTFE", 
               description = "Sends budget notification to DTFE system")
    public ResponseEntity<Map<String, String>> sendBudgetNotification(
            @Parameter(description = "Loan reference") 
            @RequestParam String loanReference,
            @Parameter(description = "Budget line") 
            @RequestParam String budgetLine,
            @Parameter(description = "Amount") 
            @RequestParam String amount) {
        try {
            dtfeIntegrationService.sendBudgetNotification(loanReference, budgetLine, amount);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Budget notification sent to DTFE successfully");
            response.put("loanReference", loanReference);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending budget notification to DTFE: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send budget notification: " + e.getMessage());
            response.put("loanReference", loanReference);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/dtfe/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Test DTFE connectivity", 
               description = "Tests connection to DTFE system")
    public ResponseEntity<Map<String, String>> testDtfeConnectivity() {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isConnected = dtfeIntegrationService.testDtfeConnectivity();
            response.put("service", "DTFE");
            response.put("status", isConnected ? "connected" : "disconnected");
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("service", "DTFE");
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Combined Health Check
    
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DETTE_DU_TRESOR') or hasRole('DETTE_INTERIEUR')")
    @Operation(summary = "Test all external integrations", 
               description = "Tests connectivity to all external systems (BAM and DTFE)")
    public ResponseEntity<Map<String, Object>> testAllIntegrations() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> bamStatus = new HashMap<>();
        Map<String, String> dtfeStatus = new HashMap<>();
        
        try {
            // Test BAM
            boolean bamConnected = bamIntegrationService.testBamConnectivity();
            bamStatus.put("status", bamConnected ? "connected" : "disconnected");
            bamStatus.put("service", "BAM");
            
            // Test DTFE
            boolean dtfeConnected = dtfeIntegrationService.testDtfeConnectivity();
            dtfeStatus.put("status", dtfeConnected ? "connected" : "disconnected");
            dtfeStatus.put("service", "DTFE");
            
            response.put("bam", bamStatus);
            response.put("dtfe", dtfeStatus);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("overall", (bamConnected && dtfeConnected) ? "healthy" : "degraded");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("overall", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}