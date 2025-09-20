package com.synthetics.core.cdm.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Business day adjustments for date calculations
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDayAdjustments {
    
    @JsonProperty("@type")
    @Builder.Default
    private String type = "BusinessDayAdjustments";
    
    @JsonProperty("businessDayConvention")
    private BusinessDayConvention businessDayConvention;
    
    @JsonProperty("businessCenters")
    private BusinessCenters businessCenters;
    
    /**
     * Business day convention enumeration
     */
    public enum BusinessDayConvention {
        FOLLOWING("Following"),
        MODIFIED_FOLLOWING("Modified Following"),
        PRECEDING("Preceding"),
        MODIFIED_PRECEDING("Modified Preceding"),
        NEAREST("Nearest"),
        NONE("None");
        
        private final String displayName;
        
        BusinessDayConvention(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Adjust a date based on business day convention
     * @param date the input date
     * @return the adjusted date
     */
    public LocalDate adjustDate(LocalDate date) {
        if (businessDayConvention == null || businessDayConvention == BusinessDayConvention.NONE) {
            return date;
        }
        
        LocalDate adjustedDate = date;
        
        switch (businessDayConvention) {
            case FOLLOWING:
                adjustedDate = adjustToFollowingBusinessDay(date);
                break;
            case MODIFIED_FOLLOWING:
                adjustedDate = adjustToModifiedFollowingBusinessDay(date);
                break;
            case PRECEDING:
                adjustedDate = adjustToPrecedingBusinessDay(date);
                break;
            case MODIFIED_PRECEDING:
                adjustedDate = adjustToModifiedPrecedingBusinessDay(date);
                break;
            case NEAREST:
                adjustedDate = adjustToNearestBusinessDay(date);
                break;
        }
        
        return adjustedDate;
    }
    
    private LocalDate adjustToFollowingBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }
    
    private LocalDate adjustToPrecedingBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.minusDays(1);
        }
        return adjusted;
    }
    
    private LocalDate adjustToModifiedFollowingBusinessDay(LocalDate date) {
        LocalDate following = adjustToFollowingBusinessDay(date);
        if (following.getMonth() != date.getMonth()) {
            return adjustToPrecedingBusinessDay(date);
        }
        return following;
    }
    
    private LocalDate adjustToModifiedPrecedingBusinessDay(LocalDate date) {
        LocalDate preceding = adjustToPrecedingBusinessDay(date);
        if (preceding.getMonth() != date.getMonth()) {
            return adjustToFollowingBusinessDay(date);
        }
        return preceding;
    }
    
    private LocalDate adjustToNearestBusinessDay(LocalDate date) {
        LocalDate following = adjustToFollowingBusinessDay(date);
        LocalDate preceding = adjustToPrecedingBusinessDay(date);
        
        long followingDays = java.time.temporal.ChronoUnit.DAYS.between(date, following);
        long precedingDays = java.time.temporal.ChronoUnit.DAYS.between(preceding, date);
        
        return followingDays <= precedingDays ? following : preceding;
    }
    
    private boolean isBusinessDay(LocalDate date) {
        // Simple implementation - exclude weekends
        // In a real implementation, this would consider business centers and holidays
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
