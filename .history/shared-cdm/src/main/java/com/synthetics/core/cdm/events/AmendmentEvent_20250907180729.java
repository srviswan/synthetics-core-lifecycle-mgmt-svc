package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Event representing a trade amendment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AmendmentEvent extends LifecycleEvent {
    
    @NotBlank(message = "Amendment ID is required")
    @Size(max = 50, message = "Amendment ID must not exceed 50 characters")
    @JsonProperty("amendment_id")
    private String amendmentId;
    
    @NotNull(message = "Changed fields are required")
    @JsonProperty("changed_fields")
    private Map<String, Object> changedFields;
    
    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;
    
    @JsonProperty("revised_lots")
    private List<RevisedLotInfo> revisedLots;
    
    @JsonProperty("amendment_reason")
    private String amendmentReason;
    
    @Override
    public String getEventType() {
        return "AMENDMENT";
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevisedLotInfo {
        @NotBlank(message = "Lot ID is required")
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
