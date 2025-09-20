package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unit type representing different measurement units
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitType {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "UnitType";
    
    @JsonProperty("capacityUnit")
    private CapacityUnit capacityUnit;
    
    @JsonProperty("weatherUnit")
    private WeatherUnit weatherUnit;
    
    @JsonProperty("financialUnit")
    private FinancialUnit financialUnit;
    
    @JsonProperty("currency")
    private Currency currency;
    
    @JsonProperty("customUnit")
    private FieldWithMetaString customUnit;
    
    /**
     * Capacity unit enumeration
     */
    public enum CapacityUnit {
        BARREL("Barrel"),
        CUBIC_METER("Cubic Meter"),
        CUBIC_FOOT("Cubic Foot"),
        GALLON("Gallon"),
        LITER("Liter"),
        TONNE("Tonne"),
        POUND("Pound"),
        KILOGRAM("Kilogram");
        
        private final String displayName;
        
        CapacityUnit(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Weather unit enumeration
     */
    public enum WeatherUnit {
        DEGREE_CELSIUS("Degree Celsius"),
        DEGREE_FAHRENHEIT("Degree Fahrenheit"),
        KELVIN("Kelvin"),
        MILLIMETER("Millimeter"),
        INCH("Inch"),
        MILLIBAR("Millibar"),
        PASCAL("Pascal");
        
        private final String displayName;
        
        WeatherUnit(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Financial unit enumeration
     */
    public enum FinancialUnit {
        SHARE("Share"),
        CONTRACT("Contract"),
        LOT("Lot"),
        BOND("Bond"),
        OPTION("Option"),
        FUTURE("Future"),
        SWAP("Swap"),
        BASIS_POINT("Basis Point"),
        PERCENTAGE("Percentage"),
        PERCENT("Percent");
        
        private final String displayName;
        
        FinancialUnit(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}


