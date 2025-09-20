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
 * TradableProduct entity representing financial products
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradableProduct {
    
    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID must not exceed 50 characters")
    @JsonProperty("product_id")
    private String productId;
    
    @NotBlank(message = "Product type is required")
    @Size(max = 50, message = "Product type must not exceed 50 characters")
    @JsonProperty("product_type")
    private String productType;
    
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @JsonProperty("product_name")
    private String productName;
    
    @Size(max = 50, message = "Asset class must not exceed 50 characters")
    @JsonProperty("asset_class")
    private String assetClass;
    
    @Size(max = 50, message = "Sub asset class must not exceed 50 characters")
    @JsonProperty("sub_asset_class")
    private String subAssetClass;
    
    @NotNull(message = "Version is required")
    @Positive(message = "Version must be positive")
    @JsonProperty("version")
    private Integer version;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @NotNull(message = "Created date is required")
    @JsonProperty("created_date")
    private LocalDate createdDate;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @JsonProperty("created_by")
    private String createdBy;
}
