package com.synthetics.core.cdm.config;

import com.synthetics.core.cdm.service.MockReferenceDataService;
import com.synthetics.core.cdm.service.RealReferenceDataService;
import com.synthetics.core.cdm.service.ReferenceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for reference data service
 * Supports both mock and real implementations
 */
@Configuration
@Slf4j
public class ReferenceDataConfiguration {
    
    /**
     * Mock reference data service bean
     * Used when reference-data.service.type=mock
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
     * Used when reference-data.service.type=real
     */
    @Bean
    @ConditionalOnProperty(name = "reference-data.service.type", havingValue = "real")
    @Primary
    public ReferenceDataService realReferenceDataService() {
        log.info("Configuring RealReferenceDataService");
        return new RealReferenceDataService();
    }
}
