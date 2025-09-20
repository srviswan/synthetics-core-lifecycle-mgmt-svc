package com.synthetics.core.cashflow.service;

import com.synthetics.core.cashflow.entity.CashflowEntity;
import com.synthetics.core.cashflow.repository.CashflowRepository;
import com.synthetics.core.cdm.domain.Cashflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for cashflow persistence operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashflowPersistenceService {
    
    private final CashflowRepository cashflowRepository;
    
    /**
     * Save a cashflow to the database
     */
    @Transactional
    public CashflowEntity saveCashflow(Cashflow cashflow) {
        log.debug("Saving cashflow: {}", cashflow.getCashflowId());
        
        CashflowEntity entity = convertToEntity(cashflow);
        CashflowEntity saved = cashflowRepository.save(entity);
        
        log.debug("Successfully saved cashflow: {}", saved.getCashflowId());
        return saved;
    }
    
    /**
     * Save multiple cashflows to the database
     */
    @Transactional
    public List<CashflowEntity> saveCashflows(List<Cashflow> cashflows) {
        log.info("Saving {} cashflows to database", cashflows.size());
        
        List<CashflowEntity> entities = cashflows.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        List<CashflowEntity> saved = cashflowRepository.saveAll(entities);
        
        log.info("Successfully saved {} cashflows to database", saved.size());
        return saved;
    }
    
    /**
     * Find cashflow by ID
     */
    public Optional<CashflowEntity> findCashflowById(String cashflowId) {
        log.debug("Finding cashflow by ID: {}", cashflowId);
        return cashflowRepository.findById(cashflowId);
    }
    
    /**
     * Find cashflows with filtering and pagination
     */
    public Page<CashflowEntity> findCashflowsWithFilters(
            String tradeId,
            String positionId,
            String cashflowType,
            String status,
            String currency,
            LocalDate settlementDateFrom,
            LocalDate settlementDateTo,
            int page,
            int size) {
        
        log.debug("Finding cashflows with filters - tradeId: {}, positionId: {}, type: {}, status: {}, currency: {}, page: {}, size: {}", 
            tradeId, positionId, cashflowType, status, currency, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        
        CashflowEntity.CashflowType typeEnum = null;
        if (cashflowType != null) {
            try {
                typeEnum = CashflowEntity.CashflowType.valueOf(cashflowType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid cashflow type: {}", cashflowType);
            }
        }
        
        CashflowEntity.CashflowStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = CashflowEntity.CashflowStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid cashflow status: {}", status);
            }
        }
        
        return cashflowRepository.findCashflowsWithFilters(
            tradeId, positionId, typeEnum, statusEnum, currency, 
            settlementDateFrom, settlementDateTo, pageable);
    }
    
    /**
     * Find cashflows by trade ID
     */
    public List<CashflowEntity> findCashflowsByTradeId(String tradeId) {
        log.debug("Finding cashflows by trade ID: {}", tradeId);
        return cashflowRepository.findByTradeId(tradeId);
    }
    
    /**
     * Find cashflows by position ID
     */
    public List<CashflowEntity> findCashflowsByPositionId(String positionId) {
        log.debug("Finding cashflows by position ID: {}", positionId);
        return cashflowRepository.findByPositionId(positionId);
    }
    
    /**
     * Find cashflows by origin event ID
     */
    public List<CashflowEntity> findCashflowsByOriginEventId(String originEventId) {
        log.debug("Finding cashflows by origin event ID: {}", originEventId);
        return cashflowRepository.findByOriginEventId(originEventId);
    }
    
    /**
     * Update cashflow status
     */
    @Transactional
    public Optional<CashflowEntity> updateCashflowStatus(String cashflowId, String newStatus) {
        log.info("Updating cashflow status: {} to {}", cashflowId, newStatus);
        
        Optional<CashflowEntity> optionalEntity = cashflowRepository.findById(cashflowId);
        if (optionalEntity.isPresent()) {
            CashflowEntity entity = optionalEntity.get();
            
            try {
                CashflowEntity.CashflowStatus statusEnum = CashflowEntity.CashflowStatus.valueOf(newStatus);
                entity.setCashflowStatus(statusEnum);
                entity.setStatusTransitionTimestamp(java.time.LocalDateTime.now());
                
                CashflowEntity updated = cashflowRepository.save(entity);
                log.info("Successfully updated cashflow status: {} to {}", cashflowId, newStatus);
                return Optional.of(updated);
            } catch (IllegalArgumentException e) {
                log.error("Invalid cashflow status: {}", newStatus);
                return Optional.empty();
            }
        } else {
            log.warn("Cashflow not found: {}", cashflowId);
            return Optional.empty();
        }
    }
    
    /**
     * Convert CDM Cashflow to JPA Entity
     */
    private CashflowEntity convertToEntity(Cashflow cashflow) {
        return CashflowEntity.builder()
                .cashflowId(cashflow.getCashflowId())
                .tradeId(cashflow.getTradeId())
                .positionId(cashflow.getPositionId())
                .lotId(cashflow.getLotId())
                .cashflowType(convertCashflowType(cashflow.getCashflowType()))
                .payerPartyId(cashflow.getPayerPartyId())
                .receiverPartyId(cashflow.getReceiverPartyId())
                .currency(cashflow.getCurrency())
                .amount(cashflow.getAmount())
                .settlementDate(cashflow.getSettlementDate())
                .paymentType(convertPaymentType(cashflow.getPaymentType()))
                .cashflowStatus(convertCashflowStatus(cashflow.getCashflowStatus()))
                .statusTransitionTimestamp(cashflow.getStatusTransitionTimestamp())
                .settlementReference(cashflow.getSettlementReference())
                .originEventId(cashflow.getOriginEventId())
                .revisionNumber(cashflow.getRevisionNumber())
                .isCancelled(cashflow.getIsCancelled())
                .createdTimestamp(cashflow.getCreatedTimestamp())
                .updatedTimestamp(cashflow.getUpdatedTimestamp())
                .build();
    }
    
    private CashflowEntity.CashflowType convertCashflowType(Cashflow.CashflowType type) {
        return CashflowEntity.CashflowType.valueOf(type.name());
    }
    
    private CashflowEntity.PaymentType convertPaymentType(Cashflow.PaymentType type) {
        return CashflowEntity.PaymentType.valueOf(type.name());
    }
    
    private CashflowEntity.CashflowStatus convertCashflowStatus(Cashflow.CashflowStatus status) {
        return CashflowEntity.CashflowStatus.valueOf(status.name());
    }
}
