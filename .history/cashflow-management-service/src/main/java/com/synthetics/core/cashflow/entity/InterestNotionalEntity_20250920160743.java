package com.synthetics.core.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for Interest Notional persistence
 */
@Entity
@Table(name = "interest_notional")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestNotionalEntity {

    @Id
    @Column(name = "position_id", length = 50)
    private String positionId;

    @Column(name = "interest_notional_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal interestNotionalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", length = 20, nullable = false)
    private CalculationMethod calculationMethod;

    @Column(name = "last_updated_timestamp", nullable = false)
    private LocalDateTime lastUpdatedTimestamp;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedTimestamp = LocalDateTime.now();
    }

    // Calculation methods
    public enum CalculationMethod {
        LOT_BASED,        // Calculated from lot quantities and prices
        POSITION_BASED    // Based on position notional amount
    }
}
