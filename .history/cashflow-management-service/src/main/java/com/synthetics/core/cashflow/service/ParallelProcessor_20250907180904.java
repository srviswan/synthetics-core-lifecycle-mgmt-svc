package com.synthetics.core.cashflow.service;

import com.synthetics.core.cdm.domain.Cashflow;
import com.synthetics.core.cdm.domain.Position;
import com.synthetics.core.cdm.domain.Trade;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Parallel processor for cashflow generation
 * Implements contract-level and position-level parallelism
 */
@Component
@Slf4j
public class ParallelProcessor {
    
    private final CashflowEngine cashflowEngine;
    private final ThreadPoolExecutor tradeProcessor;
    private final ThreadPoolExecutor positionProcessor;
    private final MeterRegistry meterRegistry;
    private final AtomicLong processedTrades = new AtomicLong(0);
    private final AtomicLong processedPositions = new AtomicLong(0);
    
    public ParallelProcessor(CashflowEngine cashflowEngine, MeterRegistry meterRegistry,
                           @Value("${thread-pools.trade-processor.core-size:20}") int tradeCoreSize,
                           @Value("${thread-pools.trade-processor.max-size:50}") int tradeMaxSize,
                           @Value("${thread-pools.position-processor.core-size:100}") int positionCoreSize,
                           @Value("${thread-pools.position-processor.max-size:200}") int positionMaxSize) {
        this.cashflowEngine = cashflowEngine;
        this.meterRegistry = meterRegistry;
        
        // Initialize trade processor thread pool
        this.tradeProcessor = new ThreadPoolExecutor(
            tradeCoreSize,
            tradeMaxSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "trade-processor-" + System.currentTimeMillis())
        );
        
        // Initialize position processor thread pool
        this.positionProcessor = new ThreadPoolExecutor(
            positionCoreSize,
            positionMaxSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5000),
            r -> new Thread(r, "position-processor-" + System.currentTimeMillis())
        );
        
        initializeMetrics();
    }
    
    /**
     * Process multiple trades in parallel
     * @param trades List of trades to process
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @return CompletableFuture containing all generated cashflows
     */
    public CompletableFuture<List<Cashflow>> processTrades(List<Trade> trades, LocalDate valuationDate, String originEventId) {
        log.info("Starting parallel processing of {} trades", trades.size());
        LocalDateTime startTime = LocalDateTime.now();
        
        List<CompletableFuture<List<Cashflow>>> tradeFutures = trades.stream()
            .map(trade -> processTradeAsync(trade, valuationDate, originEventId))
            .toList();
        
        return CompletableFuture.allOf(tradeFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<Cashflow> allCashflows = new ArrayList<>();
                for (CompletableFuture<List<Cashflow>> future : tradeFutures) {
                    try {
                        allCashflows.addAll(future.get());
                    } catch (Exception e) {
                        log.error("Error processing trade future", e);
                    }
                }
                
                Duration processingTime = Duration.between(startTime, LocalDateTime.now());
                log.info("Completed parallel processing of {} trades in {} ms, generated {} cashflows", 
                    trades.size(), processingTime.toMillis(), allCashflows.size());
                
                // Record metrics
                meterRegistry.counter("trades.processed", "batch_size", String.valueOf(trades.size()))
                    .increment(trades.size());
                meterRegistry.timer("trades.processing.time", "batch_size", String.valueOf(trades.size()))
                    .record(processingTime);
                
                return allCashflows;
            });
    }
    
    /**
     * Process a single trade asynchronously
     * @param trade The trade to process
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @return CompletableFuture containing generated cashflows
     */
    private CompletableFuture<List<Cashflow>> processTradeAsync(Trade trade, LocalDate valuationDate, String originEventId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Processing trade: {} with {} positions", trade.getTradeId(), trade.getPositions().size());
            LocalDateTime startTime = LocalDateTime.now();
            
            try {
                // Process positions in parallel within the trade
                List<CompletableFuture<List<Cashflow>>> positionFutures = trade.getPositions().stream()
                    .map(position -> processPositionAsync(position, valuationDate, originEventId))
                    .toList();
                
                CompletableFuture<List<Cashflow>> allPositionFutures = CompletableFuture.allOf(
                    positionFutures.toArray(new CompletableFuture[0])
                ).thenApply(v -> {
                    List<Cashflow> tradeCashflows = new ArrayList<>();
                    for (CompletableFuture<List<Cashflow>> future : positionFutures) {
                        try {
                            tradeCashflows.addAll(future.get());
                        } catch (Exception e) {
                            log.error("Error processing position future for trade: {}", trade.getTradeId(), e);
                        }
                    }
                    return tradeCashflows;
                });
                
                List<Cashflow> result = allPositionFutures.get();
                
                Duration processingTime = Duration.between(startTime, LocalDateTime.now());
                log.debug("Completed processing trade: {} in {} ms, generated {} cashflows", 
                    trade.getTradeId(), processingTime.toMillis(), result.size());
                
                // Record trade-level metrics
                meterRegistry.counter("trades.processed", "trade_id", trade.getTradeId())
                    .increment();
                meterRegistry.timer("trades.processing.time", "trade_id", trade.getTradeId())
                    .record(processingTime);
                
                processedTrades.incrementAndGet();
                
                return result;
                
            } catch (Exception e) {
                log.error("Error processing trade: {}", trade.getTradeId(), e);
                throw new RuntimeException("Failed to process trade: " + trade.getTradeId(), e);
            }
        }, tradeProcessor);
    }
    
    /**
     * Process a single position asynchronously
     * @param position The position to process
     * @param valuationDate The valuation date
     * @param originEventId The origin event ID
     * @return CompletableFuture containing generated cashflows
     */
    private CompletableFuture<List<Cashflow>> processPositionAsync(Position position, LocalDate valuationDate, String originEventId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Processing position: {} with {} lots", position.getPositionId(), position.getLots().size());
            LocalDateTime startTime = LocalDateTime.now();
            
            try {
                List<Cashflow> cashflows = cashflowEngine.generatePositionCashflows(position, valuationDate, originEventId);
                
                Duration processingTime = Duration.between(startTime, LocalDateTime.now());
                log.debug("Completed processing position: {} in {} ms, generated {} cashflows", 
                    position.getPositionId(), processingTime.toMillis(), cashflows.size());
                
                // Record position-level metrics
                meterRegistry.counter("positions.processed", "position_id", position.getPositionId())
                    .increment();
                meterRegistry.timer("positions.processing.time", "position_id", position.getPositionId())
                    .record(processingTime);
                meterRegistry.histogram("positions.per.trade", "trade_id", position.getTradeId())
                    .record(position.getLots().size());
                
                processedPositions.incrementAndGet();
                
                return cashflows;
                
            } catch (Exception e) {
                log.error("Error processing position: {}", position.getPositionId(), e);
                throw new RuntimeException("Failed to process position: " + position.getPositionId(), e);
            }
        }, positionProcessor);
    }
    
    /**
     * Initialize metrics for thread pools
     */
    private void initializeMetrics() {
        // Trade processor metrics
        meterRegistry.gauge("threadpool.trade.active", tradeProcessor, ThreadPoolExecutor::getActiveCount);
        meterRegistry.gauge("threadpool.trade.core", tradeProcessor, ThreadPoolExecutor::getCorePoolSize);
        meterRegistry.gauge("threadpool.trade.max", tradeProcessor, ThreadPoolExecutor::getMaximumPoolSize);
        meterRegistry.gauge("threadpool.trade.queue", tradeProcessor, executor -> executor.getQueue().size());
        
        // Position processor metrics
        meterRegistry.gauge("threadpool.position.active", positionProcessor, ThreadPoolExecutor::getActiveCount);
        meterRegistry.gauge("threadpool.position.core", positionProcessor, ThreadPoolExecutor::getCorePoolSize);
        meterRegistry.gauge("threadpool.position.max", positionProcessor, ThreadPoolExecutor::getMaximumPoolSize);
        meterRegistry.gauge("threadpool.position.queue", positionProcessor, executor -> executor.getQueue().size());
        
        // Processing counters
        meterRegistry.gauge("trades.processed.total", processedTrades, AtomicLong::get);
        meterRegistry.gauge("positions.processed.total", processedPositions, AtomicLong::get);
    }
    
    /**
     * Get thread pool status for health checks
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        return ThreadPoolStatus.builder()
            .tradeProcessorActive(tradeProcessor.getActiveCount())
            .tradeProcessorCore(tradeProcessor.getCorePoolSize())
            .tradeProcessorMax(tradeProcessor.getMaximumPoolSize())
            .tradeProcessorQueue(tradeProcessor.getQueue().size())
            .positionProcessorActive(positionProcessor.getActiveCount())
            .positionProcessorCore(positionProcessor.getCorePoolSize())
            .positionProcessorMax(positionProcessor.getMaximumPoolSize())
            .positionProcessorQueue(positionProcessor.getQueue().size())
            .build();
    }
    
    /**
     * Shutdown thread pools gracefully
     */
    public void shutdown() {
        log.info("Shutting down parallel processor thread pools");
        
        tradeProcessor.shutdown();
        positionProcessor.shutdown();
        
        try {
            if (!tradeProcessor.awaitTermination(30, TimeUnit.SECONDS)) {
                tradeProcessor.shutdownNow();
            }
            if (!positionProcessor.awaitTermination(30, TimeUnit.SECONDS)) {
                positionProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            tradeProcessor.shutdownNow();
            positionProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("Parallel processor thread pools shutdown complete");
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ThreadPoolStatus {
        private int tradeProcessorActive;
        private int tradeProcessorCore;
        private int tradeProcessorMax;
        private int tradeProcessorQueue;
        private int positionProcessorActive;
        private int positionProcessorCore;
        private int positionProcessorMax;
        private int positionProcessorQueue;
    }
}
