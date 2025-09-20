package com.synthetics.core.cashflow.repository;

import com.synthetics.core.cashflow.entity.CashflowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Cashflow persistence operations
 */
@Repository
public interface CashflowRepository extends JpaRepository<CashflowEntity, String> {
    
    /**
     * Find cashflows by trade ID
     */
    List<CashflowEntity> findByTradeId(String tradeId);
    
    /**
     * Find cashflows by position ID
     */
    List<CashflowEntity> findByPositionId(String positionId);
    
    /**
     * Find cashflows by cashflow type
     */
    List<CashflowEntity> findByCashflowType(CashflowEntity.CashflowType cashflowType);
    
    /**
     * Find cashflows by status
     */
    List<CashflowEntity> findByCashflowStatus(CashflowEntity.CashflowStatus cashflowStatus);
    
    /**
     * Find cashflows by currency
     */
    List<CashflowEntity> findByCurrency(String currency);
    
    /**
     * Find cashflows by settlement date range
     */
    List<CashflowEntity> findBySettlementDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find cashflows with complex filtering
     */
    @Query("SELECT c FROM CashflowEntity c WHERE " +
           "(:tradeId IS NULL OR c.tradeId = :tradeId) AND " +
           "(:positionId IS NULL OR c.positionId = :positionId) AND " +
           "(:cashflowType IS NULL OR c.cashflowType = :cashflowType) AND " +
           "(:status IS NULL OR c.cashflowStatus = :status) AND " +
           "(:currency IS NULL OR c.currency = :currency) AND " +
           "(:settlementDateFrom IS NULL OR c.settlementDate >= :settlementDateFrom) AND " +
           "(:settlementDateTo IS NULL OR c.settlementDate <= :settlementDateTo)")
    Page<CashflowEntity> findCashflowsWithFilters(
            @Param("tradeId") String tradeId,
            @Param("positionId") String positionId,
            @Param("cashflowType") CashflowEntity.CashflowType cashflowType,
            @Param("status") CashflowEntity.CashflowStatus status,
            @Param("currency") String currency,
            @Param("settlementDateFrom") LocalDate settlementDateFrom,
            @Param("settlementDateTo") LocalDate settlementDateTo,
            Pageable pageable);
    
    /**
     * Count cashflows by trade ID
     */
    long countByTradeId(String tradeId);
    
    /**
     * Count cashflows by position ID
     */
    long countByPositionId(String positionId);
    
    /**
     * Find cashflows by origin event ID
     */
    List<CashflowEntity> findByOriginEventId(String originEventId);
}
