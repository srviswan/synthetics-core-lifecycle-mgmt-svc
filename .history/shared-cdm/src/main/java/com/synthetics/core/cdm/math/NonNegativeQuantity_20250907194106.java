package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Non-negative quantity representing a measurable amount
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NonNegativeQuantity {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "NonNegativeQuantity";
    
    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Value must be non-negative")
    @JsonProperty("value")
    private BigDecimal value;
    
    @NotNull(message = "Unit is required")
    @JsonProperty("unit")
    private UnitType unit;
    
    @JsonProperty("datedValue")
    private List<DatedValue> datedValue;
    
    @JsonProperty("multiplier")
    private Measure multiplier;
    
    @JsonProperty("frequency")
    private Frequency frequency;
}
