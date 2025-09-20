package com.synthetics.core.infra.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Audit service for tracking operations and changes
 * Provides centralized audit logging for compliance and debugging
 */
@Service
@Slf4j
public class AuditService {
    
    /**
     * Log operation start
     * @param operation The operation name
     * @param entityId The entity ID
     * @param userId The user ID
     * @param metadata Additional metadata
     */
    public void logOperationStart(String operation, String entityId, String userId, Map<String, Object> metadata) {
        String auditId = UUID.randomUUID().toString();
        
        log.info("AUDIT_START: operation={}, entityId={}, userId={}, auditId={}, timestamp={}, metadata={}",
            operation, entityId, userId, auditId, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log operation completion
     * @param operation The operation name
     * @param entityId The entity ID
     * @param userId The user ID
     * @param success Whether the operation was successful
     * @param metadata Additional metadata
     */
    public void logOperationComplete(String operation, String entityId, String userId, 
                                   boolean success, Map<String, Object> metadata) {
        log.info("AUDIT_COMPLETE: operation={}, entityId={}, userId={}, success={}, timestamp={}, metadata={}",
            operation, entityId, userId, success, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log operation failure
     * @param operation The operation name
     * @param entityId The entity ID
     * @param userId The user ID
     * @param error The error message
     * @param metadata Additional metadata
     */
    public void logOperationFailure(String operation, String entityId, String userId, 
                                  String error, Map<String, Object> metadata) {
        log.error("AUDIT_FAILURE: operation={}, entityId={}, userId={}, error={}, timestamp={}, metadata={}",
            operation, entityId, userId, error, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log data change
     * @param entityType The type of entity
     * @param entityId The entity ID
     * @param changeType The type of change (CREATE, UPDATE, DELETE)
     * @param userId The user ID
     * @param oldValue The old value (for updates)
     * @param newValue The new value
     * @param metadata Additional metadata
     */
    public void logDataChange(String entityType, String entityId, String changeType, 
                            String userId, Object oldValue, Object newValue, Map<String, Object> metadata) {
        log.info("AUDIT_DATA_CHANGE: entityType={}, entityId={}, changeType={}, userId={}, " +
                "oldValue={}, newValue={}, timestamp={}, metadata={}",
            entityType, entityId, changeType, userId, oldValue, newValue, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log cashflow generation
     * @param tradeId The trade ID
     * @param positionId The position ID
     * @param cashflowCount The number of cashflows generated
     * @param userId The user ID
     * @param metadata Additional metadata
     */
    public void logCashflowGeneration(String tradeId, String positionId, int cashflowCount, 
                                   String userId, Map<String, Object> metadata) {
        log.info("AUDIT_CASHFLOW_GENERATION: tradeId={}, positionId={}, cashflowCount={}, " +
                "userId={}, timestamp={}, metadata={}",
            tradeId, positionId, cashflowCount, userId, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log settlement instruction
     * @param cashflowId The cashflow ID
     * @param settlementReference The settlement reference
     * @param amount The settlement amount
     * @param currency The settlement currency
     * @param userId The user ID
     * @param metadata Additional metadata
     */
    public void logSettlementInstruction(String cashflowId, String settlementReference, 
                                       String amount, String currency, String userId, 
                                       Map<String, Object> metadata) {
        log.info("AUDIT_SETTLEMENT_INSTRUCTION: cashflowId={}, settlementReference={}, " +
                "amount={}, currency={}, userId={}, timestamp={}, metadata={}",
            cashflowId, settlementReference, amount, currency, userId, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log event processing
     * @param eventId The event ID
     * @param eventType The event type
     * @param processingTime The processing time in milliseconds
     * @param success Whether processing was successful
     * @param metadata Additional metadata
     */
    public void logEventProcessing(String eventId, String eventType, long processingTime, 
                                 boolean success, Map<String, Object> metadata) {
        log.info("AUDIT_EVENT_PROCESSING: eventId={}, eventType={}, processingTime={}ms, " +
                "success={}, timestamp={}, metadata={}",
            eventId, eventType, processingTime, success, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log performance metrics
     * @param operation The operation name
     * @param duration The operation duration in milliseconds
     * @param recordCount The number of records processed
     * @param metadata Additional metadata
     */
    public void logPerformanceMetrics(String operation, long duration, int recordCount, 
                                    Map<String, Object> metadata) {
        log.info("AUDIT_PERFORMANCE: operation={}, duration={}ms, recordCount={}, " +
                "timestamp={}, metadata={}",
            operation, duration, recordCount, LocalDateTime.now(), metadata);
    }
    
    /**
     * Log security event
     * @param eventType The security event type
     * @param userId The user ID
     * @param resource The resource accessed
     * @param success Whether access was successful
     * @param metadata Additional metadata
     */
    public void logSecurityEvent(String eventType, String userId, String resource, 
                               boolean success, Map<String, Object> metadata) {
        log.warn("AUDIT_SECURITY: eventType={}, userId={}, resource={}, success={}, " +
                "timestamp={}, metadata={}",
            eventType, userId, resource, success, LocalDateTime.now(), metadata);
    }
}
