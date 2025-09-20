package com.synthetics.core.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Cashflow persistence
 */
@Entity
@Table(name = "cashflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashflowEntity {
    
    @Id
    @Column(name = "cashflow_id", length = 50)
    private String cashflowId;
    
    @Column(name = "trade_id", length = 50, nullable = false)
    private String tradeId;
    
    @Column(name = "position_id", length = 50, nullable = false)
    private String positionId;
    
    @Column(name = "lot_id", length = 50)
    private String lotId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cashflow_type", length = 20, nullable = false)
    private CashflowType cashflowType;
    
    @Column(name = "payer_party_id", length = 50, nullable = false)
    private String payerPartyId;
    
    @Column(name = "receiver_party_id", length = 50, nullable = false)
    private String receiverPartyId;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20, nullable = false)
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cashflow_status", length = 20, nullable = false)
    private CashflowStatus cashflowStatus;
    
    @Column(name = "status_transition_timestamp")
    private LocalDateTime statusTransitionTimestamp;
    
    @Column(name = "settlement_reference", length = 100)
    private String settlementReference;
    
    @Column(name = "origin_event_id", length = 50, nullable = false)
    private String originEventId;
    
    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;
    
    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled;
    
    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;
    
    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;
    
    @PrePersist
    protected void onCreate() {
        if (createdTimestamp == null) {
            createdTimestamp = LocalDateTime.now();
        }
        if (updatedTimestamp == null) {
            updatedTimestamp = LocalDateTime.now();
        }
        if (revisionNumber == null) {
            revisionNumber = 1;
        }
        if (isCancelled == null) {
            isCancelled = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTimestamp = LocalDateTime.now();
    }
    
    // Enums matching the CDM domain
    public enum CashflowType {
        ACCRUAL, INTEREST, FEE, DIVIDEND, PRINCIPAL, COUPON, SETTLEMENT, TERMINATION
    }
    
    public enum PaymentType {
        ACCRUAL, INTEREST, FEE, DIVIDEND, PRINCIPAL, COUPON, SETTLEMENT, TERMINATION
    }
    
    public enum CashflowStatus {
        ACCRUAL, SCHEDULED_PAYMENT, PAID_NOTSETTLED, SETTLED
    }
}
