package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Adjustable date with business day adjustments
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustableDate {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "AdjustableDate";
    
    @NotNull(message = "Unadjusted date is required")
    @JsonProperty("unadjustedDate")
    private LocalDate unadjustedDate;
    
    @JsonProperty("dateAdjustments")
    private BusinessDayAdjustments dateAdjustments;
    
    /**
     * Get the adjusted date based on business day adjustments
     * @return the adjusted date
     */
    public LocalDate getAdjustedDate() {
        if (dateAdjustments == null) {
            return unadjustedDate;
        }
        return dateAdjustments.adjustDate(unadjustedDate);
    }
}


