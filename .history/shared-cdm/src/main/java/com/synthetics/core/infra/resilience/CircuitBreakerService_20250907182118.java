package com.synthetics.core.infra.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Circuit breaker service for resilience patterns
 * Provides circuit breaker functionality for external service calls
 */
@Service
@Slf4j
public class CircuitBreakerService {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public CircuitBreakerService() {
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        initializeDefaultCircuitBreakers();
    }
    
    /**
     * Execute supplier with circuit breaker protection
     * @param circuitBreakerName The name of the circuit breaker
     * @param supplier The supplier to execute
     * @return CompletableFuture containing the result
     */
    public <T> CompletableFuture<T> executeWithCircuitBreaker(String circuitBreakerName, Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return circuitBreaker.executeSupplier(supplier);
            } catch (Exception e) {
                log.error("Circuit breaker {} failed for operation", circuitBreakerName, e);
                throw new RuntimeException("Circuit breaker operation failed", e);
            }
        });
    }
    
    /**
     * Execute async supplier with circuit breaker protection
     * @param circuitBreakerName The name of the circuit breaker
     * @param supplier The async supplier to execute
     * @return CompletableFuture containing the result
     */
    public <T> CompletableFuture<T> executeAsyncWithCircuitBreaker(String circuitBreakerName, Supplier<CompletableFuture<T>> supplier) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return circuitBreaker.executeSupplier(supplier).get();
            } catch (Exception e) {
                log.error("Circuit breaker {} failed for async operation", circuitBreakerName, e);
                throw new RuntimeException("Circuit breaker async operation failed", e);
            }
        });
    }
    
    /**
     * Get circuit breaker status
     * @param circuitBreakerName The name of the circuit breaker
     * @return Circuit breaker state
     */
    public CircuitBreaker.State getCircuitBreakerState(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        return circuitBreaker.getState();
    }
    
    /**
     * Get circuit breaker metrics
     * @param circuitBreakerName The name of the circuit breaker
     * @return Circuit breaker metrics
     */
    public CircuitBreaker.Metrics getCircuitBreakerMetrics(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        return circuitBreaker.getMetrics();
    }
    
    /**
     * Initialize default circuit breakers
     */
    private void initializeDefaultCircuitBreakers() {
        // Reference Data Service Circuit Breaker
        CircuitBreakerConfig referenceDataConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();
        
        circuitBreakerRegistry.addConfiguration("reference-data", referenceDataConfig);
        
        // Database Circuit Breaker
        CircuitBreakerConfig databaseConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(60)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(20)
            .minimumNumberOfCalls(10)
            .permittedNumberOfCallsInHalfOpenState(5)
            .build();
        
        circuitBreakerRegistry.addConfiguration("database", databaseConfig);
        
        // IBM MQ Circuit Breaker
        CircuitBreakerConfig mqConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(40)
            .waitDurationInOpenState(Duration.ofSeconds(45))
            .slidingWindowSize(15)
            .minimumNumberOfCalls(8)
            .permittedNumberOfCallsInHalfOpenState(4)
            .build();
        
        circuitBreakerRegistry.addConfiguration("ibm-mq", mqConfig);
        
        // Settlement Service Circuit Breaker
        CircuitBreakerConfig settlementConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(70)
            .waitDurationInOpenState(Duration.ofSeconds(120))
            .slidingWindowSize(25)
            .minimumNumberOfCalls(15)
            .permittedNumberOfCallsInHalfOpenState(8)
            .build();
        
        circuitBreakerRegistry.addConfiguration("settlement", settlementConfig);
        
        log.info("Initialized default circuit breakers: reference-data, database, ibm-mq, settlement");
    }
    
    /**
     * Create custom circuit breaker
     * @param name The name of the circuit breaker
     * @param config The circuit breaker configuration
     */
    public void createCustomCircuitBreaker(String name, CircuitBreakerConfig config) {
        circuitBreakerRegistry.addConfiguration(name, config);
        log.info("Created custom circuit breaker: {}", name);
    }
}
