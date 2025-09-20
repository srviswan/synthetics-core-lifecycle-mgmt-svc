package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dated value representing a value associated with a specific date
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatedValue {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "DatedValue";
    
    @NotNull(message = "Value is required")
    @JsonProperty("value")
    private BigDecimal value;
    
    @NotNull(message = "Date is required")
    @JsonProperty("date")
    private AdjustableDate date;
}
