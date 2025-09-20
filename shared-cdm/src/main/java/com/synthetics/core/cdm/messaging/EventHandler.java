package com.synthetics.core.cdm.messaging;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Functional interface for handling events
 */
@FunctionalInterface
public interface EventHandler {
    
    /**
     * Handle the event message
     * @param message The event message
     * @throws EventProcessingException if processing fails
     */
    void handle(EventMessage message) throws EventProcessingException;
    
    /**
     * Event message wrapper
     */
    class EventMessage {
        private String messageId;
        private String correlationId;
        private String queueName;
        private Object payload;
        private Map<String, Object> headers;
        private LocalDateTime timestamp;
        private int deliveryCount;
        
        // Constructors
        public EventMessage() {}
        
        public EventMessage(String messageId, String correlationId, String queueName, 
                          Object payload, Map<String, Object> headers, 
                          LocalDateTime timestamp, int deliveryCount) {
            this.messageId = messageId;
            this.correlationId = correlationId;
            this.queueName = queueName;
            this.payload = payload;
            this.headers = headers;
            this.timestamp = timestamp;
            this.deliveryCount = deliveryCount;
        }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String messageId;
            private String correlationId;
            private String queueName;
            private Object payload;
            private Map<String, Object> headers;
            private LocalDateTime timestamp;
            private int deliveryCount;
            
            public Builder messageId(String messageId) {
                this.messageId = messageId;
                return this;
            }
            
            public Builder correlationId(String correlationId) {
                this.correlationId = correlationId;
                return this;
            }
            
            public Builder queueName(String queueName) {
                this.queueName = queueName;
                return this;
            }
            
            public Builder payload(Object payload) {
                this.payload = payload;
                return this;
            }
            
            public Builder headers(Map<String, Object> headers) {
                this.headers = headers;
                return this;
            }
            
            public Builder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public Builder deliveryCount(int deliveryCount) {
                this.deliveryCount = deliveryCount;
                return this;
            }
            
            public EventMessage build() {
                return new EventMessage(messageId, correlationId, queueName, payload, 
                                      headers, timestamp, deliveryCount);
            }
        }
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
        
        public Map<String, Object> getHeaders() { return headers; }
        public void setHeaders(Map<String, Object> headers) { this.headers = headers; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public int getDeliveryCount() { return deliveryCount; }
        public void setDeliveryCount(int deliveryCount) { this.deliveryCount = deliveryCount; }
    }
}



