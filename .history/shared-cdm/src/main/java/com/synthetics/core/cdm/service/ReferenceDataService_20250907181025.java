package com.synthetics.core.cdm.service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for reference data service
 * Supports both mock and real implementations
 */
public interface ReferenceDataService {
    
    /**
     * Get interest rate curve
     * @param curveId The curve identifier
     * @param date The valuation date
     * @return CompletableFuture containing the interest rate curve
     */
    CompletableFuture<InterestRateCurve> getInterestRateCurve(String curveId, LocalDate date);
    
    /**
     * Get fixing value
     * @param indexId The index identifier
     * @param date The fixing date
     * @return CompletableFuture containing the fixing value
     */
    CompletableFuture<Fixing> getFixing(String indexId, LocalDate date);
    
    /**
     * Get business calendar
     * @param calendarId The calendar identifier
     * @return CompletableFuture containing the business calendar
     */
    CompletableFuture<Calendar> getBusinessCalendar(String calendarId);
    
    /**
     * Get party information
     * @param partyId The party identifier
     * @return CompletableFuture containing the party information
     */
    CompletableFuture<Party> getParty(String partyId);
    
    /**
     * Interest rate curve data
     */
    class InterestRateCurve {
        private String curveId;
        private LocalDate curveDate;
        private java.util.Map<String, Double> rates;
        
        public InterestRateCurve(String curveId, LocalDate curveDate, java.util.Map<String, Double> rates) {
            this.curveId = curveId;
            this.curveDate = curveDate;
            this.rates = rates;
        }
        
        public String getCurveId() { return curveId; }
        public LocalDate getCurveDate() { return curveDate; }
        public java.util.Map<String, Double> getRates() { return rates; }
    }
    
    /**
     * Fixing data
     */
    class Fixing {
        private String indexId;
        private LocalDate fixingDate;
        private Double fixingValue;
        
        public Fixing(String indexId, LocalDate fixingDate, Double fixingValue) {
            this.indexId = indexId;
            this.fixingDate = fixingDate;
            this.fixingValue = fixingValue;
        }
        
        public String getIndexId() { return indexId; }
        public LocalDate getFixingDate() { return fixingDate; }
        public Double getFixingValue() { return fixingValue; }
    }
    
    /**
     * Business calendar data
     */
    class Calendar {
        private String calendarId;
        private java.util.Set<LocalDate> holidays;
        private java.util.Set<Integer> businessDays;
        
        public Calendar(String calendarId, java.util.Set<LocalDate> holidays, java.util.Set<Integer> businessDays) {
            this.calendarId = calendarId;
            this.holidays = holidays;
            this.businessDays = businessDays;
        }
        
        public String getCalendarId() { return calendarId; }
        public java.util.Set<LocalDate> getHolidays() { return holidays; }
        public java.util.Set<Integer> getBusinessDays() { return businessDays; }
        
        public boolean isBusinessDay(LocalDate date) {
            return !holidays.contains(date) && businessDays.contains(date.getDayOfWeek().getValue());
        }
    }
    
    /**
     * Party data
     */
    class Party {
        private String partyId;
        private String partyName;
        private String partyType;
        
        public Party(String partyId, String partyName, String partyType) {
            this.partyId = partyId;
            this.partyName = partyName;
            this.partyType = partyType;
        }
        
        public String getPartyId() { return partyId; }
        public String getPartyName() { return partyName; }
        public String getPartyType() { return partyType; }
    }
}
