package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Currency representation
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "Currency";
    
    @NotBlank(message = "Currency value is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("value")
    private String value;
    
    /**
     * Common currency codes
     */
    public static class CurrencyCodes {
        public static final String USD = "USD";
        public static final String EUR = "EUR";
        public static final String GBP = "GBP";
        public static final String JPY = "JPY";
        public static final String CHF = "CHF";
        public static final String CAD = "CAD";
        public static final String AUD = "AUD";
        public static final String CNY = "CNY";
        public static final String INR = "INR";
        public static final String BRL = "BRL";
    }
}
