package com.synthetics.core.cashflow.repository;

import com.synthetics.core.cashflow.entity.InterestNotionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Interest Notional persistence operations
 */
@Repository
public interface InterestNotionalRepository extends JpaRepository<InterestNotionalEntity, String> {

    /**
     * Find interest notional by position ID
     */
    Optional<InterestNotionalEntity> findByPositionId(String positionId);

    /**
     * Check if position has lot-based calculation method
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
           "FROM InterestNotionalEntity i WHERE i.positionId = :positionId " +
           "AND i.calculationMethod = 'LOT_BASED'")
    boolean hasLotBasedCalculation(@Param("positionId") String positionId);

    /**
     * Get interest notional amount for position
     */
    @Query("SELECT i.interestNotionalAmount FROM InterestNotionalEntity i WHERE i.positionId = :positionId")
    java.util.Optional<java.math.BigDecimal> getInterestNotionalAmount(@Param("positionId") String positionId);
}
