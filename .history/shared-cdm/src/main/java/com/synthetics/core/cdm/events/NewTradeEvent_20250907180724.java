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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event representing a new trade creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewTradeEvent extends LifecycleEvent {
    
    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID must not exceed 50 characters")
    @JsonProperty("product_id")
    private String productId;
    
    @NotBlank(message = "Economic terms ID is required")
    @Size(max = 50, message = "Economic terms ID must not exceed 50 characters")
    @JsonProperty("economic_terms_id")
    private String economicTermsId;
    
    @NotNull(message = "Parties are required")
    @JsonProperty("parties")
    private List<PartyInfo> parties;
    
    @NotNull(message = "Lots are required")
    @JsonProperty("lots")
    private List<LotInfo> lots;
    
    @NotNull(message = "Trade timestamp is required")
    @JsonProperty("trade_timestamp")
    private LocalDateTime tradeTimestamp;
    
    @Override
    public String getEventType() {
        return "NEW_TRADE";
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyInfo {
        @NotBlank(message = "Party ID is required")
        @JsonProperty("party_id")
        private String partyId;
        
        @JsonProperty("party_role")
        private String partyRole;
        
        @JsonProperty("party_name")
        private String partyName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LotInfo {
        @NotBlank(message = "Lot ID is required")
        @JsonProperty("lot_id")
        private String lotId;
        
        @NotBlank(message = "Underlier ID is required")
        @JsonProperty("underlier_id")
        private String underlierId;
        
        @JsonProperty("quantity")
        private java.math.BigDecimal quantity;
        
        @JsonProperty("unit_price")
        private java.math.BigDecimal unitPrice;
        
        @JsonProperty("currency")
        private String currency;
    }
}
