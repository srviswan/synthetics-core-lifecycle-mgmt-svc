package com.synthetics.core.cashflow.controller;

import com.synthetics.core.cashflow.service.CashflowEngine;
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
    private final MetricsService metricsService;

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
            
            LifecycleEventResponse response = LifecycleEventResponse.builder()
                .eventId(event.getEventId())
                .status("FAILED")
                .processedAt(java.time.LocalDateTime.now())
                .errors(List.of(e.getMessage()))
                .build();
            
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
            @Parameter(description = "Event identifier") @PathVariable String eventId) {
        
        log.info("Retrieving lifecycle event: {}", eventId);
        
        // TODO: Implement event retrieval from event store
        // For now, return a mock response
        LifecycleEventResponse response = LifecycleEventResponse.builder()
            .eventId(eventId)
            .status("PROCESSED")
            .processedAt(java.time.LocalDateTime.now())
            .generatedCashflows(List.of("CF-001", "CF-002"))
            .build();
        
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
            @Parameter(description = "Filter by trade ID") @RequestParam(required = false) String tradeId,
            @Parameter(description = "Filter by position ID") @RequestParam(required = false) String positionId,
            @Parameter(description = "Filter by cashflow type") @RequestParam(required = false) String cashflowType,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "100") int size) {
        
        log.info("Listing cashflows with filters - tradeId: {}, positionId: {}, type: {}, status: {}, currency: {}, page: {}, size: {}", 
            tradeId, positionId, cashflowType, status, currency, page, size);
        
        // TODO: Implement cashflow retrieval from database
        // For now, return a mock response
        CashflowListResponse response = new CashflowListResponse();
        response.setCashflows(List.of());
        response.setTotalElements(0);
        response.setTotalPages(0);
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
            @Parameter(description = "Cashflow identifier") @PathVariable String cashflowId) {
        
        log.info("Retrieving cashflow: {}", cashflowId);
        
        // TODO: Implement cashflow retrieval from database
        // For now, return a mock response
        Cashflow cashflow = Cashflow.builder()
            .cashflowId(cashflowId)
            .tradeId("TRD-001")
            .positionId("POS-001")
            .cashflowType(Cashflow.CashflowType.ACCRUAL)
            .payerPartyId("PARTY-001")
            .receiverPartyId("PARTY-002")
            .currency("USD")
            .amount(new java.math.BigDecimal("1000.00"))
            .settlementDate(java.time.LocalDate.now().plusDays(2))
            .paymentType(Cashflow.PaymentType.PRINCIPAL)
            .cashflowStatus(Cashflow.CashflowStatus.ACCRUAL)
            .statusTransitionTimestamp(java.time.LocalDateTime.now())
            .originEventId("EVT-001")
            .revisionNumber(1)
            .build();
        
        return ResponseEntity.ok(cashflow);
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
            @Parameter(description = "Cashflow identifier") @PathVariable String cashflowId,
            @RequestBody CashflowStatusUpdate statusUpdate) {
        
        log.info("Updating cashflow status: {} to {}", cashflowId, statusUpdate.getStatus());
        
        // TODO: Implement cashflow status update
        // For now, return a mock response
        Cashflow cashflow = Cashflow.builder()
            .cashflowId(cashflowId)
            .tradeId("TRD-001")
            .positionId("POS-001")
            .cashflowType(Cashflow.CashflowType.ACCRUAL)
            .payerPartyId("PARTY-001")
            .receiverPartyId("PARTY-002")
            .currency("USD")
            .amount(new java.math.BigDecimal("1000.00"))
            .settlementDate(java.time.LocalDate.now().plusDays(2))
            .paymentType(Cashflow.PaymentType.PRINCIPAL)
            .cashflowStatus(Cashflow.CashflowStatus.valueOf(statusUpdate.getStatus()))
            .statusTransitionTimestamp(java.time.LocalDateTime.now())
            .originEventId("EVT-001")
            .revisionNumber(1)
            .build();
        
        return ResponseEntity.ok(cashflow);
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
        
        HealthResponse response = HealthResponse.builder()
            .status("UP")
            .components(Map.of(
                "database", Map.of("status", "UP", "details", Map.of()),
                "messaging", Map.of("status", "UP", "details", Map.of()),
                "referenceData", Map.of("status", "UP", "details", Map.of())
            ))
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
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
        
        MetricsResponse response = MetricsResponse.builder()
            .totalTradesProcessed(0)
            .totalPositionsProcessed(0)
            .totalCashflowsGenerated(0)
            .totalSettlementInstructions(0)
            .averageProcessingTimeMs(0.0)
            .throughputPerMinute(0.0)
            .errorRate(0.0)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }

    // Response DTOs
    @lombok.Data
    @lombok.Builder
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
    @lombok.Builder
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
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthResponse {
        private String status;
        private Map<String, Object> components;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
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
}
