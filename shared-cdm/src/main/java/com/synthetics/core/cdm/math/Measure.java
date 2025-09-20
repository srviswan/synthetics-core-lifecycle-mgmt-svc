package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Measure representing a value with a unit
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Measure {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "Measure";
    
    @NotNull(message = "Value is required")
    @JsonProperty("value")
    private BigDecimal value;
    
    @JsonProperty("unit")
    private UnitType unit;
}


