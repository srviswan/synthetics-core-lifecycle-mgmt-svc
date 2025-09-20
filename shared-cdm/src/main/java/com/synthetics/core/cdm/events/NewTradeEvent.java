package com.synthetics.core.cdm.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * NewTradeEvent representing creation of a new trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewTradeEvent extends LifecycleEvent {
    
    @JsonProperty("product_id")
    private String productId;
    
    @JsonProperty("economic_terms_id")
    private String economicTermsId;
    
    @JsonProperty("parties")
    private List<PartyInfo> parties;
    
    @JsonProperty("lots")
    private List<LotInfo> lots;
    
    @JsonProperty("trade_timestamp")
    private LocalDateTime tradeTimestamp;
    
    public NewTradeEvent(String eventId, String tradeId, LocalDateTime timestamp, String sourceBlotterId,
                        Map<String, Object> metadata, String userId, String reasonCode,
                        String productId, String economicTermsId, List<PartyInfo> parties, 
                        List<LotInfo> lots, LocalDateTime tradeTimestamp) {
        super(eventId, tradeId, timestamp, sourceBlotterId, metadata, userId, reasonCode);
        this.productId = productId;
        this.economicTermsId = economicTermsId;
        this.parties = parties;
        this.lots = lots;
        this.tradeTimestamp = tradeTimestamp;
    }
    
    @Override
    public String getEventType() {
        return "NEW_TRADE";
    }
    
    // Inner classes for party and lot info
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyInfo {
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
        @JsonProperty("lot_id")
        private String lotId;
        
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
