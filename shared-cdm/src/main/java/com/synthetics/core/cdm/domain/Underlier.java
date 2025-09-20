package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Underlier entity representing underlying assets in swap trades
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Underlier {
    
    @NotBlank(message = "Underlier ID is required")
    @Size(max = 50, message = "Underlier ID must not exceed 50 characters")
    @JsonProperty("underlier_id")
    private String underlierId;
    
    @NotNull(message = "Asset type is required")
    @JsonProperty("asset_type")
    private AssetType assetType;
    
    @NotBlank(message = "Primary identifier is required")
    @Size(max = 50, message = "Primary identifier must not exceed 50 characters")
    @JsonProperty("primary_identifier")
    private String primaryIdentifier;
    
    @NotNull(message = "Identifier type is required")
    @JsonProperty("identifier_type")
    private IdentifierType identifierType;
    
    @JsonProperty("secondary_identifiers")
    private Map<String, String> secondaryIdentifiers;
    
    @NotBlank(message = "Asset name is required")
    @Size(max = 200, message = "Asset name must not exceed 200 characters")
    @JsonProperty("asset_name")
    private String assetName;
    
    @JsonProperty("asset_description")
    private String assetDescription;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase letters")
    @JsonProperty("currency")
    private String currency;
    
    @Size(max = 50, message = "Exchange must not exceed 50 characters")
    @JsonProperty("exchange")
    private String exchange;
    
    @Size(max = 2, message = "Country code must be 2 characters")
    @JsonProperty("country")
    private String country;
    
    @Size(max = 100, message = "Sector must not exceed 100 characters")
    @JsonProperty("sector")
    private String sector;
    
    @Builder.Default
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    @JsonProperty("created_date")
    private LocalDate createdDate;
    
    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;
    
    public enum AssetType {
        SINGLE_NAME("Single Name"),
        INDEX("Index"),
        BASKET("Basket");
        
        private final String displayName;
        
        AssetType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum IdentifierType {
        ISIN("ISIN"),
        CUSIP("CUSIP"),
        RIC("RIC"),
        BLOOMBERG("Bloomberg"),
        INTERNAL("Internal");
        
        private final String displayName;
        
        IdentifierType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

