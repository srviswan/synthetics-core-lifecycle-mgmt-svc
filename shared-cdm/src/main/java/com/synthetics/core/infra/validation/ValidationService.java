package com.synthetics.core.infra.validation;

import com.synthetics.core.cdm.domain.Cashflow;
import com.synthetics.core.cdm.domain.Position;
import com.synthetics.core.cdm.domain.Trade;
import com.synthetics.core.cdm.events.LifecycleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation service for business rules and data validation
 * Provides centralized validation logic for domain objects and events
 */
@Service
@Slf4j
public class ValidationService {
    
    /**
     * Validate trade
     * @param trade The trade to validate
     * @return List of validation errors
     */
    public List<String> validateTrade(Trade trade) {
        List<String> errors = new ArrayList<>();
        
        if (trade == null) {
            errors.add("Trade cannot be null");
            return errors;
        }
        
        if (trade.getTradeId() == null || trade.getTradeId().trim().isEmpty()) {
            errors.add("Trade ID is required");
        }
        
        if (trade.getProductId() == null || trade.getProductId().trim().isEmpty()) {
            errors.add("Product ID is required");
        }
        
        if (trade.getTradeDate() == null) {
            errors.add("Trade date is required");
        } else if (trade.getTradeDate().isAfter(LocalDate.now())) {
            errors.add("Trade date cannot be in the future");
        }
        
        if (trade.getPositions() == null || trade.getPositions().isEmpty()) {
            errors.add("Trade must have at least one position");
        } else {
            for (Position position : trade.getPositions()) {
                errors.addAll(validatePosition(position));
            }
        }
        
        return errors;
    }
    
    /**
     * Validate position
     * @param position The position to validate
     * @return List of validation errors
     */
    public List<String> validatePosition(Position position) {
        List<String> errors = new ArrayList<>();
        
        if (position == null) {
            errors.add("Position cannot be null");
            return errors;
        }
        
        if (position.getPositionId() == null || position.getPositionId().trim().isEmpty()) {
            errors.add("Position ID is required");
        }
        
        if (position.getTradeId() == null || position.getTradeId().trim().isEmpty()) {
            errors.add("Trade ID is required");
        }
        
        if (position.getUnderlierId() == null || position.getUnderlierId().trim().isEmpty()) {
            errors.add("Underlier ID is required");
        }
        
        if (position.getNotionalAmount() == null) {
            errors.add("Notional amount is required");
        } else if (position.getNotionalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Notional amount must be positive");
        }
        
        if (position.getNotionalCurrency() == null || position.getNotionalCurrency().trim().isEmpty()) {
            errors.add("Notional currency is required");
        } else if (position.getNotionalCurrency().length() != 3) {
            errors.add("Notional currency must be 3 characters");
        }
        
        if (position.getEffectiveDate() == null) {
            errors.add("Effective date is required");
        }
        
        if (position.getTerminationDate() != null && 
            position.getTerminationDate().isBefore(position.getEffectiveDate())) {
            errors.add("Termination date cannot be before effective date");
        }
        
        if (position.getLots() == null || position.getLots().isEmpty()) {
            errors.add("Position must have at least one lot");
        }
        
        return errors;
    }
    
    /**
     * Validate cashflow
     * @param cashflow The cashflow to validate
     * @return List of validation errors
     */
    public List<String> validateCashflow(Cashflow cashflow) {
        List<String> errors = new ArrayList<>();
        
        if (cashflow == null) {
            errors.add("Cashflow cannot be null");
            return errors;
        }
        
        if (cashflow.getCashflowId() == null || cashflow.getCashflowId().trim().isEmpty()) {
            errors.add("Cashflow ID is required");
        }
        
        if (cashflow.getTradeId() == null || cashflow.getTradeId().trim().isEmpty()) {
            errors.add("Trade ID is required");
        }
        
        if (cashflow.getPositionId() == null || cashflow.getPositionId().trim().isEmpty()) {
            errors.add("Position ID is required");
        }
        
        if (cashflow.getCashflowType() == null) {
            errors.add("Cashflow type is required");
        }
        
        if (cashflow.getPayerPartyId() == null || cashflow.getPayerPartyId().trim().isEmpty()) {
            errors.add("Payer party ID is required");
        }
        
        if (cashflow.getReceiverPartyId() == null || cashflow.getReceiverPartyId().trim().isEmpty()) {
            errors.add("Receiver party ID is required");
        }
        
        if (cashflow.getPayerPartyId() != null && cashflow.getReceiverPartyId() != null &&
            cashflow.getPayerPartyId().equals(cashflow.getReceiverPartyId())) {
            errors.add("Payer and receiver party cannot be the same");
        }
        
        if (cashflow.getCurrency() == null || cashflow.getCurrency().trim().isEmpty()) {
            errors.add("Currency is required");
        } else if (cashflow.getCurrency().length() != 3) {
            errors.add("Currency must be 3 characters");
        }
        
        if (cashflow.getAmount() == null) {
            errors.add("Amount is required");
        } else if (cashflow.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            errors.add("Amount cannot be zero");
        }
        
        if (cashflow.getSettlementDate() == null) {
            errors.add("Settlement date is required");
        }
        
        if (cashflow.getPaymentType() == null) {
            errors.add("Payment type is required");
        }
        
        if (cashflow.getCashflowStatus() == null) {
            errors.add("Cashflow status is required");
        }
        
        if (cashflow.getOriginEventId() == null || cashflow.getOriginEventId().trim().isEmpty()) {
            errors.add("Origin event ID is required");
        }
        
        if (cashflow.getRevisionNumber() == null || cashflow.getRevisionNumber() <= 0) {
            errors.add("Revision number must be positive");
        }
        
        return errors;
    }
    
    /**
     * Validate lifecycle event
     * @param event The lifecycle event to validate
     * @return List of validation errors
     */
    public List<String> validateLifecycleEvent(LifecycleEvent event) {
        List<String> errors = new ArrayList<>();
        
        if (event == null) {
            errors.add("Lifecycle event cannot be null");
            return errors;
        }
        
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            errors.add("Event ID is required");
        }
        
        if (event.getTradeId() == null || event.getTradeId().trim().isEmpty()) {
            errors.add("Trade ID is required");
        }
        
        if (event.getTimestamp() == null) {
            errors.add("Event timestamp is required");
        }
        
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            errors.add("Event type is required");
        }
        
        return errors;
    }
    
    /**
     * Validate business rules for cashflow generation
     * @param trade The trade to validate
     * @param valuationDate The valuation date
     * @return List of validation errors
     */
    public List<String> validateCashflowGeneration(Trade trade, LocalDate valuationDate) {
        List<String> errors = new ArrayList<>();
        
        if (trade == null) {
            errors.add("Trade cannot be null");
            return errors;
        }
        
        if (valuationDate == null) {
            errors.add("Valuation date is required");
            return errors;
        }
        
        if (trade.getTradeDate() != null && valuationDate.isBefore(trade.getTradeDate())) {
            errors.add("Valuation date cannot be before trade date");
        }
        
        if (trade.getPositions() == null || trade.getPositions().isEmpty()) {
            errors.add("Trade must have positions for cashflow generation");
        }
        
        for (Position position : trade.getPositions()) {
            if (position.getEffectiveDate() != null && valuationDate.isBefore(position.getEffectiveDate())) {
                errors.add("Valuation date cannot be before position effective date: " + position.getPositionId());
            }
            
            if (position.getTerminationDate() != null && valuationDate.isAfter(position.getTerminationDate())) {
                errors.add("Valuation date cannot be after position termination date: " + position.getPositionId());
            }
        }
        
        return errors;
    }
    
    /**
     * Check if validation errors exist
     * @param errors List of validation errors
     * @return true if errors exist, false otherwise
     */
    public boolean hasErrors(List<String> errors) {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * Log validation errors
     * @param errors List of validation errors
     * @param context Context for logging
     */
    public void logValidationErrors(List<String> errors, String context) {
        if (hasErrors(errors)) {
            log.error("Validation errors in {}: {}", context, String.join(", ", errors));
        }
    }
}



