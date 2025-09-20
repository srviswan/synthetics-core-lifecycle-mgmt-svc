package com.synthetics.core.cdm.messaging;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for publishing events to message queues
 * Abstracts the underlying messaging implementation (IBM MQ, Solace, etc.)
 */
public interface EventPublisher {
    
    /**
     * Publish event to specified queue
     * @param queueName The name of the queue
     * @param event The event to publish
     */
    void publish(String queueName, Object event);
    
    /**
     * Publish event with correlation ID
     * @param queueName The name of the queue
     * @param correlationId The correlation ID for message tracking
     * @param event The event to publish
     */
    void publishWithCorrelationId(String queueName, String correlationId, Object event);
    
    /**
     * Publish event asynchronously
     * @param queueName The name of the queue
     * @param event The event to publish
     * @return CompletableFuture for async handling
     */
    CompletableFuture<Void> publishAsync(String queueName, Object event);
    
    /**
     * Publish event with retry mechanism
     * @param queueName The name of the queue
     * @param event The event to publish
     * @param maxRetries Maximum number of retry attempts
     */
    void publishWithRetry(String queueName, Object event, int maxRetries);
    
    /**
     * Publish event to dead letter queue
     * @param originalQueueName The original queue name
     * @param event The event that failed
     * @param errorMessage The error message
     */
    void publishToDeadLetterQueue(String originalQueueName, Object event, String errorMessage);
}
