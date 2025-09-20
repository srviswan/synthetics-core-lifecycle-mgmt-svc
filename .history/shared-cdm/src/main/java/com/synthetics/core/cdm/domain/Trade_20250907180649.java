package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Trade entity representing swap trades
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    
    @NotBlank(message = "Trade ID is required")
    @Size(max = 50, message = "Trade ID must not exceed 50 characters")
    @JsonProperty("trade_id")
    private String tradeId;
    
    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID must not exceed 50 characters")
    @JsonProperty("product_id")
    private String productId;
    
    @NotNull(message = "Trade date is required")
    @PastOrPresent(message = "Trade date cannot be in the future")
    @JsonProperty("trade_date")
    private LocalDate tradeDate;
    
    @JsonProperty("trade_time")
    private LocalDateTime tradeTime;
    
    @Builder.Default
    @JsonProperty("status")
    private TradeStatus status = TradeStatus.ACTIVE;
    
    @Size(max = 50, message = "Master agreement ID must not exceed 50 characters")
    @JsonProperty("master_agreement_id")
    private String masterAgreementId;
    
    @JsonProperty("confirmation_method")
    private String confirmationMethod;
    
    @JsonProperty("created_timestamp")
    private LocalDateTime createdTimestamp;
    
    @JsonProperty("updated_timestamp")
    private LocalDateTime updatedTimestamp;
    
    // Enriched fields from related entities
    @JsonProperty("product")
    private TradableProduct product;
    
    @JsonProperty("parties")
    private List<Party> parties;
    
    @JsonProperty("positions")
    private List<Position> positions;
    
    @JsonProperty("economic_terms")
    private EconomicTerms economicTerms;
    
    public enum TradeStatus {
        ACTIVE("Active"),
        TERMINATED("Terminated"),
        SUSPENDED("Suspended");
        
        private final String displayName;
        
        TradeStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
