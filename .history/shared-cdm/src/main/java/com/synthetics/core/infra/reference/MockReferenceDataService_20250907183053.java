package com.synthetics.core.infra.reference;

import com.synthetics.core.cdm.domain.Party;
import com.synthetics.core.cdm.domain.Underlier;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Mock implementation of reference data service for development and testing
 * Provides simulated reference data for all required operations
 */
@Slf4j
public class MockReferenceDataService implements ReferenceDataService {
    
    @Override
    public CompletableFuture<Party> getParty(String partyId) {
        log.debug("Mock: Getting party information for ID: {}", partyId);
        
        // Simulate async operation
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return Party.builder()
                .partyId(partyId)
                .partyName("Mock Party " + partyId)
                .partyType(Party.PartyType.BANK)
                .leiCode("MOCK" + partyId)
                .countryOfIncorporation("US")
                .isActive(true)
                .build();
        });
    }
    
    @Override
    public CompletableFuture<Underlier> getUnderlier(String underlierId) {
        log.debug("Mock: Getting underlier information for ID: {}", underlierId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return Underlier.builder()
                .underlierId(underlierId)
                .assetType(Underlier.AssetType.SINGLE_NAME)
                .primaryIdentifier("MOCK" + underlierId)
                .identifierType(Underlier.IdentifierType.ISIN)
                .assetName("Mock Asset " + underlierId)
                .currency("USD")
                .exchange("NYSE")
                .country("US")
                .isActive(true)
                .build();
        });
    }
    
    @Override
    public CompletableFuture<Double> getInterestRate(String currency, LocalDate date) {
        log.debug("Mock: Getting interest rate for currency: {} on date: {}", currency, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(30); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock interest rates based on currency
            return switch (currency) {
                case "USD" -> 0.05; // 5%
                case "EUR" -> 0.03; // 3%
                case "GBP" -> 0.04; // 4%
                default -> 0.02; // 2%
            };
        });
    }
    
    @Override
    public CompletableFuture<Double> getDividendRate(String underlierId, LocalDate date) {
        log.debug("Mock: Getting dividend rate for underlier: {} on date: {}", underlierId, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(30); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock dividend rate (2-4% range)
            return 0.02 + (Math.random() * 0.02);
        });
    }
    
    @Override
    public CompletableFuture<List<LocalDate>> getBusinessDays(String currency) {
        log.debug("Mock: Getting business days for currency: {}", currency);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock business days (weekdays only for simplicity)
            List<LocalDate> businessDays = new ArrayList<>();
            LocalDate startDate = LocalDate.now().minusYears(1);
            LocalDate endDate = LocalDate.now().plusYears(1);
            
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (date.getDayOfWeek().getValue() <= 5) { // Monday to Friday
                    businessDays.add(date);
                }
            }
            
            return businessDays;
        });
    }
    
    @Override
    public CompletableFuture<Boolean> isBusinessDay(String currency, LocalDate date) {
        log.debug("Mock: Checking if date {} is business day for currency: {}", date, currency);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(20); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock business day check (weekdays only)
            return date.getDayOfWeek().getValue() <= 5;
        });
    }
    
    @Override
    public CompletableFuture<LocalDate> getNextBusinessDay(String currency, LocalDate date) {
        log.debug("Mock: Getting next business day for currency: {} from date: {}", currency, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(20); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            LocalDate nextDay = date.plusDays(1);
            while (nextDay.getDayOfWeek().getValue() > 5) {
                nextDay = nextDay.plusDays(1);
            }
            return nextDay;
        });
    }
    
    @Override
    public CompletableFuture<LocalDate> getPreviousBusinessDay(String currency, LocalDate date) {
        log.debug("Mock: Getting previous business day for currency: {} from date: {}", currency, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(20); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            LocalDate prevDay = date.minusDays(1);
            while (prevDay.getDayOfWeek().getValue() > 5) {
                prevDay = prevDay.minusDays(1);
            }
            return prevDay;
        });
    }
    
    @Override
    public CompletableFuture<Double> getDayCountFraction(LocalDate startDate, LocalDate endDate, String dayCountConvention) {
        log.debug("Mock: Getting day count fraction from {} to {} with convention: {}", startDate, endDate, dayCountConvention);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(30); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock day count fraction calculation
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
            return switch (dayCountConvention) {
                case "ACT/360" -> daysBetween / 360.0;
                case "ACT/365" -> daysBetween / 365.0;
                case "30/360" -> daysBetween / 360.0;
                default -> daysBetween / 365.0;
            };
        });
    }
    
    @Override
    public CompletableFuture<Double> getExchangeRate(String fromCurrency, String toCurrency, LocalDate date) {
        log.debug("Mock: Getting exchange rate from {} to {} on date: {}", fromCurrency, toCurrency, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(40); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock exchange rates
            if (fromCurrency.equals(toCurrency)) {
                return 1.0;
            }
            
            return switch (fromCurrency + "/" + toCurrency) {
                case "USD/EUR" -> 0.85;
                case "EUR/USD" -> 1.18;
                case "USD/GBP" -> 0.75;
                case "GBP/USD" -> 1.33;
                default -> 1.0;
            };
        });
    }
    
    @Override
    public CompletableFuture<Double> getMarketData(String underlierId, LocalDate date) {
        log.debug("Mock: Getting market data for underlier: {} on date: {}", underlierId, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock market data (price between 50-200)
            return 50.0 + (Math.random() * 150.0);
        });
    }
    
    @Override
    public CompletableFuture<Double> getVolatility(String underlierId, LocalDate date) {
        log.debug("Mock: Getting volatility for underlier: {} on date: {}", underlierId, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(40); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock volatility (15-35% range)
            return 0.15 + (Math.random() * 0.20);
        });
    }
    
    @Override
    public CompletableFuture<Double> getCorrelation(String underlierId1, String underlierId2, LocalDate date) {
        log.debug("Mock: Getting correlation between {} and {} on date: {}", underlierId1, underlierId2, date);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(60); // Simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Mock correlation (-0.5 to 0.9 range)
            return -0.5 + (Math.random() * 1.4);
        });
    }
}
