package com.synthetics.core.cdm.math;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the math package
 */
public class MathPackageTest {
    
    @Test
    public void testNonNegativeQuantityCreation() {
        // Create currency
        Currency usd = Currency.builder()
            .value("USD")
            .build();
        
        // Create unit type
        UnitType unit = UnitType.builder()
            .currency(usd)
            .build();
        
        // Create non-negative quantity
        NonNegativeQuantity quantity = NonNegativeQuantity.builder()
            .value(new BigDecimal("1000.50"))
            .unit(unit)
            .build();
        
        assertEquals("NonNegativeQuantity", quantity.getType());
        assertEquals(new BigDecimal("1000.50"), quantity.getValue());
        assertEquals("USD", quantity.getUnit().getCurrency().getValue());
    }
    
    @Test
    public void testDatedValueWithBusinessDayAdjustments() {
        // Create business centers
        BusinessCenters businessCenters = BusinessCenters.builder()
            .businessCenter(Arrays.asList("USNY", "GBLO"))
            .build();
        
        // Create business day adjustments
        BusinessDayAdjustments adjustments = BusinessDayAdjustments.builder()
            .businessDayConvention(BusinessDayAdjustments.BusinessDayConvention.FOLLOWING)
            .businessCenters(businessCenters)
            .build();
        
        // Create adjustable date
        AdjustableDate adjustableDate = AdjustableDate.builder()
            .unadjustedDate(LocalDate.of(2024, 1, 15))
            .dateAdjustments(adjustments)
            .build();
        
        // Create dated value
        DatedValue datedValue = DatedValue.builder()
            .value(new BigDecimal("500.25"))
            .date(adjustableDate)
            .build();
        
        assertEquals("DatedValue", datedValue.getType());
        assertEquals(new BigDecimal("500.25"), datedValue.getValue());
        assertEquals(LocalDate.of(2024, 1, 15), datedValue.getDate().getUnadjustedDate());
    }
    
    @Test
    public void testFrequencyCreation() {
        Frequency frequency = Frequency.builder()
            .periodMultiplier(1)
            .period(Frequency.Period.M)
            .rollConvention(Frequency.RollConvention.EOM)
            .build();
        
        assertEquals("Frequency", frequency.getType());
        assertEquals(1, frequency.getPeriodMultiplier());
        assertEquals(Frequency.Period.M, frequency.getPeriod());
        assertEquals(Frequency.RollConvention.EOM, frequency.getRollConvention());
    }
    
    @Test
    public void testMathUtilsAdd() {
        NonNegativeQuantity qty1 = MathUtils.createCurrencyQuantity(new BigDecimal("100.00"), "USD");
        NonNegativeQuantity qty2 = MathUtils.createCurrencyQuantity(new BigDecimal("200.50"), "USD");
        
        NonNegativeQuantity result = MathUtils.add(qty1, qty2);
        
        assertEquals(new BigDecimal("300.50"), result.getValue());
        assertEquals("USD", result.getUnit().getCurrency().getValue());
    }
    
    @Test
    public void testMathUtilsMultiply() {
        NonNegativeQuantity qty = MathUtils.createCurrencyQuantity(new BigDecimal("100.00"), "USD");
        BigDecimal multiplier = new BigDecimal("1.5");
        
        NonNegativeQuantity result = MathUtils.multiply(qty, multiplier);
        
        assertEquals(new BigDecimal("150.000"), result.getValue());
        assertEquals("USD", result.getUnit().getCurrency().getValue());
    }
    
    @Test
    public void testWeightedAverage() {
        DatedValue value1 = DatedValue.builder()
            .value(new BigDecimal("100.00"))
            .date(AdjustableDate.builder()
                .unadjustedDate(LocalDate.of(2024, 1, 1))
                .build())
            .build();
        
        DatedValue value2 = DatedValue.builder()
            .value(new BigDecimal("200.00"))
            .date(AdjustableDate.builder()
                .unadjustedDate(LocalDate.of(2024, 1, 2))
                .build())
            .build();
        
        List<DatedValue> values = Arrays.asList(value1, value2);
        List<BigDecimal> weights = Arrays.asList(new BigDecimal("0.3"), new BigDecimal("0.7"));
        
        BigDecimal result = MathUtils.calculateWeightedAverage(values, weights);
        
        assertEquals(new BigDecimal("170.0000000000"), result);
    }
    
    @Test
    public void testCompleteExample() {
        // Create the complete example from the JSON
        Currency usd = Currency.builder()
            .value("USD")
            .build();
        
        UnitType unit = UnitType.builder()
            .capacityUnit(UnitType.CapacityUnit.BARREL)
            .weatherUnit(UnitType.WeatherUnit.DEGREE_CELSIUS)
            .financialUnit(UnitType.FinancialUnit.SHARE)
            .currency(usd)
            .customUnit(FieldWithMetaString.builder()
                .value("custom_unit_name")
                .build())
            .build();
        
        BusinessCenters businessCenters = BusinessCenters.builder()
            .businessCenter(Arrays.asList("USNY", "GBLO"))
            .build();
        
        BusinessDayAdjustments adjustments = BusinessDayAdjustments.builder()
            .businessDayConvention(BusinessDayAdjustments.BusinessDayConvention.FOLLOWING)
            .businessCenters(businessCenters)
            .build();
        
        AdjustableDate date1 = AdjustableDate.builder()
            .unadjustedDate(LocalDate.of(2024, 1, 15))
            .dateAdjustments(adjustments)
            .build();
        
        AdjustableDate date2 = AdjustableDate.builder()
            .unadjustedDate(LocalDate.of(2024, 6, 15))
            .build();
        
        DatedValue datedValue1 = DatedValue.builder()
            .value(new BigDecimal("500.25"))
            .date(date1)
            .build();
        
        DatedValue datedValue2 = DatedValue.builder()
            .value(new BigDecimal("750.75"))
            .date(date2)
            .build();
        
        UnitType multiplierUnit = UnitType.builder()
            .financialUnit(UnitType.FinancialUnit.CONTRACT)
            .build();
        
        Measure multiplier = Measure.builder()
            .value(new BigDecimal("1.5"))
            .unit(multiplierUnit)
            .build();
        
        Frequency frequency = Frequency.builder()
            .periodMultiplier(1)
            .period(Frequency.Period.M)
            .rollConvention(Frequency.RollConvention.EOM)
            .build();
        
        NonNegativeQuantity quantity = NonNegativeQuantity.builder()
            .value(new BigDecimal("1000.50"))
            .unit(unit)
            .datedValue(Arrays.asList(datedValue1, datedValue2))
            .multiplier(multiplier)
            .frequency(frequency)
            .build();
        
        // Verify the complete structure
        assertEquals("NonNegativeQuantity", quantity.getType());
        assertEquals(new BigDecimal("1000.50"), quantity.getValue());
        assertEquals("USD", quantity.getUnit().getCurrency().getValue());
        assertEquals(UnitType.CapacityUnit.BARREL, quantity.getUnit().getCapacityUnit());
        assertEquals(UnitType.WeatherUnit.DEGREE_CELSIUS, quantity.getUnit().getWeatherUnit());
        assertEquals(UnitType.FinancialUnit.SHARE, quantity.getUnit().getFinancialUnit());
        assertEquals("custom_unit_name", quantity.getUnit().getCustomUnit().getValue());
        assertEquals(2, quantity.getDatedValue().size());
        assertEquals(new BigDecimal("1.5"), quantity.getMultiplier().getValue());
        assertEquals(Frequency.Period.M, quantity.getFrequency().getPeriod());
    }
}
