package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * EconomicTerms entity representing economic terms of a trade
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EconomicTerms {
    
    @NotBlank(message = "Economic terms ID is required")
    @Size(max = 50, message = "Economic terms ID must not exceed 50 characters")
    @JsonProperty("economic_terms_id")
    private String economicTermsId;
    
    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID must not exceed 50 characters")
    @JsonProperty("product_id")
    private String productId;
    
    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;
    
    @JsonProperty("termination_date")
    private LocalDate terminationDate;
    
    @Size(max = 50, message = "Calculation agent ID must not exceed 50 characters")
    @JsonProperty("calculation_agent_id")
    private String calculationAgentId;
    
    @Size(max = 30, message = "Business day convention must not exceed 30 characters")
    @JsonProperty("business_day_convention")
    private String businessDayConvention;
    
    @JsonProperty("business_centers")
    private String businessCenters;
    
    @JsonProperty("extraordinary_events")
    private String extraordinaryEvents;
    
    @NotNull(message = "Version is required")
    @Positive(message = "Version must be positive")
    @JsonProperty("version")
    private Integer version;
}



