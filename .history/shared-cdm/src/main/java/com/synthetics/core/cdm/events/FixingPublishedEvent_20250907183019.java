package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * FixingPublishedEvent representing fixing publication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FixingPublishedEvent extends LifecycleEvent {
    
    @JsonProperty("index_id")
    private String indexId;
    
    @JsonProperty("fixing_date")
    private LocalDate fixingDate;
    
    @JsonProperty("fixing_time")
    private LocalDateTime fixingTime;
    
    @JsonProperty("fixing_value")
    private Double fixingValue;
    
    public FixingPublishedEvent(String eventId, String tradeId, LocalDateTime timestamp, String eventType,
                              String indexId, LocalDate fixingDate, LocalDateTime fixingTime, Double fixingValue) {
        super(eventId, tradeId, timestamp, eventType);
        this.indexId = indexId;
        this.fixingDate = fixingDate;
        this.fixingTime = fixingTime;
        this.fixingValue = fixingValue;
    }
}
