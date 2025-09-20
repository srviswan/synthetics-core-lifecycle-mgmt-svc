package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base class for all lifecycle events
 * Based on event sourcing pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event_type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NewTradeEvent.class, name = "NEW_TRADE"),
    @JsonSubTypes.Type(value = AmendmentEvent.class, name = "AMENDMENT"),
    @JsonSubTypes.Type(value = PartialUnwindEvent.class, name = "PARTIAL_UNWIND"),
    @JsonSubTypes.Type(value = TerminationEvent.class, name = "TERMINATION"),
    @JsonSubTypes.Type(value = FixingPublishedEvent.class, name = "FIXING_PUBLISHED")
})
public abstract class LifecycleEvent {
    
    @NotBlank(message = "Event ID is required")
    @Size(max = 50, message = "Event ID must not exceed 50 characters")
    @JsonProperty("event_id")
    private String eventId;
    
    @NotBlank(message = "Trade ID is required")
    @Size(max = 50, message = "Trade ID must not exceed 50 characters")
    @JsonProperty("trade_id")
    private String tradeId;
    
    @NotNull(message = "Timestamp is required")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @Size(max = 50, message = "Source blotter ID must not exceed 50 characters")
    @JsonProperty("source_blotter_id")
    private String sourceBlotterId;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("reason_code")
    private String reasonCode;
    
    /**
     * Get the event type for this lifecycle event
     */
    public abstract String getEventType();
}
