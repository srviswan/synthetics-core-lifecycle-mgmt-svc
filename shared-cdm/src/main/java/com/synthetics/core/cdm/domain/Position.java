package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Position entity representing positions within a trade
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    
    @NotBlank(message = "Position ID is required")
    @Size(max = 50, message = "Position ID must not exceed 50 characters")
    @JsonProperty("position_id")
    private String positionId;
    
    @NotBlank(message = "Trade ID is required")
    @Size(max = 50, message = "Trade ID must not exceed 50 characters")
    @JsonProperty("trade_id")
    private String tradeId;
    
    @NotBlank(message = "Underlier ID is required")
    @Size(max = 50, message = "Underlier ID must not exceed 50 characters")
    @JsonProperty("underlier_id")
    private String underlierId;
    
    @NotNull(message = "Notional amount is required")
    @JsonProperty("notional_amount")
    private BigDecimal notionalAmount;
    
    @NotBlank(message = "Notional currency is required")
    @Size(min = 3, max = 3, message = "Notional currency must be 3 characters")
    @JsonProperty("notional_currency")
    private String notionalCurrency;
    
    @JsonProperty("position_type")
    private PositionType positionType;
    
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;
    
    @JsonProperty("termination_date")
    private LocalDate terminationDate;
    
    @JsonProperty("created_timestamp")
    private LocalDateTime createdTimestamp;
    
    @JsonProperty("updated_timestamp")
    private LocalDateTime updatedTimestamp;
    
    // Enriched fields
    @JsonProperty("underlier")
    private Underlier underlier;
    
    @JsonProperty("lots")
    private List<Lot> lots;
    
    @JsonProperty("cashflows")
    private List<Cashflow> cashflows;
    
    public enum PositionType {
        LONG("Long"),
        SHORT("Short"),
        NEUTRAL("Neutral");
        
        private final String displayName;
        
        PositionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

