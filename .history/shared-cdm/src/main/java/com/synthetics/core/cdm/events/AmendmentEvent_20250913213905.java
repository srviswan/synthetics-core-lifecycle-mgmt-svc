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
 * AmendmentEvent representing amendment of an existing trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AmendmentEvent extends LifecycleEvent {
    
    @JsonProperty("amendment_id")
    private String amendmentId;
    
    @JsonProperty("changed_fields")
    private Map<String, Object> changedFields;
    
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;
    
    @JsonProperty("revised_lots")
    private List<RevisedLotInfo> revisedLots;
    
    @JsonProperty("amendment_reason")
    private String amendmentReason;
    
    public AmendmentEvent(String eventId, String tradeId, LocalDateTime timestamp, String sourceBlotterId,
                        Map<String, Object> metadata, String userId, String reasonCode,
                        String amendmentId, Map<String, Object> changedFields, LocalDate effectiveDate,
                        List<RevisedLotInfo> revisedLots, String amendmentReason) {
        super(eventId, tradeId, timestamp, sourceBlotterId, metadata, userId, reasonCode);
        this.amendmentId = amendmentId;
        this.changedFields = changedFields;
        this.effectiveDate = effectiveDate;
        this.revisedLots = revisedLots;
        this.amendmentReason = amendmentReason;
    }
    
    @Override
    public String getEventType() {
        return "AMENDMENT";
    }
    
    // Inner class for revised lot info
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevisedLotInfo {
        @JsonProperty("lot_id")
        private String lotId;
        
        @JsonProperty("action")
        private String action; // ADD, MODIFY, REMOVE
        
        @JsonProperty("new_quantity")
        private java.math.BigDecimal newQuantity;
        
        @JsonProperty("new_unit_price")
        private java.math.BigDecimal newUnitPrice;
        
        @JsonProperty("new_currency")
        private String newCurrency;
    }
}
