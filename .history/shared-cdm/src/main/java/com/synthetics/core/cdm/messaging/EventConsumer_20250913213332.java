package com.synthetics.core.cdm.messaging;

/**
 * Interface for consuming events from message queues
 * Abstracts the underlying messaging implementation (IBM MQ, Solace, etc.)
 */
public interface EventConsumer {
    
    /**
     * Subscribe to queue with event handler
     * @param queueName The name of the queue
     * @param handler The event handler
     */
    void subscribe(String queueName, EventHandler handler);
    
    /**
     * Subscribe with message selector
     * @param queueName The name of the queue
     * @param selector The message selector expression
     * @param handler The event handler
     */
    void subscribeWithSelector(String queueName, String selector, EventHandler handler);
    
    /**
     * Subscribe with batch processing
     * @param queueName The name of the queue
     * @param handler The event handler
     * @param batchSize The batch size for processing
     */
    void subscribeWithBatch(String queueName, EventHandler handler, int batchSize);
    
    /**
     * Unsubscribe from queue
     * @param queueName The name of the queue
     */
    void unsubscribe(String queueName);
    
    /**
     * Get queue depth
     * @param queueName The name of the queue
     * @return The current queue depth
     */
    int getQueueDepth(String queueName);
}

