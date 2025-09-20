package com.synthetics.core.cdm.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for business and technical metrics
 * Provides centralized metrics collection and reporting
 */
@Service
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    private final AtomicLong totalTradesProcessed = new AtomicLong(0);
    private final AtomicLong totalPositionsProcessed = new AtomicLong(0);
    private final AtomicLong totalCashflowsGenerated = new AtomicLong(0);
    private final AtomicLong totalSettlementInstructions = new AtomicLong(0);
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }
    
    /**
     * Record trade processing metrics
     * @param tradeId The trade ID
     * @param processingTime The processing time
     * @param positionCount The number of positions processed
     * @param cashflowCount The number of cashflows generated
     */
    public void recordTradeProcessing(String tradeId, Duration processingTime, int positionCount, int cashflowCount) {
        // Trade-level metrics
        Counter.builder("trades.processed")
            .tag("trade_id", tradeId)
            .register(meterRegistry)
            .increment();
        
        Timer.builder("trades.processing.time")
            .tag("trade_id", tradeId)
            .register(meterRegistry)
            .record(processingTime);
        
        // Aggregate metrics
        totalTradesProcessed.incrementAndGet();
        totalPositionsProcessed.addAndGet(positionCount);
        totalCashflowsGenerated.addAndGet(cashflowCount);
        
        log.debug("Recorded trade processing metrics: tradeId={}, processingTime={}ms, positions={}, cashflows={}",
            tradeId, processingTime.toMillis(), positionCount, cashflowCount);
    }
    
    /**
     * Record position processing metrics
     * @param positionId The position ID
     * @param processingTime The processing time
     * @param lotCount The number of lots processed
     * @param cashflowCount The number of cashflows generated
     */
    public void recordPositionProcessing(String positionId, Duration processingTime, int lotCount, int cashflowCount) {
        // Position-level metrics
        Counter.builder("positions.processed")
            .tag("position_id", positionId)
            .register(meterRegistry)
            .increment();
        
        Timer.builder("positions.processing.time")
            .tag("position_id", positionId)
            .register(meterRegistry)
            .record(processingTime);
        
        // Lot distribution metrics
        meterRegistry.histogram("positions.lots.count", "position_id", positionId)
            .record(lotCount);
        
        // Cashflow generation metrics
        meterRegistry.histogram("positions.cashflows.count", "position_id", positionId)
            .record(cashflowCount);
        
        log.debug("Recorded position processing metrics: positionId={}, processingTime={}ms, lots={}, cashflows={}",
            positionId, processingTime.toMillis(), lotCount, cashflowCount);
    }
    
    /**
     * Record cashflow generation metrics
     * @param cashflowType The cashflow type
     * @param amount The cashflow amount
     * @param currency The cashflow currency
     */
    public void recordCashflowGeneration(String cashflowType, String amount, String currency) {
        Counter.builder("cashflows.generated")
            .tag("type", cashflowType)
            .tag("currency", currency)
            .register(meterRegistry)
            .increment();
        
        // Amount distribution metrics
        meterRegistry.histogram("cashflows.amount", "type", cashflowType, "currency", currency)
            .record(Double.parseDouble(amount));
        
        totalCashflowsGenerated.incrementAndGet();
        
        log.debug("Recorded cashflow generation metrics: type={}, amount={}, currency={}",
            cashflowType, amount, currency);
    }
    
    /**
     * Record settlement instruction metrics
     * @param settlementReference The settlement reference
     * @param amount The settlement amount
     * @param currency The settlement currency
     * @param status The settlement status
     */
    public void recordSettlementInstruction(String settlementReference, String amount, 
                                          String currency, String status) {
        Counter.builder("settlement.instructions.generated")
            .tag("currency", currency)
            .tag("status", status)
            .register(meterRegistry)
            .increment();
        
        // Settlement amount distribution
        meterRegistry.histogram("settlement.amount", "currency", currency, "status", status)
            .record(Double.parseDouble(amount));
        
        totalSettlementInstructions.incrementAndGet();
        
        log.debug("Recorded settlement instruction metrics: reference={}, amount={}, currency={}, status={}",
            settlementReference, amount, currency, status);
    }
    
    /**
     * Record event processing metrics
     * @param eventType The event type
     * @param processingTime The processing time
     * @param success Whether processing was successful
     */
    public void recordEventProcessing(String eventType, Duration processingTime, boolean success) {
        Counter.builder("events.processed")
            .tag("type", eventType)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
        
        Timer.builder("events.processing.time")
            .tag("type", eventType)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(processingTime);
        
        log.debug("Recorded event processing metrics: type={}, processingTime={}ms, success={}",
            eventType, processingTime.toMillis(), success);
    }
    
    /**
     * Record error metrics
     * @param errorType The error type
     * @param component The component where error occurred
     * @param severity The error severity
     */
    public void recordError(String errorType, String component, String severity) {
        Counter.builder("errors.occurred")
            .tag("type", errorType)
            .tag("component", component)
            .tag("severity", severity)
            .register(meterRegistry)
            .increment();
        
        log.warn("Recorded error metrics: type={}, component={}, severity={}", errorType, component, severity);
    }
    
    /**
     * Record reference data access metrics
     * @param dataType The type of reference data
     * @param accessTime The access time
     * @param success Whether access was successful
     */
    public void recordReferenceDataAccess(String dataType, Duration accessTime, boolean success) {
        Counter.builder("reference.data.access")
            .tag("type", dataType)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
        
        Timer.builder("reference.data.access.time")
            .tag("type", dataType)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(accessTime);
        
        log.debug("Recorded reference data access metrics: type={}, accessTime={}ms, success={}",
            dataType, accessTime.toMillis(), success);
    }
    
    /**
     * Record cache metrics
     * @param cacheName The cache name
     * @param operation The cache operation (hit, miss, put, remove)
     */
    public void recordCacheOperation(String cacheName, String operation) {
        Counter.builder("cache.operations")
            .tag("cache", cacheName)
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();
        
        log.debug("Recorded cache operation metrics: cache={}, operation={}", cacheName, operation);
    }
    
    /**
     * Initialize metrics
     */
    private void initializeMetrics() {
        // Total counters
        meterRegistry.gauge("trades.processed.total", totalTradesProcessed, AtomicLong::get);
        meterRegistry.gauge("positions.processed.total", totalPositionsProcessed, AtomicLong::get);
        meterRegistry.gauge("cashflows.generated.total", totalCashflowsGenerated, AtomicLong::get);
        meterRegistry.gauge("settlement.instructions.total", totalSettlementInstructions, AtomicLong::get);
        
        log.info("Initialized metrics service with total counters");
    }
    
    /**
     * Get total trades processed
     * @return Total number of trades processed
     */
    public long getTotalTradesProcessed() {
        return totalTradesProcessed.get();
    }
    
    /**
     * Get total positions processed
     * @return Total number of positions processed
     */
    public long getTotalPositionsProcessed() {
        return totalPositionsProcessed.get();
    }
    
    /**
     * Get total cashflows generated
     * @return Total number of cashflows generated
     */
    public long getTotalCashflowsGenerated() {
        return totalCashflowsGenerated.get();
    }
    
    /**
     * Get total settlement instructions
     * @return Total number of settlement instructions
     */
    public long getTotalSettlementInstructions() {
        return totalSettlementInstructions.get();
    }
}
