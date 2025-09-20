package com.synthetics.core.cdm.service;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Mock implementation of reference data service for development and testing
 */
@Slf4j
public class MockReferenceDataService implements ReferenceDataService {
    
    @Override
    public CompletableFuture<InterestRateCurve> getInterestRateCurve(String curveId, LocalDate date) {
        log.debug("Getting mock interest rate curve: {} for date: {}", curveId, date);
        
        Map<String, Double> rates = new HashMap<>();
        rates.put("1M", 0.05);
        rates.put("3M", 0.06);
        rates.put("6M", 0.07);
        rates.put("1Y", 0.08);
        rates.put("2Y", 0.09);
        rates.put("5Y", 0.10);
        rates.put("10Y", 0.11);
        
        InterestRateCurve curve = new InterestRateCurve(curveId, date, rates);
        
        return CompletableFuture.completedFuture(curve);
    }
    
    @Override
    public CompletableFuture<Fixing> getFixing(String indexId, LocalDate date) {
        log.debug("Getting mock fixing: {} for date: {}", indexId, date);
        
        // Mock fixing values based on index type
        Double fixingValue;
        switch (indexId.toUpperCase()) {
            case "LIBOR-USD-3M":
                fixingValue = 0.05 + (Math.random() * 0.02); // Random between 5-7%
                break;
            case "SOFR":
                fixingValue = 0.04 + (Math.random() * 0.01); // Random between 4-5%
                break;
            case "EURIBOR-3M":
                fixingValue = 0.03 + (Math.random() * 0.01); // Random between 3-4%
                break;
            default:
                fixingValue = 0.05; // Default 5%
        }
        
        Fixing fixing = new Fixing(indexId, date, fixingValue);
        
        return CompletableFuture.completedFuture(fixing);
    }
    
    @Override
    public CompletableFuture<Calendar> getBusinessCalendar(String calendarId) {
        log.debug("Getting mock business calendar: {}", calendarId);
        
        Set<LocalDate> holidays = new HashSet<>();
        // Add some common holidays
        holidays.add(LocalDate.of(2024, 1, 1));   // New Year's Day
        holidays.add(LocalDate.of(2024, 12, 25)); // Christmas Day
        holidays.add(LocalDate.of(2024, 12, 26)); // Boxing Day
        
        Set<Integer> businessDays = new HashSet<>();
        businessDays.add(DayOfWeek.MONDAY.getValue());
        businessDays.add(DayOfWeek.TUESDAY.getValue());
        businessDays.add(DayOfWeek.WEDNESDAY.getValue());
        businessDays.add(DayOfWeek.THURSDAY.getValue());
        businessDays.add(DayOfWeek.FRIDAY.getValue());
        
        Calendar calendar = new Calendar(calendarId, holidays, businessDays);
        
        return CompletableFuture.completedFuture(calendar);
    }
    
    @Override
    public CompletableFuture<Party> getParty(String partyId) {
        log.debug("Getting mock party: {}", partyId);
        
        Party party;
        switch (partyId) {
            case "BANK001":
                party = new Party("BANK001", "Mock Bank", "BANK");
                break;
            case "CLIENT001":
                party = new Party("CLIENT001", "Mock Client", "CORPORATION");
                break;
            case "CLIENT002":
                party = new Party("CLIENT002", "Mock Fund", "FUND");
                break;
            default:
                party = new Party(partyId, "Unknown Party", "OTHER");
        }
        
        return CompletableFuture.completedFuture(party);
    }
}
