package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * TerminationEvent representing termination of a trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TerminationEvent extends LifecycleEvent {
    
    @JsonProperty("termination_id")
    private String terminationId;
    
    @JsonProperty("termination_date")
    private LocalDate terminationDate;
    
    @JsonProperty("termination_cashflows")
    private List<String> terminationCashflows;
    
    public TerminationEvent(String eventId, String tradeId, LocalDateTime timestamp, String sourceBlotterId,
                          Map<String, Object> metadata, String userId, String reasonCode,
                          String terminationId, LocalDate terminationDate, List<String> terminationCashflows) {
        super(eventId, tradeId, timestamp, sourceBlotterId, metadata, userId, reasonCode);
        this.terminationId = terminationId;
        this.terminationDate = terminationDate;
        this.terminationCashflows = terminationCashflows;
    }
    
    @Override
    public String getEventType() {
        return "TERMINATION";
    }
}
