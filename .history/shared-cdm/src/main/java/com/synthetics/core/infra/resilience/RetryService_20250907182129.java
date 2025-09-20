package com.synthetics.core.infra.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Retry service for resilience patterns
 * Provides retry functionality for external service calls
 */
@Service
@Slf4j
public class RetryService {
    
    private final RetryRegistry retryRegistry;
    
    public RetryService() {
        this.retryRegistry = RetryRegistry.ofDefaults();
        initializeDefaultRetryConfigurations();
    }
    
    /**
     * Execute supplier with retry protection
     * @param retryName The name of the retry configuration
     * @param supplier The supplier to execute
     * @return CompletableFuture containing the result
     */
    public <T> CompletableFuture<T> executeWithRetry(String retryName, Supplier<T> supplier) {
        Retry retry = retryRegistry.retry(retryName);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retry.executeSupplier(supplier);
            } catch (Exception e) {
                log.error("Retry {} failed for operation after all attempts", retryName, e);
                throw new RuntimeException("Retry operation failed", e);
            }
        });
    }
    
    /**
     * Execute async supplier with retry protection
     * @param retryName The name of the retry configuration
     * @param supplier The async supplier to execute
     * @return CompletableFuture containing the result
     */
    public <T> CompletableFuture<T> executeAsyncWithRetry(String retryName, Supplier<CompletableFuture<T>> supplier) {
        Retry retry = retryRegistry.retry(retryName);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retry.executeSupplier(supplier).get();
            } catch (Exception e) {
                log.error("Retry {} failed for async operation after all attempts", retryName, e);
                throw new RuntimeException("Retry async operation failed", e);
            }
        });
    }
    
    /**
     * Get retry metrics
     * @param retryName The name of the retry configuration
     * @return Retry metrics
     */
    public Retry.Metrics getRetryMetrics(String retryName) {
        Retry retry = retryRegistry.retry(retryName);
        return retry.getMetrics();
    }
    
    /**
     * Initialize default retry configurations
     */
    private void initializeDefaultRetryConfigurations() {
        // Reference Data Service Retry
        RetryConfig referenceDataConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .exponentialBackoffMultiplier(2)
            .retryOnException(throwable -> true)
            .build();
        
        retryRegistry.addConfiguration("reference-data", referenceDataConfig);
        
        // Database Retry
        RetryConfig databaseConfig = RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofSeconds(2))
            .exponentialBackoffMultiplier(1.5)
            .retryOnException(throwable -> true)
            .build();
        
        retryRegistry.addConfiguration("database", databaseConfig);
        
        // IBM MQ Retry
        RetryConfig mqConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .exponentialBackoffMultiplier(2)
            .retryOnException(throwable -> true)
            .build();
        
        retryRegistry.addConfiguration("ibm-mq", mqConfig);
        
        // Settlement Service Retry
        RetryConfig settlementConfig = RetryConfig.custom()
            .maxAttempts(4)
            .waitDuration(Duration.ofSeconds(3))
            .exponentialBackoffMultiplier(2)
            .retryOnException(throwable -> true)
            .build();
        
        retryRegistry.addConfiguration("settlement", settlementConfig);
        
        // Cashflow Processing Retry
        RetryConfig cashflowConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofSeconds(5))
            .exponentialBackoffMultiplier(1.5)
            .retryOnException(throwable -> true)
            .build();
        
        retryRegistry.addConfiguration("cashflow-processing", cashflowConfig);
        
        log.info("Initialized default retry configurations: reference-data, database, ibm-mq, settlement, cashflow-processing");
    }
    
    /**
     * Create custom retry configuration
     * @param name The name of the retry configuration
     * @param config The retry configuration
     */
    public void createCustomRetry(String name, RetryConfig config) {
        retryRegistry.addConfiguration(name, config);
        log.info("Created custom retry configuration: {}", name);
    }
}
