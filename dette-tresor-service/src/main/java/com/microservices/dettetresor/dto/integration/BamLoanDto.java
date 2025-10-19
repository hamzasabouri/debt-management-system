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
public class BamLoanDto {
    
    @JsonProperty("loan_number")
    private String loanNumber;
    
    @JsonProperty("signature_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signatureDate;
    
    @JsonProperty("lender_organization")
    private String lenderOrganization;
    
    @JsonProperty("loan_object")
    private String loanObject;
    
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("duration_months")
    private Integer durationMonths;
    
    @JsonProperty("interest_rate")
    private BigDecimal interestRate;
    
    @JsonProperty("current_balance")
    private BigDecimal currentBalance;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate lastUpdated;
}