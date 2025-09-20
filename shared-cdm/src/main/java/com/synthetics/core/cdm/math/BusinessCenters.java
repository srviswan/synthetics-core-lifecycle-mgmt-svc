package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Business centers for date adjustments
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCenters {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "BusinessCenters";
    
    @JsonProperty("businessCenter")
    private List<String> businessCenter;
    
    /**
     * Common business center codes
     */
    public static class BusinessCenterCodes {
        public static final String USNY = "USNY"; // New York
        public static final String GBLO = "GBLO"; // London
        public static final String DEBE = "DEBE"; // Berlin
        public static final String FRPA = "FRPA"; // Paris
        public static final String CHZU = "CHZU"; // Zurich
        public static final String JPTO = "JPTO"; // Tokyo
        public static final String HKHK = "HKHK"; // Hong Kong
        public static final String SGSG = "SGSG"; // Singapore
        public static final String AUAU = "AUAU"; // Sydney
        public static final String CACO = "CACO"; // Toronto
        public static final String BRSP = "BRSP"; // São Paulo
        public static final String MXMC = "MXMC"; // Mexico City
    }
}


