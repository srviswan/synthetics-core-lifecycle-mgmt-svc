package com.synthetics.core.cashflow.service;

import com.synthetics.core.cdm.domain.Cashflow;
import com.synthetics.core.cdm.domain.Position;
import com.synthetics.core.cdm.domain.Trade;
import com.synthetics.core.infra.audit.AuditService;
import com.synthetics.core.infra.cache.CacheService;
import com.synthetics.core.infra.metrics.MetricsService;
import com.synthetics.core.infra.reference.ReferenceDataService;
import com.synthetics.core.infra.validation.ValidationService;
import com.synthetics.core.cashflow.service.InterestNotionalService;
import com.synthetics.core.cashflow.service.ParallelProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic cashflow generation engine
 * Pure function of (position + reference data) at a specified valuation time
 */
@Service
@Slf4j
public class CashflowEngine {
    
    private final ReferenceDataService referenceDataService;
    private final ValidationService validationService;
    private final AuditService auditService;
    private final MetricsService metricsService;
    private final CacheService cacheService;
    private final InterestNotionalService interestNotionalService;
    
    public CashflowEngine(ReferenceDataService referenceDataService,
                         ValidationService validationService,
                         AuditService auditService,
                         MetricsService metricsService,
                         CacheService cacheService,
                         InterestNotionalService interestNotionalService) {
        this.referenceDataService = referenceDataService;
        this.validationService = validationService;
        this.auditService = auditService;
        this.metricsService = metricsService;
        this.cacheService = cacheService;
        this.interestNotionalService = interestNotionalService;
    }
    
    /**
     * Generate cashflows for multiple trades in parallel
     * @param trades List of trades to process
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @param parallelProcessor The parallel processor to use
     * @return List of generated cashflows from all trades
     */
    public java.util.concurrent.CompletableFuture<List<Cashflow>> generateCashflowsParallel(
            java.util.List<Trade> trades, LocalDate valuationDate, String originEventId,
            ParallelProcessor parallelProcessor) {

        log.info("Starting parallel cashflow generation for {} trades", trades.size());

        // Use parallel processor for multiple trades
        return parallelProcessor.processTrades(trades, valuationDate, originEventId)
            .whenComplete((cashflows, throwable) -> {
                if (throwable != null) {
                    log.error("Error in parallel cashflow generation", throwable);
                    auditService.logOperationFailure("PARALLEL_CASHFLOW_GENERATION", "batch-" + originEventId,
                        "SYSTEM", throwable.getMessage(),
                        java.util.Map.of("tradeCount", trades.size(), "error", throwable.getClass().getSimpleName()));
                } else {
                    log.info("Successfully completed parallel cashflow generation for {} trades, generated {} cashflows",
                        trades.size(), cashflows.size());
                    auditService.logOperationComplete("PARALLEL_CASHFLOW_GENERATION", "batch-" + originEventId,
                        "SYSTEM", true, java.util.Map.of("tradeCount", trades.size(), "cashflowCount", cashflows.size()));
                }
            });
    }

    /**
     * Generate cashflows for a trade
     * @param trade The trade to generate cashflows for
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @return List of generated cashflows
     */
    public List<Cashflow> generateCashflows(Trade trade, LocalDate valuationDate, String originEventId) {
        log.info("Generating cashflows for trade: {} on valuation date: {}", trade.getTradeId(), valuationDate);
        
        // Audit operation start
        auditService.logOperationStart("CASHFLOW_GENERATION", trade.getTradeId(), "SYSTEM", 
            java.util.Map.of("valuationDate", valuationDate, "originEventId", originEventId));
        
        try {
            // Validate trade and cashflow generation rules
            List<String> validationErrors = validationService.validateTrade(trade);
            validationErrors.addAll(validationService.validateCashflowGeneration(trade, valuationDate));
            
            if (validationService.hasErrors(validationErrors)) {
                validationService.logValidationErrors(validationErrors, "CashflowEngine.generateCashflows");
                throw new IllegalArgumentException("Validation failed: " + String.join(", ", validationErrors));
            }
            
            List<Cashflow> cashflows = new ArrayList<>();

            // Use sequential processing for now (parallel processing available via separate endpoint)
            for (Position position : trade.getPositions()) {
                List<Cashflow> positionCashflows = generatePositionCashflows(position, valuationDate, originEventId);
                cashflows.addAll(positionCashflows);
            }
            
            // Record metrics
            metricsService.recordTradeProcessing(trade.getTradeId(), 
                java.time.Duration.ofMillis(System.currentTimeMillis()), 
                trade.getPositions().size(), cashflows.size());
            
            // Audit operation completion
            auditService.logOperationComplete("CASHFLOW_GENERATION", trade.getTradeId(), "SYSTEM", true,
                java.util.Map.of("cashflowCount", cashflows.size(), "positionCount", trade.getPositions().size()));
            
            log.info("Generated {} cashflows for trade: {}", cashflows.size(), trade.getTradeId());
            return cashflows;
            
        } catch (Exception e) {
            // Audit operation failure
            auditService.logOperationFailure("CASHFLOW_GENERATION", trade.getTradeId(), "SYSTEM", 
                e.getMessage(), java.util.Map.of("error", e.getClass().getSimpleName()));
            
            // Record error metrics
            metricsService.recordError("CASHFLOW_GENERATION_ERROR", "CashflowEngine", "HIGH");
            
            throw e;
        }
    }
    
    /**
     * Generate cashflows for a position
     * @param position The position to generate cashflows for
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @return List of generated cashflows
     */
    public List<Cashflow> generatePositionCashflows(Position position, LocalDate valuationDate, String originEventId) {
        log.debug("Generating cashflows for position: {} on valuation date: {}", position.getPositionId(), valuationDate);
        
        List<Cashflow> cashflows = new ArrayList<>();
        
        // Generate different types of cashflows based on position type and terms
        cashflows.addAll(generateAccrualCashflows(position, valuationDate, originEventId));
        cashflows.addAll(generateInterestCashflows(position, valuationDate, originEventId));
        cashflows.addAll(generateFeeCashflows(position, valuationDate, originEventId));
        cashflows.addAll(generateDividendCashflows(position, valuationDate, originEventId));
        cashflows.addAll(generatePrincipalCashflows(position, valuationDate, originEventId));
        
        log.debug("Generated {} cashflows for position: {}", cashflows.size(), position.getPositionId());
        return cashflows;
    }
    
    /**
     * Generate accrual cashflows
     */
    private List<Cashflow> generateAccrualCashflows(Position position, LocalDate valuationDate, String originEventId) {
        List<Cashflow> cashflows = new ArrayList<>();
        
        // Calculate accrual based on position notional and time period
        BigDecimal accrualAmount = calculateAccrualAmount(position, valuationDate);
        
        if (accrualAmount.compareTo(BigDecimal.ZERO) != 0) {
            Cashflow accrualCashflow = Cashflow.builder()
                .cashflowId(UUID.randomUUID().toString())
                .tradeId(position.getTradeId())
                .positionId(position.getPositionId())
                .cashflowType(Cashflow.CashflowType.ACCRUAL)
                .payerPartyId(getPayerPartyId(position))
                .receiverPartyId(getReceiverPartyId(position))
                .currency(position.getNotionalCurrency())
                .amount(accrualAmount)
                .settlementDate(calculateSettlementDate(valuationDate))
                .paymentType(Cashflow.PaymentType.ACCRUAL)
                .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
                .originEventId(originEventId)
                .revisionNumber(1)
                .isCancelled(false)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
            
            cashflows.add(accrualCashflow);
        }
        
        return cashflows;
    }
    
    /**
     * Generate interest cashflows with conditional calculation logic
     */
    private List<Cashflow> generateInterestCashflows(Position position, LocalDate valuationDate, String originEventId) {
        List<Cashflow> cashflows = new ArrayList<>();

        // Use the new conditional interest calculation logic
        InterestNotionalService.InterestCalculationResult calculationResult =
            interestNotionalService.getInterestCalculationResult(position.getPositionId(), position.getLots());

        BigDecimal interestAmount = calculationResult.getAmount();

        log.debug("Interest calculation for position {}: {} using method {} - {}",
                  position.getPositionId(), interestAmount, calculationResult.getMethod(),
                  calculationResult.getDescription());

        if (interestAmount.compareTo(BigDecimal.ZERO) != 0) {
            Cashflow interestCashflow = Cashflow.builder()
                .cashflowId(UUID.randomUUID().toString())
                .tradeId(position.getTradeId())
                .positionId(position.getPositionId())
                .cashflowType(Cashflow.CashflowType.INTEREST)
                .payerPartyId(getPayerPartyId(position))
                .receiverPartyId(getReceiverPartyId(position))
                .currency(position.getNotionalCurrency())
                .amount(interestAmount)
                .settlementDate(calculateSettlementDate(valuationDate))
                .paymentType(Cashflow.PaymentType.ACCRUAL)
                .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
                .originEventId(originEventId)
                .revisionNumber(1)
                .isCancelled(false)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();

            cashflows.add(interestCashflow);
        }

        return cashflows;
    }
    
    /**
     * Generate fee cashflows
     */
    private List<Cashflow> generateFeeCashflows(Position position, LocalDate valuationDate, String originEventId) {
        List<Cashflow> cashflows = new ArrayList<>();
        
        // Calculate fees based on position notional and fee rate
        BigDecimal feeAmount = calculateFeeAmount(position, valuationDate);
        
        if (feeAmount.compareTo(BigDecimal.ZERO) != 0) {
            Cashflow feeCashflow = Cashflow.builder()
                .cashflowId(UUID.randomUUID().toString())
                .tradeId(position.getTradeId())
                .positionId(position.getPositionId())
                .cashflowType(Cashflow.CashflowType.FEE)
                .payerPartyId(getPayerPartyId(position))
                .receiverPartyId(getReceiverPartyId(position))
                .currency(position.getNotionalCurrency())
                .amount(feeAmount)
                .settlementDate(calculateSettlementDate(valuationDate))
                .paymentType(Cashflow.PaymentType.FEE)
                .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
                .originEventId(originEventId)
                .revisionNumber(1)
                .isCancelled(false)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
            
            cashflows.add(feeCashflow);
        }
        
        return cashflows;
    }
    
    /**
     * Generate dividend cashflows
     */
    private List<Cashflow> generateDividendCashflows(Position position, LocalDate valuationDate, String originEventId) {
        List<Cashflow> cashflows = new ArrayList<>();
        
        // Calculate dividends based on position notional and dividend rate
        BigDecimal dividendAmount = calculateDividendAmount(position, valuationDate);
        
        if (dividendAmount.compareTo(BigDecimal.ZERO) != 0) {
            Cashflow dividendCashflow = Cashflow.builder()
                .cashflowId(UUID.randomUUID().toString())
                .tradeId(position.getTradeId())
                .positionId(position.getPositionId())
                .cashflowType(Cashflow.CashflowType.DIVIDEND)
                .payerPartyId(getPayerPartyId(position))
                .receiverPartyId(getReceiverPartyId(position))
                .currency(position.getNotionalCurrency())
                .amount(dividendAmount)
                .settlementDate(calculateSettlementDate(valuationDate))
                .paymentType(Cashflow.PaymentType.DIVIDEND)
                .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
                .originEventId(originEventId)
                .revisionNumber(1)
                .isCancelled(false)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
            
            cashflows.add(dividendCashflow);
        }
        
        return cashflows;
    }
    
    /**
     * Generate principal cashflows
     */
    private List<Cashflow> generatePrincipalCashflows(Position position, LocalDate valuationDate, String originEventId) {
        List<Cashflow> cashflows = new ArrayList<>();
        
        // Calculate principal based on position notional
        BigDecimal principalAmount = calculatePrincipalAmount(position, valuationDate);
        
        if (principalAmount.compareTo(BigDecimal.ZERO) != 0) {
            Cashflow principalCashflow = Cashflow.builder()
                .cashflowId(UUID.randomUUID().toString())
                .tradeId(position.getTradeId())
                .positionId(position.getPositionId())
                .cashflowType(Cashflow.CashflowType.PRINCIPAL)
                .payerPartyId(getPayerPartyId(position))
                .receiverPartyId(getReceiverPartyId(position))
                .currency(position.getNotionalCurrency())
                .amount(principalAmount)
                .settlementDate(calculateSettlementDate(valuationDate))
                .paymentType(Cashflow.PaymentType.PRINCIPAL)
                .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
                .originEventId(originEventId)
                .revisionNumber(1)
                .isCancelled(false)
                .createdTimestamp(LocalDateTime.now())
                .updatedTimestamp(LocalDateTime.now())
                .build();
            
            cashflows.add(principalCashflow);
        }
        
        return cashflows;
    }
    
    // Helper methods for calculations
    private BigDecimal calculateAccrualAmount(Position position, LocalDate valuationDate) {
        // Simplified accrual calculation - in real implementation, this would use reference data
        return position.getNotionalAmount().multiply(new BigDecimal("0.01"));
    }
    
    private BigDecimal calculateInterestAmount(Position position, LocalDate valuationDate) {
        // Simplified interest calculation - in real implementation, this would use reference data
        return position.getNotionalAmount().multiply(new BigDecimal("0.02"));
    }
    
    private BigDecimal calculateFeeAmount(Position position, LocalDate valuationDate) {
        // Simplified fee calculation - in real implementation, this would use reference data
        return position.getNotionalAmount().multiply(new BigDecimal("0.001"));
    }
    
    private BigDecimal calculateDividendAmount(Position position, LocalDate valuationDate) {
        // Simplified dividend calculation - in real implementation, this would use reference data
        return position.getNotionalAmount().multiply(new BigDecimal("0.005"));
    }
    
    private BigDecimal calculatePrincipalAmount(Position position, LocalDate valuationDate) {
        // Principal amount is the notional amount
        return position.getNotionalAmount();
    }
    
    private String getPayerPartyId(Position position) {
        // Simplified - in real implementation, this would be determined by position type and terms
        return "BANK001";
    }
    
    private String getReceiverPartyId(Position position) {
        // Simplified - in real implementation, this would be determined by position type and terms
        return "CLIENT001";
    }
    
    private LocalDate calculateSettlementDate(LocalDate valuationDate) {
        // Simplified settlement date calculation - in real implementation, this would use business calendar
        return valuationDate.plusDays(2);
    }
}
