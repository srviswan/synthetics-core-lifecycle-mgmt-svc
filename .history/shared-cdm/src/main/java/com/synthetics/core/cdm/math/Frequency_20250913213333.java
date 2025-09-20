package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Frequency representation for periodic events
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Frequency {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "Frequency";
    
    @Min(value = 1, message = "Period multiplier must be at least 1")
    @JsonProperty("periodMultiplier")
    private Integer periodMultiplier;
    
    @NotNull(message = "Period is required")
    @JsonProperty("period")
    private Period period;
    
    @JsonProperty("rollConvention")
    private RollConvention rollConvention;
    
    /**
     * Period enumeration
     */
    public enum Period {
        D("Day"),
        W("Week"),
        M("Month"),
        Q("Quarter"),
        S("Semi-Annual"),
        Y("Year");
        
        private final String displayName;
        
        Period(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Roll convention enumeration
     */
    public enum RollConvention {
        EOM("End of Month"),
        SOM("Start of Month"),
        IMM("IMM Date"),
        NONE("None");
        
        private final String displayName;
        
        RollConvention(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

