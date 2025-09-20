# CDM Math Package

This package provides comprehensive mathematical data structures and utilities based on the FINOS CDM Rosetta standard for financial data modeling.

## Overview

The math package contains classes for representing and manipulating financial quantities, measurements, dates, and frequencies in a standardized way that ensures consistency across the cashflow management system.

## Core Classes

### NonNegativeQuantity
Represents a measurable amount with associated units and metadata.

```java
NonNegativeQuantity quantity = NonNegativeQuantity.builder()
    .value(new BigDecimal("1000.50"))
    .unit(unitType)
    .datedValue(Arrays.asList(datedValue1, datedValue2))
    .multiplier(multiplier)
    .frequency(frequency)
    .build();
```

### UnitType
Defines different types of measurement units including:
- **Capacity Units**: BARREL, CUBIC_METER, GALLON, etc.
- **Weather Units**: DEGREE_CELSIUS, DEGREE_FAHRENHEIT, etc.
- **Financial Units**: SHARE, CONTRACT, LOT, BOND, etc.
- **Currency**: USD, EUR, GBP, JPY, etc.
- **Custom Units**: User-defined units

### DatedValue
Represents a value associated with a specific date, supporting business day adjustments.

```java
DatedValue datedValue = DatedValue.builder()
    .value(new BigDecimal("500.25"))
    .date(adjustableDate)
    .build();
```

### AdjustableDate
Handles date adjustments based on business day conventions:
- **FOLLOWING**: Move to next business day
- **MODIFIED_FOLLOWING**: Move to next business day, or previous if month changes
- **PRECEDING**: Move to previous business day
- **MODIFIED_PRECEDING**: Move to previous business day, or next if month changes
- **NEAREST**: Move to nearest business day

### BusinessDayAdjustments
Configures business day adjustment rules with support for multiple business centers.

### Measure
Represents a value with an associated unit type.

### Frequency
Defines periodic frequency for recurring events:
- **Periods**: D (Day), W (Week), M (Month), Q (Quarter), S (Semi-Annual), Y (Year)
- **Roll Conventions**: EOM (End of Month), SOM (Start of Month), IMM (IMM Date)

## Utility Functions

### MathUtils
Provides mathematical operations on CDM objects:

```java
// Add two quantities
NonNegativeQuantity sum = MathUtils.add(qty1, qty2);

// Multiply quantity by scalar
NonNegativeQuantity result = MathUtils.multiply(qty, multiplier);

// Calculate weighted average
BigDecimal avg = MathUtils.calculateWeightedAverage(values, weights);

// Calculate time-weighted average
BigDecimal timeAvg = MathUtils.calculateTimeWeightedAverage(values, startDate, endDate);

// Create currency quantity
NonNegativeQuantity usdQty = MathUtils.createCurrencyQuantity(new BigDecimal("100.00"), "USD");

// Create financial quantity
NonNegativeQuantity shareQty = MathUtils.createFinancialQuantity(new BigDecimal("1000"), FinancialUnit.SHARE);
```

## Usage Examples

### Creating a Currency Quantity
```java
NonNegativeQuantity usdAmount = MathUtils.createCurrencyQuantity(
    new BigDecimal("1000.50"), 
    "USD"
);
```

### Creating a Financial Quantity
```java
NonNegativeQuantity shareQuantity = MathUtils.createFinancialQuantity(
    new BigDecimal("100"), 
    UnitType.FinancialUnit.SHARE
);
```

### Working with Dated Values
```java
// Create business day adjustments
BusinessDayAdjustments adjustments = BusinessDayAdjustments.builder()
    .businessDayConvention(BusinessDayAdjustments.BusinessDayConvention.FOLLOWING)
    .businessCenters(BusinessCenters.builder()
        .businessCenter(Arrays.asList("USNY", "GBLO"))
        .build())
    .build();

// Create adjustable date
AdjustableDate date = AdjustableDate.builder()
    .unadjustedDate(LocalDate.of(2024, 1, 15))
    .dateAdjustments(adjustments)
    .build();

// Create dated value
DatedValue datedValue = DatedValue.builder()
    .value(new BigDecimal("500.25"))
    .date(date)
    .build();
```

### Complex Quantity with All Components
```java
NonNegativeQuantity complexQuantity = NonNegativeQuantity.builder()
    .value(new BigDecimal("1000.50"))
    .unit(UnitType.builder()
        .capacityUnit(UnitType.CapacityUnit.BARREL)
        .weatherUnit(UnitType.WeatherUnit.DEGREE_CELSIUS)
        .financialUnit(UnitType.FinancialUnit.SHARE)
        .currency(Currency.builder().value("USD").build())
        .customUnit(FieldWithMetaString.builder().value("custom_unit").build())
        .build())
    .datedValue(Arrays.asList(datedValue1, datedValue2))
    .multiplier(Measure.builder()
        .value(new BigDecimal("1.5"))
        .unit(UnitType.builder().financialUnit(UnitType.FinancialUnit.CONTRACT).build())
        .build())
    .frequency(Frequency.builder()
        .periodMultiplier(1)
        .period(Frequency.Period.M)
        .rollConvention(Frequency.RollConvention.EOM)
        .build())
    .build();
```

## Validation

All classes include comprehensive validation:
- **NonNegativeQuantity**: Value must be non-negative
- **Currency**: Must be exactly 3 characters
- **Frequency**: Period multiplier must be at least 1
- **DatedValue**: Value and date are required

## JSON Serialization

All classes support JSON serialization/deserialization with proper `@JsonProperty` annotations and `@type` fields for polymorphic handling.

## Business Day Calculations

The `BusinessDayAdjustments` class provides sophisticated business day calculation logic that considers:
- Weekend exclusions (Saturday/Sunday)
- Business center configurations
- Various business day conventions
- Month boundary handling

## Thread Safety

All classes are immutable and thread-safe by design, using Lombok's `@Builder` pattern and immutable data structures.

## Testing

Comprehensive test coverage is provided in `MathPackageTest.java` demonstrating:
- Basic object creation
- Complex nested structures
- Mathematical operations
- Business day adjustments
- Complete JSON example implementation



