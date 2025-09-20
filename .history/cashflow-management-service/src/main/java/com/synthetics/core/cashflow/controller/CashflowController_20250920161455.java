package com.synthetics.core.cashflow.controller;

import com.synthetics.core.cashflow.service.CashflowEngine;
import com.synthetics.core.cashflow.service.CashflowPersistenceService;
import com.synthetics.core.cashflow.entity.CashflowEntity;
import com.synthetics.core.cdm.domain.Cashflow;
import com.synthetics.core.cdm.domain.Trade;
import com.synthetics.core.cdm.events.LifecycleEvent;
import com.synthetics.core.infra.metrics.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Cashflow Management operations
 * Provides endpoints for lifecycle event processing and cashflow management
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cashflow Management", description = "Cashflow generation and management operations")
public class CashflowController {

    private final CashflowEngine cashflowEngine;
    private final CashflowPersistenceService cashflowPersistenceService;
    private final MetricsService metricsService;
    private final ParallelProcessor parallelProcessor;

    @PostMapping("/lifecycle-events/simple")
    @Operation(
        summary = "Process simple lifecycle event (for testing)",
        description = "Simple endpoint to test lifecycle event processing with optional lot-based interest calculation"
    )
    public ResponseEntity<LifecycleEventResponse> processSimpleLifecycleEvent(
            @RequestBody java.util.Map<String, Object> eventData) {
        
        log.info("Processing simple lifecycle event: {}", eventData);
        
        try {
            // Check if lots are provided in the request
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> lotsData =
                (java.util.List<java.util.Map<String, Object>>) eventData.get("lots");

            java.util.List<com.synthetics.core.cdm.domain.Lot> lots = null;
            if (lotsData != null && !lotsData.isEmpty()) {
                log.info("Processing {} lots from request input", lotsData.size());
                lots = lotsData.stream()
                    .map(lotData -> com.synthetics.core.cdm.domain.Lot.builder()
                        .lotId((String) lotData.get("lotId"))
                        .positionId((String) lotData.get("positionId"))
                        .tradeId((String) eventData.get("tradeId"))
                        .quantity(new java.math.BigDecimal(lotData.get("quantity").toString()))
                        .unitPrice(new java.math.BigDecimal(lotData.get("unitPrice").toString()))
                        .currency((String) lotData.get("currency"))
                        .build())
                    .collect(java.util.stream.Collectors.toList());
            } else {
                // Create mock lots for testing when none provided
                log.info("No lots provided in request, using mock data");
                com.synthetics.core.cdm.domain.Lot mockLot = com.synthetics.core.cdm.domain.Lot.builder()
                    .lotId("LOT-001")
                    .positionId("POS-001")
                    .tradeId((String) eventData.get("tradeId"))
                    .quantity(new java.math.BigDecimal("1000"))
                    .unitPrice(new java.math.BigDecimal("150.50"))
                    .currency("USD")
                    .build();
                lots = java.util.List.of(mockLot);
            }

            // Calculate notional amount from lots if provided
            java.math.BigDecimal calculatedNotional = lots.stream()
                .map(lot -> lot.getQuantity().multiply(lot.getUnitPrice()))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            com.synthetics.core.cdm.domain.Position mockPosition = com.synthetics.core.cdm.domain.Position.builder()
                .positionId("POS-001")
                .tradeId((String) eventData.get("tradeId"))
                .underlierId("UND-001")
                .notionalAmount(calculatedNotional)
                .notionalCurrency("USD")
                .effectiveDate(java.time.LocalDate.now())
                .terminationDate(java.time.LocalDate.now().plusYears(1))
                .lots(lots)
                .build();
            
            // Create a mock trade for testing
            Trade mockTrade = Trade.builder()
                .tradeId((String) eventData.get("tradeId"))
                .productId("PROD-001")
                .tradeDate(java.time.LocalDate.now())
                .tradeTime(java.time.LocalDateTime.now())
                .positions(java.util.List.of(mockPosition))
                .build();
            
            List<Cashflow> generatedCashflows = cashflowEngine.generateCashflows(
                mockTrade, 
                java.time.LocalDate.now(), 
                (String) eventData.get("eventId")
            );
            
            // Save cashflows to database
            cashflowPersistenceService.saveCashflows(generatedCashflows);
            
            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId((String) eventData.get("eventId"));
            response.setStatus("PROCESSED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setGeneratedCashflows(generatedCashflows.stream()
                .map(Cashflow::getCashflowId)
                .toList());
            
            log.info("Successfully processed simple event: {} with {} cashflows generated", 
                eventData.get("eventId"), generatedCashflows.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing simple lifecycle event: {}", eventData.get("eventId"), e);
            
            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId((String) eventData.get("eventId"));
            response.setStatus("FAILED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setErrors(List.of(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/lifecycle-events/parallel")
    @Operation(
        summary = "Process lifecycle events in parallel",
        description = "Process multiple trades in parallel using thread pools for high-performance processing"
    )
    public ResponseEntity<LifecycleEventResponse> processLifecycleEventsParallel(
            @RequestBody java.util.List<java.util.Map<String, Object>> eventsData) {

        log.info("Processing {} lifecycle events in parallel", eventsData.size());

        try {
            // Create multiple trades for parallel processing
            java.util.List<Trade> trades = new java.util.ArrayList<>();
            String batchEventId = "BATCH-" + System.currentTimeMillis();

            for (int i = 0; i < eventsData.size(); i++) {
                java.util.Map<String, Object> eventData = eventsData.get(i);
                String tradeId = (String) eventData.get("tradeId");

                // Create positions and lots for each trade
                com.synthetics.core.cdm.domain.Lot lot = com.synthetics.core.cdm.domain.Lot.builder()
                    .lotId("LOT-" + i + "-1")
                    .positionId("POS-" + i)
                    .tradeId(tradeId)
                    .quantity(new java.math.BigDecimal("1000"))
                    .unitPrice(new java.math.BigDecimal("150.50"))
                    .currency("USD")
                    .build();

                com.synthetics.core.cdm.domain.Position position = com.synthetics.core.cdm.domain.Position.builder()
                    .positionId("POS-" + i)
                    .tradeId(tradeId)
                    .underlierId("UND-001")
                    .notionalAmount(new java.math.BigDecimal("150500"))
                    .notionalCurrency("USD")
                    .effectiveDate(java.time.LocalDate.now())
                    .terminationDate(java.time.LocalDate.now().plusYears(1))
                    .lots(java.util.List.of(lot))
                    .build();

                Trade trade = Trade.builder()
                    .tradeId(tradeId)
                    .productId("PROD-001")
                    .tradeDate(java.time.LocalDate.now())
                    .tradeTime(java.time.LocalDateTime.now())
                    .positions(java.util.List.of(position))
                    .build();

                trades.add(trade);
            }

            // Process trades in parallel
            java.util.concurrent.CompletableFuture<java.util.List<Cashflow>> futureCashflows =
                cashflowEngine.generateCashflowsParallel(trades, java.time.LocalDate.now(), batchEventId);

            // Wait for completion and collect results
            java.util.List<Cashflow> allCashflows = futureCashflows.get();
            cashflowPersistenceService.saveCashflows(allCashflows);

            // Record metrics
            metricsService.recordCashflowGeneration("PARALLEL_BATCH", String.valueOf(allCashflows.size()), "USD");

            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId(batchEventId);
            response.setStatus("PARALLEL_PROCESSED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setGeneratedCashflows(allCashflows.stream()
                .map(Cashflow::getCashflowId)
                .collect(java.util.stream.Collectors.toList()));

            log.info("Successfully processed {} events in parallel with {} cashflows generated",
                eventsData.size(), allCashflows.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing lifecycle events in parallel", e);

            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId("BATCH-ERROR-" + System.currentTimeMillis());
            response.setStatus("PARALLEL_FAILED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setErrors(java.util.List.of(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/lifecycle-events")
    @Operation(
        summary = "Process lifecycle event",
        description = "Processes a lifecycle event and generates corresponding cashflows. " +
                     "Supports NEW_TRADE, AMENDMENT, PARTIAL_UNWIND, TERMINATION, and FIXING_PUBLISHED events."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event processed successfully",
            content = @Content(schema = @Schema(implementation = LifecycleEventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid event data"),
        @ApiResponse(responseCode = "422", description = "Event validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LifecycleEventResponse> processLifecycleEvent(
            @RequestBody LifecycleEvent event) {
        
        log.info("Processing lifecycle event: {} for trade: {}", event.getEventId(), event.getTradeId());
        
        try {
            // For now, create a mock trade and generate cashflows
            // TODO: Implement proper lifecycle event processing
            Trade mockTrade = Trade.builder()
                .tradeId(event.getTradeId())
                .productId("PROD-001")
                .tradeDate(java.time.LocalDate.now())
                .tradeTime(java.time.LocalDateTime.now())
                .build();
            
            List<Cashflow> generatedCashflows = cashflowEngine.generateCashflows(
                mockTrade, 
                java.time.LocalDate.now(), 
                event.getEventId()
            );
            
            // Save cashflows to database
            cashflowPersistenceService.saveCashflows(generatedCashflows);
            
            // Record metrics
            metricsService.recordCashflowGeneration(
                event.getEventType(),
                String.valueOf(generatedCashflows.size()),
                "USD" // Default currency for now
            );
            
            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId(event.getEventId());
            response.setStatus("PROCESSED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setGeneratedCashflows(generatedCashflows.stream()
                .map(Cashflow::getCashflowId)
                .toList());
            
            log.info("Successfully processed event: {} with {} cashflows generated", 
                event.getEventId(), generatedCashflows.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing lifecycle event: {}", event.getEventId(), e);
            
            LifecycleEventResponse response = new LifecycleEventResponse();
            response.setEventId(event.getEventId());
            response.setStatus("FAILED");
            response.setProcessedAt(java.time.LocalDateTime.now());
            response.setErrors(List.of(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/lifecycle-events/{eventId}")
    @Operation(
        summary = "Get lifecycle event by ID",
        description = "Retrieves a specific lifecycle event by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event found"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<LifecycleEventResponse> getLifecycleEvent(
            @Parameter(description = "Event identifier") @PathVariable("eventId") String eventId) {
        
        log.info("Retrieving lifecycle event: {}", eventId);
        
        // TODO: Implement event retrieval from event store
        // For now, return a mock response
        LifecycleEventResponse response = new LifecycleEventResponse();
        response.setEventId(eventId);
        response.setStatus("PROCESSED");
        response.setProcessedAt(java.time.LocalDateTime.now());
        response.setGeneratedCashflows(List.of("CF-001", "CF-002"));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cashflows")
    @Operation(
        summary = "List cashflows",
        description = "Retrieves cashflows with optional filtering and pagination"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cashflows retrieved successfully")
    })
    public ResponseEntity<CashflowListResponse> listCashflows(
            @Parameter(description = "Filter by trade ID") @RequestParam(value = "tradeId", required = false) String tradeId,
            @Parameter(description = "Filter by position ID") @RequestParam(value = "positionId", required = false) String positionId,
            @Parameter(description = "Filter by cashflow type") @RequestParam(value = "cashflowType", required = false) String cashflowType,
            @Parameter(description = "Filter by status") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Filter by currency") @RequestParam(value = "currency", required = false) String currency,
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "100") int size) {
        
        log.info("Listing cashflows with filters - tradeId: {}, positionId: {}, type: {}, status: {}, currency: {}, page: {}, size: {}", 
            tradeId, positionId, cashflowType, status, currency, page, size);
        
        // Retrieve cashflows from database with filtering
        org.springframework.data.domain.Page<CashflowEntity> cashflowPage = cashflowPersistenceService.findCashflowsWithFilters(
            tradeId, positionId, cashflowType, status, currency, null, null, page, size);
        
        // Convert entities to domain objects
        List<Cashflow> cashflows = cashflowPage.getContent().stream()
            .map(this::convertToDomain)
            .toList();
        
        CashflowListResponse response = new CashflowListResponse();
        response.setCashflows(cashflows);
        response.setTotalElements((int) cashflowPage.getTotalElements());
        response.setTotalPages(cashflowPage.getTotalPages());
        response.setCurrentPage(page);
        response.setPageSize(size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cashflows/{cashflowId}")
    @Operation(
        summary = "Get cashflow by ID",
        description = "Retrieves a specific cashflow by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cashflow found"),
        @ApiResponse(responseCode = "404", description = "Cashflow not found")
    })
    public ResponseEntity<Cashflow> getCashflow(
            @Parameter(description = "Cashflow identifier") @PathVariable("cashflowId") String cashflowId) {
        
        log.info("Retrieving cashflow: {}", cashflowId);
        
        // Retrieve cashflow from database
        java.util.Optional<CashflowEntity> optionalEntity = cashflowPersistenceService.findCashflowById(cashflowId);
        
        if (optionalEntity.isPresent()) {
            Cashflow cashflow = convertToDomain(optionalEntity.get());
            return ResponseEntity.ok(cashflow);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/cashflows/{cashflowId}/status")
    @Operation(
        summary = "Update cashflow status",
        description = "Updates the status of a cashflow (e.g., mark as paid, settled)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Cashflow not found")
    })
    public ResponseEntity<Cashflow> updateCashflowStatus(
            @Parameter(description = "Cashflow identifier") @PathVariable("cashflowId") String cashflowId,
            @RequestBody CashflowStatusUpdate statusUpdate) {
        
        log.info("Updating cashflow status: {} to {}", cashflowId, statusUpdate.getStatus());
        
        // Update cashflow status in database
        java.util.Optional<CashflowEntity> optionalEntity = cashflowPersistenceService.updateCashflowStatus(
            cashflowId, statusUpdate.getStatus());
        
        if (optionalEntity.isPresent()) {
            Cashflow cashflow = convertToDomain(optionalEntity.get());
            return ResponseEntity.ok(cashflow);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Returns the health status of the service"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<HealthResponse> healthCheck() {
        log.debug("Health check requested");
        
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setComponents(Map.of(
            "database", Map.of("status", "UP", "details", Map.of()),
            "messaging", Map.of("status", "UP", "details", Map.of()),
            "referenceData", Map.of("status", "UP", "details", Map.of())
        ));
        response.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    @Operation(
        summary = "Get business metrics",
        description = "Returns key business metrics for the service"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    })
    public ResponseEntity<MetricsResponse> getBusinessMetrics() {
        log.debug("Business metrics requested");
        
        MetricsResponse response = new MetricsResponse();
        response.setTotalTradesProcessed(0);
        response.setTotalPositionsProcessed(0);
        response.setTotalCashflowsGenerated(0);
        response.setTotalSettlementInstructions(0);
        response.setAverageProcessingTimeMs(0.0);
        response.setThroughputPerMinute(0.0);
        response.setErrorRate(0.0);
        response.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    // Response DTOs
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LifecycleEventResponse {
        private String eventId;
        private String status;
        private java.time.LocalDateTime processedAt;
        private List<String> generatedCashflows;
        private List<String> errors;
        private Long processingTimeMs;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CashflowListResponse {
        private List<Cashflow> cashflows;
        private Integer totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CashflowStatusUpdate {
        private String status;
        private String settlementReference;
        private String notes;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthResponse {
        private String status;
        private Map<String, Object> components;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MetricsResponse {
        private Integer totalTradesProcessed;
        private Integer totalPositionsProcessed;
        private Integer totalCashflowsGenerated;
        private Integer totalSettlementInstructions;
        private Double averageProcessingTimeMs;
        private Double throughputPerMinute;
        private Double errorRate;
        private java.time.LocalDateTime timestamp;
    }
    
    /**
     * Convert CashflowEntity to Cashflow domain object
     */
    private Cashflow convertToDomain(CashflowEntity entity) {
        return Cashflow.builder()
                .cashflowId(entity.getCashflowId())
                .tradeId(entity.getTradeId())
                .positionId(entity.getPositionId())
                .lotId(entity.getLotId())
                .cashflowType(convertCashflowType(entity.getCashflowType()))
                .payerPartyId(entity.getPayerPartyId())
                .receiverPartyId(entity.getReceiverPartyId())
                .currency(entity.getCurrency())
                .amount(entity.getAmount())
                .settlementDate(entity.getSettlementDate())
                .paymentType(convertPaymentType(entity.getPaymentType()))
                .cashflowStatus(convertCashflowStatus(entity.getCashflowStatus()))
                .statusTransitionTimestamp(entity.getStatusTransitionTimestamp())
                .settlementReference(entity.getSettlementReference())
                .originEventId(entity.getOriginEventId())
                .revisionNumber(entity.getRevisionNumber())
                .isCancelled(entity.getIsCancelled())
                .createdTimestamp(entity.getCreatedTimestamp())
                .updatedTimestamp(entity.getUpdatedTimestamp())
                .build();
    }
    
    private Cashflow.CashflowType convertCashflowType(CashflowEntity.CashflowType type) {
        return Cashflow.CashflowType.valueOf(type.name());
    }
    
    private Cashflow.PaymentType convertPaymentType(CashflowEntity.PaymentType type) {
        return Cashflow.PaymentType.valueOf(type.name());
    }
    
    private Cashflow.CashflowStatus convertCashflowStatus(CashflowEntity.CashflowStatus status) {
        return Cashflow.CashflowStatus.valueOf(status.name());
    }
}
