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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Party entity representing counterparties in swap trades
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Party {
    
    @NotBlank(message = "Party ID is required")
    @Size(max = 50, message = "Party ID must not exceed 50 characters")
    @JsonProperty("party_id")
    private String partyId;
    
    @NotBlank(message = "Party name is required")
    @Size(max = 200, message = "Party name must not exceed 200 characters")
    @JsonProperty("party_name")
    private String partyName;
    
    @NotNull(message = "Party type is required")
    @JsonProperty("party_type")
    private PartyType partyType;
    
    @Size(max = 20, message = "LEI code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9]{20}$", message = "LEI code must be 20 alphanumeric characters")
    @JsonProperty("lei_code")
    private String leiCode;
    
    @JsonProperty("party_identifiers")
    private Map<String, String> partyIdentifiers;
    
    @Size(max = 2, message = "Country code must be 2 characters")
    @JsonProperty("country_of_incorporation")
    private String countryOfIncorporation;
    
    @JsonProperty("address")
    private Address address;
    
    @JsonProperty("contact_information")
    private ContactInformation contactInformation;
    
    @JsonProperty("regulatory_status")
    private Map<String, Object> regulatoryStatus;
    
    @Size(max = 10, message = "Credit rating must not exceed 10 characters")
    @JsonProperty("credit_rating")
    private String creditRating;
    
    @Builder.Default
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    @JsonProperty("created_date")
    private LocalDate createdDate;
    
    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;
    
    public enum PartyType {
        BANK("Bank"),
        FUND("Fund"),
        CORPORATION("Corporation"),
        INDIVIDUAL("Individual"),
        GOVERNMENT("Government"),
        OTHER("Other");
        
        private final String displayName;
        
        PartyType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @JsonProperty("street_address")
        private String streetAddress;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("state_province")
        private String stateProvince;
        
        @JsonProperty("postal_code")
        private String postalCode;
        
        @JsonProperty("country")
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInformation {
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("fax")
        private String fax;
        
        @JsonProperty("website")
        private String website;
    }
}
