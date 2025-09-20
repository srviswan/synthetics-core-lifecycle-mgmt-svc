package com.synthetics.core.infra.config;

import com.synthetics.core.infra.audit.AuditService;
import com.synthetics.core.infra.cache.CacheService;
import com.synthetics.core.infra.metrics.MetricsService;
import com.synthetics.core.infra.reference.MockReferenceDataService;
import com.synthetics.core.infra.reference.RealReferenceDataService;
import com.synthetics.core.infra.reference.ReferenceDataService;
import com.synthetics.core.infra.resilience.CircuitBreakerService;
import com.synthetics.core.infra.resilience.RetryService;
import com.synthetics.core.infra.validation.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for infrastructure services
 * Provides centralized configuration for all infrastructure services
 */
@Configuration
@Slf4j
public class InfrastructureConfiguration {
    
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
    
    /**
     * Mock reference data service bean
     */
    @Bean
    @ConditionalOnProperty(name = "reference-data.service.type", havingValue = "mock", matchIfMissing = true)
    @Primary
    public ReferenceDataService mockReferenceDataService() {
        log.info("Configuring MockReferenceDataService");
        return new MockReferenceDataService();
    }
    
    /**
     * Real reference data service bean
     */
    @Bean
    @ConditionalOnProperty(name = "reference-data.service.type", havingValue = "real")
    @Primary
    public ReferenceDataService realReferenceDataService() {
        log.info("Configuring RealReferenceDataService");
        return new RealReferenceDataService();
    }
}
