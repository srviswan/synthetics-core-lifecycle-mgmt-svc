package com.synthetics.core.cdm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cashflow entity representing generated cashflows
 * Based on FINOS CDM Rosetta standard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cashflow {
    
    @NotBlank(message = "Cashflow ID is required")
    @Size(max = 50, message = "Cashflow ID must not exceed 50 characters")
    @JsonProperty("cashflow_id")
    private String cashflowId;
    
    @NotBlank(message = "Trade ID is required")
    @Size(max = 50, message = "Trade ID must not exceed 50 characters")
    @JsonProperty("trade_id")
    private String tradeId;
    
    @NotBlank(message = "Position ID is required")
    @Size(max = 50, message = "Position ID must not exceed 50 characters")
    @JsonProperty("position_id")
    private String positionId;
    
    @Size(max = 50, message = "Lot ID must not exceed 50 characters")
    @JsonProperty("lot_id")
    private String lotId;
    
    @NotNull(message = "Cashflow type is required")
    @JsonProperty("cashflow_type")
    private CashflowType cashflowType;
    
    @NotBlank(message = "Payer party ID is required")
    @Size(max = 50, message = "Payer party ID must not exceed 50 characters")
    @JsonProperty("payer_party_id")
    private String payerPartyId;
    
    @NotBlank(message = "Receiver party ID is required")
    @Size(max = 50, message = "Receiver party ID must not exceed 50 characters")
    @JsonProperty("receiver_party_id")
    private String receiverPartyId;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @JsonProperty("currency")
    private String currency;
    
    @NotNull(message = "Amount is required")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @NotNull(message = "Settlement date is required")
    @JsonProperty("settlement_date")
    private LocalDate settlementDate;
    
    @NotNull(message = "Payment type is required")
    @JsonProperty("payment_type")
    private PaymentType paymentType;
    
    @Builder.Default
    @JsonProperty("cashflow_status")
    private CashflowStatus cashflowStatus = CashflowStatus.ACCRUAL;
    
    @NotBlank(message = "Origin event ID is required")
    @Size(max = 50, message = "Origin event ID must not exceed 50 characters")
    @JsonProperty("origin_event_id")
    private String originEventId;
    
    @Builder.Default
    @JsonProperty("revision_number")
    private Integer revisionNumber = 1;
    
    @Builder.Default
    @JsonProperty("is_cancelled")
    private Boolean isCancelled = false;
    
    @Size(max = 100, message = "Cancellation reason must not exceed 100 characters")
    @JsonProperty("cancellation_reason")
    private String cancellationReason;
    
    @JsonProperty("status_transition_timestamp")
    private LocalDateTime statusTransitionTimestamp;
    
    @Size(max = 100, message = "Settlement reference must not exceed 100 characters")
    @JsonProperty("settlement_reference")
    private String settlementReference;
    
    @JsonProperty("created_timestamp")
    private LocalDateTime createdTimestamp;
    
    @JsonProperty("updated_timestamp")
    private LocalDateTime updatedTimestamp;
    
    public enum CashflowType {
        ACCRUAL("Accrual"),
        INTEREST("Interest"),
        FEE("Fee"),
        DIVIDEND("Dividend"),
        PRINCIPAL("Principal"),
        RESET("Reset");
        
        private final String displayName;
        
        CashflowType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum PaymentType {
        ACCRUAL("Accrual"),
        PRINCIPAL("Principal"),
        DIVIDEND("Dividend"),
        FEE("Fee");
        
        private final String displayName;
        
        PaymentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum CashflowStatus {
        ACCRUAL("Accrual"),
        SCHEDULED_PAYMENT("Scheduled Payment"),
        PAID_NOTSETTLED("Paid Not Settled"),
        SETTLED("Settled");
        
        private final String displayName;
        
        CashflowStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

