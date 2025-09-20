package com.synthetics.core.cdm.config;

import com.synthetics.core.cdm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for shared services
 * Provides centralized configuration for all shared services
 */
@Configuration
@Slf4j
public class SharedServicesConfiguration {
    
    /**
     * Circuit breaker service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerService circuitBreakerService() {
        log.info("Configuring CircuitBreakerService");
        return new CircuitBreakerService();
    }
    
    /**
     * Retry service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryService retryService() {
        log.info("Configuring RetryService");
        return new RetryService();
    }
    
    /**
     * Cache service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        log.info("Configuring CacheService");
        return new CacheService();
    }
    
    /**
     * Validation service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidationService validationService() {
        log.info("Configuring ValidationService");
        return new ValidationService();
    }
    
    /**
     * Audit service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService() {
        log.info("Configuring AuditService");
        return new AuditService();
    }
    
    /**
     * Metrics service bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MetricsService metricsService(io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        log.info("Configuring MetricsService");
        return new MetricsService(meterRegistry);
    }
}
