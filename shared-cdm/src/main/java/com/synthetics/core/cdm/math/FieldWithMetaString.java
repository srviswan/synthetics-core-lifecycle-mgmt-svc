package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Field with metadata for string values
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldWithMetaString {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "FieldWithMetaString";
    
    @NotBlank(message = "Value is required")
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("meta")
    private Meta meta;
    
    /**
     * Metadata for the field
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("version")
        private String version;
        
        @JsonProperty("timestamp")
        private String timestamp;
        
        @JsonProperty("description")
        private String description;
    }
}


