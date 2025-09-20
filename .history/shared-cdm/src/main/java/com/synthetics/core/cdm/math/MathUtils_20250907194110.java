package com.synthetics.core.cdm.math;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Utility class for mathematical operations on CDM math objects
 * Based on FINOS CDM Rosetta standard
 */
@UtilityClass
public class MathUtils {
    
    /**
     * Add two NonNegativeQuantity objects
     * @param qty1 first quantity
     * @param qty2 second quantity
     * @return sum of the quantities
     */
    public static NonNegativeQuantity add(NonNegativeQuantity qty1, NonNegativeQuantity qty2) {
        if (qty1 == null || qty2 == null) {
            throw new IllegalArgumentException("Quantities cannot be null");
        }
        
        if (!isSameUnit(qty1.getUnit(), qty2.getUnit())) {
            throw new IllegalArgumentException("Cannot add quantities with different units");
        }
        
        BigDecimal sum = qty1.getValue().add(qty2.getValue());
        
        return NonNegativeQuantity.builder()
            .value(sum)
            .unit(qty1.getUnit())
            .build();
    }
    
    /**
     * Multiply a NonNegativeQuantity by a multiplier
     * @param qty the quantity to multiply
     * @param multiplier the multiplier
     * @return the multiplied quantity
     */
    public static NonNegativeQuantity multiply(NonNegativeQuantity qty, BigDecimal multiplier) {
        if (qty == null || multiplier == null) {
            throw new IllegalArgumentException("Quantity and multiplier cannot be null");
        }
        
        BigDecimal result = qty.getValue().multiply(multiplier);
        
        return NonNegativeQuantity.builder()
            .value(result)
            .unit(qty.getUnit())
            .build();
    }
    
    /**
     * Calculate the weighted average of dated values
     * @param datedValues list of dated values
     * @param weights list of weights (must match the size of datedValues)
     * @return weighted average
     */
    public static BigDecimal calculateWeightedAverage(List<DatedValue> datedValues, List<BigDecimal> weights) {
        if (datedValues == null || weights == null || datedValues.size() != weights.size()) {
            throw new IllegalArgumentException("Dated values and weights must have the same size");
        }
        
        if (datedValues.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate average of empty list");
        }
        
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (int i = 0; i < datedValues.size(); i++) {
            BigDecimal value = datedValues.get(i).getValue();
            BigDecimal weight = weights.get(i);
            
            weightedSum = weightedSum.add(value.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Total weight cannot be zero");
        }
        
        return weightedSum.divide(totalWeight, 10, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate the time-weighted average of dated values
     * @param datedValues list of dated values sorted by date
     * @param startDate start date for the calculation
     * @param endDate end date for the calculation
     * @return time-weighted average
     */
    public static BigDecimal calculateTimeWeightedAverage(List<DatedValue> datedValues, LocalDate startDate, LocalDate endDate) {
        if (datedValues == null || datedValues.isEmpty()) {
            throw new IllegalArgumentException("Dated values cannot be null or empty");
        }
        
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        
        BigDecimal totalWeightedValue = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (int i = 0; i < datedValues.size(); i++) {
            DatedValue currentValue = datedValues.get(i);
            LocalDate currentDate = currentValue.getDate().getAdjustedDate();
            
            // Calculate the weight (time period) for this value
            LocalDate nextDate = (i < datedValues.size() - 1) 
                ? datedValues.get(i + 1).getDate().getAdjustedDate() 
                : endDate;
            
            long days = ChronoUnit.DAYS.between(currentDate, nextDate);
            BigDecimal weight = BigDecimal.valueOf(days);
            
            totalWeightedValue = totalWeightedValue.add(currentValue.getValue().multiply(weight));
            totalWeight = totalWeight.add(weight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Total weight cannot be zero");
        }
        
        return totalWeightedValue.divide(totalWeight, 10, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if two unit types are the same
     * @param unit1 first unit
     * @param unit2 second unit
     * @return true if units are the same
     */
    private static boolean isSameUnit(UnitType unit1, UnitType unit2) {
        if (unit1 == null || unit2 == null) {
            return false;
        }
        
        // Compare currency
        if (unit1.getCurrency() != null && unit2.getCurrency() != null) {
            return unit1.getCurrency().getValue().equals(unit2.getCurrency().getValue());
        }
        
        // Compare financial unit
        if (unit1.getFinancialUnit() != null && unit2.getFinancialUnit() != null) {
            return unit1.getFinancialUnit().equals(unit2.getFinancialUnit());
        }
        
        // Compare capacity unit
        if (unit1.getCapacityUnit() != null && unit2.getCapacityUnit() != null) {
            return unit1.getCapacityUnit().equals(unit2.getCapacityUnit());
        }
        
        // Compare weather unit
        if (unit1.getWeatherUnit() != null && unit2.getWeatherUnit() != null) {
            return unit1.getWeatherUnit().equals(unit2.getWeatherUnit());
        }
        
        // Compare custom unit
        if (unit1.getCustomUnit() != null && unit2.getCustomUnit() != null) {
            return unit1.getCustomUnit().getValue().equals(unit2.getCustomUnit().getValue());
        }
        
        return false;
    }
    
    /**
     * Create a simple NonNegativeQuantity with currency
     * @param value the numeric value
     * @param currency the currency code
     * @return NonNegativeQuantity object
     */
    public static NonNegativeQuantity createCurrencyQuantity(BigDecimal value, String currency) {
        Currency currencyObj = Currency.builder()
            .value(currency)
            .build();
        
        UnitType unit = UnitType.builder()
            .currency(currencyObj)
            .build();
        
        return NonNegativeQuantity.builder()
            .value(value)
            .unit(unit)
            .build();
    }
    
    /**
     * Create a simple NonNegativeQuantity with financial unit
     * @param value the numeric value
     * @param financialUnit the financial unit
     * @return NonNegativeQuantity object
     */
    public static NonNegativeQuantity createFinancialQuantity(BigDecimal value, UnitType.FinancialUnit financialUnit) {
        UnitType unit = UnitType.builder()
            .financialUnit(financialUnit)
            .build();
        
        return NonNegativeQuantity.builder()
            .value(value)
            .unit(unit)
            .build();
    }
}
