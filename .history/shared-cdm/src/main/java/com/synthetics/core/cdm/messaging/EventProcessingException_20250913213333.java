package com.synthetics.core.cdm.messaging;

/**
 * Exception thrown when event processing fails
 */
public class EventProcessingException extends Exception {
    
    private final String eventId;
    private final String queueName;
    private final int retryCount;
    
    public EventProcessingException(String message) {
        super(message);
        this.eventId = null;
        this.queueName = null;
        this.retryCount = 0;
    }
    
    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.eventId = null;
        this.queueName = null;
        this.retryCount = 0;
    }
    
    public EventProcessingException(String message, String eventId, String queueName) {
        super(message);
        this.eventId = eventId;
        this.queueName = queueName;
        this.retryCount = 0;
    }
    
    public EventProcessingException(String message, String eventId, String queueName, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
        this.queueName = queueName;
        this.retryCount = 0;
    }
    
    public EventProcessingException(String message, String eventId, String queueName, int retryCount, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
        this.queueName = queueName;
        this.retryCount = retryCount;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
}

