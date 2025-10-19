package com.microservices.dettetresor.dto.integration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtfePaymentDto {
    
    @JsonProperty("payment_order_number")
    private String paymentOrderNumber;
    
    @JsonProperty("loan_reference")
    private String loanReference;
    
    @JsonProperty("payment_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("payment_type")
    private String paymentType;
    
    @JsonProperty("beneficiary")
    private String beneficiary;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("treasury_account")
    private String treasuryAccount;
    
    @JsonProperty("execution_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate executionDate;
    
    @JsonProperty("reference_document")
    private String referenceDocument;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DtfeBalanceDto {
    
    @JsonProperty("loan_reference")
    private String loanReference;
    
    @JsonProperty("current_balance")
    private BigDecimal currentBalance;
    
    @JsonProperty("outstanding_principal")
    private BigDecimal outstandingPrincipal;
    
    @JsonProperty("outstanding_interest")
    private BigDecimal outstandingInterest;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate lastUpdated;
    
    @JsonProperty("next_payment_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextPaymentDate;
}