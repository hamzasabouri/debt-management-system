package com.microservices.dettetresor.service.calculation;

import com.microservices.dettetresor.entity.Echeancier;
import com.microservices.dettetresor.entity.Pret;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestCalculationService {
    
    @Value("${loan.penalty.rate:0.05}")
    private BigDecimal penaltyRate; // 5% penalty rate
    
    @Value("${loan.penalty.grace-period:30}")
    private int penaltyGracePeriod; // 30 days grace period
    
    /**
     * Calculate interest for a loan over a specific period
     */
    public BigDecimal calculateInterest(Pret pret, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating interest for loan {} from {} to {}", 
            pret.getNumeroPret(), startDate, endDate);
        
        if (pret.getTauxInteret() == null || pret.getTauxInteret().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Daily interest rate
        BigDecimal dailyRate = pret.getTauxInteret()
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        
        // Simple interest calculation
        BigDecimal interest = pret.getSoldeCourant()
            .multiply(dailyRate)
            .multiply(BigDecimal.valueOf(daysBetween))
            .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Calculated interest: {} for {} days", interest, daysBetween);
        return interest;
    }
    
    /**
     * Calculate compound interest for a loan
     */
    public BigDecimal calculateCompoundInterest(Pret pret, LocalDate startDate, LocalDate endDate, 
                                              int compoundingFrequency) {
        log.debug("Calculating compound interest for loan {} with frequency {}", 
            pret.getNumeroPret(), compoundingFrequency);
        
        if (pret.getTauxInteret() == null || pret.getTauxInteret().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Convert annual rate to decimal
        BigDecimal annualRate = pret.getTauxInteret().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        // Number of years
        BigDecimal years = BigDecimal.valueOf(daysBetween).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        
        // Compound interest formula: A = P(1 + r/n)^(nt) - P
        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(compoundingFrequency), 10, RoundingMode.HALF_UP);
        BigDecimal base = BigDecimal.ONE.add(rate);
        BigDecimal exponent = BigDecimal.valueOf(compoundingFrequency).multiply(years);
        
        // Simplified compound calculation
        BigDecimal compound = pret.getSoldeCourant().multiply(
            BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()))
        ).subtract(pret.getSoldeCourant());
        
        return compound.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate penalty interest for overdue payments
     */
    public BigDecimal calculatePenaltyInterest(Echeancier echeancier, LocalDate currentDate) {
        log.debug("Calculating penalty interest for schedule {}", echeancier.getId());
        
        LocalDate dueDate = echeancier.getDateEcheance();
        LocalDate penaltyStartDate = dueDate.plusDays(penaltyGracePeriod);
        
        if (currentDate.isBefore(penaltyStartDate)) {
            return BigDecimal.ZERO;
        }
        
        long overdueDays = ChronoUnit.DAYS.between(penaltyStartDate, currentDate);
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Daily penalty rate
        BigDecimal dailyPenaltyRate = penaltyRate
            .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        
        // Penalty on the overdue amount
        BigDecimal overdueAmount = echeancier.getMontantTotal(); // Simplified - should subtract paid amount
        BigDecimal penalty = overdueAmount
            .multiply(dailyPenaltyRate)
            .multiply(BigDecimal.valueOf(overdueDays))
            .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Calculated penalty interest: {} for {} overdue days", penalty, overdueDays);
        return penalty;
    }
    
    /**
     * Generate payment schedule with interest calculations
     */
    public List<PaymentScheduleItem> generatePaymentSchedule(Pret pret) {
        log.info("Generating payment schedule for loan: {}", pret.getNumeroPret());
        
        List<PaymentScheduleItem> schedule = new ArrayList<>();
        
        if (pret.getDuree() == null || pret.getDuree() <= 0) {
            log.warn("Invalid loan duration for loan: {}", pret.getNumeroPret());
            return schedule;
        }
        
        BigDecimal monthlyRate = pret.getTauxInteret() != null ? 
            pret.getTauxInteret().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        BigDecimal principal = pret.getMontantTotal();
        LocalDate paymentDate = pret.getDateSignature().plusMonths(1);
        
        // Calculate monthly payment using amortization formula
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyRate, pret.getDuree());
        
        BigDecimal remainingPrincipal = principal;
        
        for (int month = 1; month <= pret.getDuree(); month++) {
            BigDecimal interestPayment = remainingPrincipal.multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            
            // Adjust for last payment
            if (month == pret.getDuree()) {
                principalPayment = remainingPrincipal;
                monthlyPayment = principalPayment.add(interestPayment);
            }
            
            PaymentScheduleItem item = PaymentScheduleItem.builder()
                .paymentNumber(month)
                .paymentDate(paymentDate)
                .principalPayment(principalPayment)
                .interestPayment(interestPayment)
                .totalPayment(monthlyPayment)
                .remainingBalance(remainingPrincipal.subtract(principalPayment))
                .build();
            
            schedule.add(item);
            
            remainingPrincipal = remainingPrincipal.subtract(principalPayment);
            paymentDate = paymentDate.plusMonths(1);
        }
        
        log.info("Generated payment schedule with {} payments for loan: {}", 
            schedule.size(), pret.getNumeroPret());
        return schedule;
    }
    
    /**
     * Calculate monthly payment using amortization formula
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int months) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        
        // M = P [ r(1 + r)^n ] / [ (1 + r)^n – 1]
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = BigDecimal.valueOf(Math.pow(onePlusRate.doubleValue(), months));
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate early payment savings
     */
    public EarlyPaymentCalculation calculateEarlyPaymentSavings(Pret pret, 
                                                              BigDecimal earlyPaymentAmount, 
                                                              LocalDate paymentDate) {
        log.debug("Calculating early payment savings for loan: {}", pret.getNumeroPret());
        
        // Calculate remaining interest without early payment
        LocalDate maturityDate = pret.getDateSignature().plusMonths(pret.getDuree());
        BigDecimal remainingInterest = calculateInterest(pret, paymentDate, maturityDate);
        
        // Calculate new balance after early payment
        BigDecimal newBalance = pret.getSoldeCourant().subtract(earlyPaymentAmount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        
        // Calculate reduced interest
        BigDecimal reducedInterest = newBalance.multiply(pret.getTauxInteret())
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(paymentDate, maturityDate)))
            .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
        
        BigDecimal interestSavings = remainingInterest.subtract(reducedInterest);
        
        return EarlyPaymentCalculation.builder()
            .earlyPaymentAmount(earlyPaymentAmount)
            .newBalance(newBalance)
            .interestSavings(interestSavings)
            .remainingInterest(reducedInterest)
            .totalSavings(interestSavings)
            .newMaturityDate(newBalance.compareTo(BigDecimal.ZERO) == 0 ? paymentDate : maturityDate)
            .build();
    }
    
    /**
     * Calculate grace period interest
     */
    public BigDecimal calculateGracePeriodInterest(Pret pret, int gracePeriodMonths) {
        log.debug("Calculating grace period interest for loan: {} with {} months grace", 
            pret.getNumeroPret(), gracePeriodMonths);
        
        if (pret.getTauxInteret() == null || gracePeriodMonths <= 0) {
            return BigDecimal.ZERO;
        }
        
        LocalDate startDate = pret.getDateSignature();
        LocalDate endDate = startDate.plusMonths(gracePeriodMonths);
        
        return calculateInterest(pret, startDate, endDate);
    }
    
    /**
     * Payment schedule item data class
     */
    @lombok.Data
    @lombok.Builder
    public static class PaymentScheduleItem {
        private int paymentNumber;
        private LocalDate paymentDate;
        private BigDecimal principalPayment;
        private BigDecimal interestPayment;
        private BigDecimal totalPayment;
        private BigDecimal remainingBalance;
    }
    
    /**
     * Early payment calculation data class
     */
    @lombok.Data
    @lombok.Builder
    public static class EarlyPaymentCalculation {
        private BigDecimal earlyPaymentAmount;
        private BigDecimal newBalance;
        private BigDecimal interestSavings;
        private BigDecimal remainingInterest;
        private BigDecimal totalSavings;
        private LocalDate newMaturityDate;
    }
}