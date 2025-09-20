package com.synthetics.core.cashflow.service;

import com.synthetics.core.cashflow.entity.InterestNotionalEntity;
import com.synthetics.core.cashflow.repository.InterestNotionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for Interest Notional operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterestNotionalService {

    private final InterestNotionalRepository interestNotionalRepository;

    /**
     * Calculate interest notional based on lots from input
     */
    public BigDecimal calculateLotBasedInterestNotional(
            java.util.List<com.synthetics.core.cdm.domain.Lot> lots) {

        log.debug("Calculating lot-based interest notional for {} lots", lots.size());

        // Calculate total notional from all lots (quantity * unit price)
        BigDecimal totalNotional = lots.stream()
            .map(lot -> lot.getQuantity().multiply(lot.getUnitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Calculated lot-based interest notional: {}", totalNotional);
        return totalNotional;
    }

    /**
     * Get interest notional from database for a position
     */
    public Optional<BigDecimal> getInterestNotionalFromDb(String positionId) {
        log.debug("Getting interest notional from DB for position: {}", positionId);

        Optional<BigDecimal> amount = interestNotionalRepository.getInterestNotionalAmount(positionId);
        log.debug("Found interest notional in DB: {}", amount.orElse(null));

        return amount;
    }

    /**
     * Check if position has lot-based calculation method
     */
    public boolean hasLotBasedCalculation(String positionId) {
        boolean hasLotBased = interestNotionalRepository.hasLotBasedCalculation(positionId);
        log.debug("Position {} has lot-based calculation: {}", positionId, hasLotBased);
        return hasLotBased;
    }

    /**
     * Save or update interest notional for a position
     */
    @Transactional
    public void saveInterestNotional(String positionId, BigDecimal amount,
                                   InterestNotionalEntity.CalculationMethod method) {
        log.info("Saving interest notional for position {}: {} using method {}",
                positionId, amount, method);

        InterestNotionalEntity entity = InterestNotionalEntity.builder()
            .positionId(positionId)
            .interestNotionalAmount(amount)
            .calculationMethod(method)
            .build();

        interestNotionalRepository.save(entity);
        log.info("Successfully saved interest notional for position: {}", positionId);
    }

    /**
     * Determine the best interest calculation method and get the amount
     */
    public InterestCalculationResult getInterestCalculationResult(String positionId,
                                                                 java.util.List<com.synthetics.core.cdm.domain.Lot> lots) {

        log.debug("Determining interest calculation method for position: {}", positionId);

        // Check if lots are provided and position has lot-based calculation preference
        if (lots != null && !lots.isEmpty()) {
            BigDecimal lotBasedAmount = calculateLotBasedInterestNotional(lots);

            // Save this calculation for future use
            saveInterestNotional(positionId, lotBasedAmount,
                               InterestNotionalEntity.CalculationMethod.LOT_BASED);

            return new InterestCalculationResult(lotBasedAmount,
                                              InterestNotionalEntity.CalculationMethod.LOT_BASED,
                                              "Calculated from " + lots.size() + " lots");
        }

        // Try to get from database
        Optional<BigDecimal> dbAmount = getInterestNotionalFromDb(positionId);
        if (dbAmount.isPresent()) {
            return new InterestCalculationResult(dbAmount.get(),
                                              InterestNotionalEntity.CalculationMethod.POSITION_BASED,
                                              "Retrieved from database");
        }

        // Fallback to position notional amount
        log.warn("No interest notional found for position: {}, using position-based calculation", positionId);
        return new InterestCalculationResult(BigDecimal.ZERO,
                                          InterestNotionalEntity.CalculationMethod.POSITION_BASED,
                                          "Fallback to zero (no data available)");
    }

    /**
     * Result object for interest calculation
     */
    public static class InterestCalculationResult {
        private final BigDecimal amount;
        private final InterestNotionalEntity.CalculationMethod method;
        private final String description;

        public InterestCalculationResult(BigDecimal amount,
                                       InterestNotionalEntity.CalculationMethod method,
                                       String description) {
            this.amount = amount;
            this.method = method;
            this.description = description;
        }

        public BigDecimal getAmount() { return amount; }
        public InterestNotionalEntity.CalculationMethod getMethod() { return method; }
        public String getDescription() { return description; }
    }
}
