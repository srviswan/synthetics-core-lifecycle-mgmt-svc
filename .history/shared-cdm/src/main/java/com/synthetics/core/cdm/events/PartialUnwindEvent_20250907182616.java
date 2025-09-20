package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PartialUnwindEvent representing partial unwind of a trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartialUnwindEvent extends LifecycleEvent {
    
    @JsonProperty("unwind_id")
    private String unwindId;
    
    @JsonProperty("lots_to_remove")
    private List<String> lotsToRemove;
    
    @JsonProperty("notional_reduction")
    private String notionalReduction;
    
    public PartialUnwindEvent(String eventId, String tradeId, LocalDateTime timestamp, String eventType,
                            String unwindId, List<String> lotsToRemove, String notionalReduction) {
        super(eventId, tradeId, timestamp, eventType);
        this.unwindId = unwindId;
        this.lotsToRemove = lotsToRemove;
        this.notionalReduction = notionalReduction;
    }
}
