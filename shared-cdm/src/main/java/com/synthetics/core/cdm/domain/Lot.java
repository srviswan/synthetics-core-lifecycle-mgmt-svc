package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lot entity representing individual lots within a position
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lot {
    
    @NotBlank(message = "Lot ID is required")
    @Size(max = 50, message = "Lot ID must not exceed 50 characters")
    @JsonProperty("lot_id")
    private String lotId;
    
    @NotBlank(message = "Position ID is required")
    @Size(max = 50, message = "Position ID must not exceed 50 characters")
    @JsonProperty("position_id")
    private String positionId;
    
    @NotBlank(message = "Trade ID is required")
    @Size(max = 50, message = "Trade ID must not exceed 50 characters")
    @JsonProperty("trade_id")
    private String tradeId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("lot_type")
    private LotType lotType;
    
    @JsonProperty("acquisition_date")
    private LocalDate acquisitionDate;
    
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("created_timestamp")
    private LocalDateTime createdTimestamp;
    
    @JsonProperty("updated_timestamp")
    private LocalDateTime updatedTimestamp;
    
    public enum LotType {
        ORIGINAL("Original"),
        AMENDMENT("Amendment"),
        UNWIND("Unwind"),
        TERMINATION("Termination");
        
        private final String displayName;
        
        LotType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

