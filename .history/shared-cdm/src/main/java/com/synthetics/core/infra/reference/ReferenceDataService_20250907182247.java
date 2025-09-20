package com.synthetics.core.infra.reference;

import com.synthetics.core.cdm.domain.Party;
import com.synthetics.core.cdm.domain.Underlier;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for reference data service
 * Provides access to market data, calendars, and party information
 */
public interface ReferenceDataService {
    
    /**
     * Get party information by party ID
     * @param partyId The party ID
     * @return CompletableFuture containing the party information
     */
    CompletableFuture<Party> getParty(String partyId);
    
    /**
     * Get underlier information by underlier ID
     * @param underlierId The underlier ID
     * @return CompletableFuture containing the underlier information
     */
    CompletableFuture<Underlier> getUnderlier(String underlierId);
    
    /**
     * Get interest rate for a given date and currency
     * @param currency The currency code
     * @param date The date
     * @return CompletableFuture containing the interest rate
     */
    CompletableFuture<Double> getInterestRate(String currency, LocalDate date);
    
    /**
     * Get dividend rate for an underlier on a given date
     * @param underlierId The underlier ID
     * @param date The date
     * @return CompletableFuture containing the dividend rate
     */
    CompletableFuture<Double> getDividendRate(String underlierId, LocalDate date);
    
    /**
     * Get business day calendar for a given currency
     * @param currency The currency code
     * @return CompletableFuture containing the business day calendar
     */
    CompletableFuture<List<LocalDate>> getBusinessDays(String currency);
    
    /**
     * Check if a date is a business day for a given currency
     * @param currency The currency code
     * @param date The date to check
     * @return CompletableFuture containing true if business day, false otherwise
     */
    CompletableFuture<Boolean> isBusinessDay(String currency, LocalDate date);
    
    /**
     * Get next business day for a given currency
     * @param currency The currency code
     * @param date The reference date
     * @return CompletableFuture containing the next business day
     */
    CompletableFuture<LocalDate> getNextBusinessDay(String currency, LocalDate date);
    
    /**
     * Get previous business day for a given currency
     * @param currency The currency code
     * @param date The reference date
     * @return CompletableFuture containing the previous business day
     */
    CompletableFuture<LocalDate> getPreviousBusinessDay(String currency, LocalDate date);
    
    /**
     * Get day count fraction between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @param dayCountConvention The day count convention
     * @return CompletableFuture containing the day count fraction
     */
    CompletableFuture<Double> getDayCountFraction(LocalDate startDate, LocalDate endDate, String dayCountConvention);
    
    /**
     * Get exchange rate between two currencies
     * @param fromCurrency The source currency
     * @param toCurrency The target currency
     * @param date The date
     * @return CompletableFuture containing the exchange rate
     */
    CompletableFuture<Double> getExchangeRate(String fromCurrency, String toCurrency, LocalDate date);
    
    /**
     * Get market data for an underlier
     * @param underlierId The underlier ID
     * @param date The date
     * @return CompletableFuture containing the market data
     */
    CompletableFuture<Double> getMarketData(String underlierId, LocalDate date);
    
    /**
     * Get volatility for an underlier
     * @param underlierId The underlier ID
     * @param date The date
     * @return CompletableFuture containing the volatility
     */
    CompletableFuture<Double> getVolatility(String underlierId, LocalDate date);
    
    /**
     * Get correlation between two underliers
     * @param underlierId1 The first underlier ID
     * @param underlierId2 The second underlier ID
     * @param date The date
     * @return CompletableFuture containing the correlation
     */
    CompletableFuture<Double> getCorrelation(String underlierId1, String underlierId2, LocalDate date);
}
