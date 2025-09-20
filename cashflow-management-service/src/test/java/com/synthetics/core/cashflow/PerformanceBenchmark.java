package com.synthetics.core.cashflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * Performance Benchmark Tests for Cashflow Management Service
 * Tests various scenarios to measure performance characteristics
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class PerformanceBenchmark {

    private static final String BASE_URL = "http://localhost:8083/api/v1";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Benchmark: Sequential vs Parallel Processing")
    void benchmark_SequentialVsParallelProcessing() throws Exception {
        log.info("=== BENCHMARK: Sequential vs Parallel Processing ===");

        int numberOfEvents = 20;
        
        // Test Sequential Processing
        long sequentialStart = System.currentTimeMillis();
        for (int i = 0; i < numberOfEvents; i++) {
            sendSimpleLifecycleEvent("SEQ-" + i, "TRD-SEQ-" + i);
        }
        long sequentialEnd = System.currentTimeMillis();
        long sequentialDuration = sequentialEnd - sequentialStart;

        // Test Parallel Processing
        List<Map<String, Object>> parallelEvents = new ArrayList<>();
        for (int i = 0; i < numberOfEvents; i++) {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "PAR-" + i);
            event.put("tradeId", "TRD-PAR-" + i);
            event.put("eventType", "NEW_TRADE");
            parallelEvents.add(event);
        }

        long parallelStart = System.currentTimeMillis();
        sendParallelLifecycleEvents(parallelEvents);
        long parallelEnd = System.currentTimeMillis();
        long parallelDuration = parallelEnd - parallelStart;

        // Results
        log.info("Sequential Processing: {} events in {} ms ({} ms/event)", 
                numberOfEvents, sequentialDuration, sequentialDuration / numberOfEvents);
        log.info("Parallel Processing: {} events in {} ms ({} ms/batch)", 
                numberOfEvents, parallelDuration, parallelDuration);
        log.info("Performance Improvement: {}x faster", 
                (double) sequentialDuration / parallelDuration);
    }

    @Test
    @DisplayName("Benchmark: Database Query Performance")
    void benchmark_DatabaseQueryPerformance() throws Exception {
        log.info("=== BENCHMARK: Database Query Performance ===");

        // First generate some test data
        generateTestData(100);

        Map<String, Long> queryTimes = new HashMap<>();

        // Test different query types
        queryTimes.put("All Cashflows", measureQueryTime(() -> queryAllCashflows()));
        queryTimes.put("Filter by Type", measureQueryTime(() -> queryCashflowsByType("INTEREST")));
        queryTimes.put("Filter by Status", measureQueryTime(() -> queryCashflowsByStatus("ACCRUAL")));
        queryTimes.put("Filter by Currency", measureQueryTime(() -> queryCashflowsByCurrency("USD")));
        queryTimes.put("Pagination (Page 0)", measureQueryTime(() -> queryCashflowsWithPagination(0, 20)));
        queryTimes.put("Pagination (Page 5)", measureQueryTime(() -> queryCashflowsWithPagination(5, 20)));

        // Print results
        log.info("=== DATABASE QUERY PERFORMANCE RESULTS ===");
        queryTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> log.info("{}: {} ms", entry.getKey(), entry.getValue()));
    }

    @Test
    @DisplayName("Benchmark: Throughput Under Load")
    void benchmark_ThroughputUnderLoad() throws Exception {
        log.info("=== BENCHMARK: Throughput Under Load ===");

        int[] concurrencyLevels = {1, 5, 10, 20, 50};
        int requestsPerLevel = 100;

        for (int concurrency : concurrencyLevels) {
            log.info("Testing concurrency level: {}", concurrency);

            ExecutorService executor = Executors.newFixedThreadPool(concurrency);
            CountDownLatch latch = new CountDownLatch(requestsPerLevel);
            AtomicLong levelResponseTime = new AtomicLong(0);
            AtomicInteger levelSuccessCount = new AtomicInteger(0);

            long levelStart = System.currentTimeMillis();

            for (int i = 0; i < requestsPerLevel; i++) {
                final int requestId = i;
                executor.submit(() -> {
                    try {
                        long requestStart = System.currentTimeMillis();
                        sendSimpleLifecycleEvent("THROUGHPUT-" + concurrency + "-" + requestId, 
                                               "TRD-THROUGHPUT-" + concurrency + "-" + requestId);
                        long requestEnd = System.currentTimeMillis();
                        
                        levelResponseTime.addAndGet(requestEnd - requestStart);
                        levelSuccessCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(120, TimeUnit.SECONDS);
            long levelEnd = System.currentTimeMillis();

            executor.shutdown();

            if (completed) {
                double levelDuration = (levelEnd - levelStart) / 1000.0;
                double throughput = requestsPerLevel / levelDuration;
                double avgResponseTime = levelResponseTime.get() / (double) levelSuccessCount.get();

                log.info("Concurrency {}: {:.2f} req/sec, {:.2f} ms avg response time", 
                        concurrency, throughput, avgResponseTime);
            } else {
                log.warn("Concurrency {} test did not complete within timeout", concurrency);
            }

            // Brief pause between tests
            Thread.sleep(2000);
        }
    }

    @Test
    @DisplayName("Benchmark: Memory and Resource Usage")
    void benchmark_MemoryAndResourceUsage() throws Exception {
        log.info("=== BENCHMARK: Memory and Resource Usage ===");

        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Generate large volume of cashflows
        int numberOfLargeEvents = 10;
        int lotsPerEvent = 500;

        for (int i = 0; i < numberOfLargeEvents; i++) {
            List<Map<String, Object>> lots = new ArrayList<>();
            
            for (int j = 0; j < lotsPerEvent; j++) {
                Map<String, Object> lot = new HashMap<>();
                lot.put("lotId", "MEMORY-LOT-" + i + "-" + j);
                lot.put("positionId", "MEMORY-POS-" + i);
                lot.put("quantity", 1000 + j);
                lot.put("unitPrice", 150.50 + j * 0.01);
                lot.put("currency", "USD");
                lots.add(lot);
            }

            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "MEMORY-EVENT-" + i);
            event.put("tradeId", "TRD-MEMORY-" + i);
            event.put("eventType", "NEW_TRADE");
            event.put("lots", lots);

            sendSimpleLifecycleEventWithLots(event);

            // Check memory usage periodically
            if (i % 3 == 0) {
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                log.info("Memory usage after {} events: {} MB (delta: {} MB)", 
                        i + 1, currentMemory / 1024 / 1024, 
                        (currentMemory - initialMemory) / 1024 / 1024);
            }
        }

        // Final memory check
        System.gc(); // Suggest garbage collection
        Thread.sleep(1000);
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        log.info("=== MEMORY USAGE RESULTS ===");
        log.info("Initial Memory: {} MB", initialMemory / 1024 / 1024);
        log.info("Final Memory: {} MB", finalMemory / 1024 / 1024);
        log.info("Memory Increase: {} MB", (finalMemory - initialMemory) / 1024 / 1024);
        log.info("Events Processed: {} (with {} lots each)", numberOfLargeEvents, lotsPerEvent);
    }

    @Test
    @DisplayName("Benchmark: Latency Distribution")
    void benchmark_LatencyDistribution() throws Exception {
        log.info("=== BENCHMARK: Latency Distribution ===");

        int numberOfRequests = 200;
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            long start = System.currentTimeMillis();
            sendSimpleLifecycleEvent("LATENCY-" + i, "TRD-LATENCY-" + i);
            long end = System.currentTimeMillis();
            
            responseTimes.add(end - start);
        }

        // Calculate percentiles
        Collections.sort(responseTimes);
        
        double p50 = getPercentile(responseTimes, 50);
        double p90 = getPercentile(responseTimes, 90);
        double p95 = getPercentile(responseTimes, 95);
        double p99 = getPercentile(responseTimes, 99);
        
        double min = responseTimes.get(0);
        double max = responseTimes.get(responseTimes.size() - 1);
        double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        log.info("=== LATENCY DISTRIBUTION RESULTS ===");
        log.info("Requests: {}", numberOfRequests);
        log.info("Min: {:.2f} ms", min);
        log.info("P50: {:.2f} ms", p50);
        log.info("P90: {:.2f} ms", p90);
        log.info("P95: {:.2f} ms", p95);
        log.info("P99: {:.2f} ms", p99);
        log.info("Max: {:.2f} ms", max);
        log.info("Average: {:.2f} ms", avg);
    }

    // Helper methods
    private void generateTestData(int numberOfEvents) throws Exception {
        log.info("Generating {} test events for benchmark", numberOfEvents);
        
        for (int i = 0; i < numberOfEvents; i++) {
            sendSimpleLifecycleEvent("BENCH-DATA-" + i, "TRD-BENCH-DATA-" + i);
        }
        
        log.info("Test data generation completed");
    }

    private long measureQueryTime(Runnable queryOperation) {
        long start = System.currentTimeMillis();
        queryOperation.run();
        return System.currentTimeMillis() - start;
    }

    private double getPercentile(List<Long> sortedList, int percentile) {
        int index = (int) Math.ceil((percentile / 100.0) * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, Math.min(index, sortedList.size() - 1)));
    }

    // Request sending methods (reused from LoadTestRunner)
    private void sendSimpleLifecycleEvent(String eventId, String tradeId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", eventId);
            event.put("tradeId", tradeId);
            event.put("eventType", "NEW_TRADE");
            
            String json = objectMapper.writeValueAsString(event);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/lifecycle-events/simple"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            log.error("Error sending request for event: {}", eventId, e);
        }
    }

    private void sendSimpleLifecycleEventWithLots(Map<String, Object> eventData) {
        try {
            String json = objectMapper.writeValueAsString(eventData);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/lifecycle-events/simple"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Large payload request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            log.error("Error sending large payload request", e);
        }
    }

    private void sendParallelLifecycleEvents(List<Map<String, Object>> events) {
        try {
            String json = objectMapper.writeValueAsString(events);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/lifecycle-events/parallel"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Parallel request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            log.error("Error sending parallel request", e);
        }
    }

    private void queryCashflowsWithPagination(int page, int size) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cashflows?page=" + page + "&size=" + size))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Query failed: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Error querying cashflows with pagination", e);
        }
    }

    private void queryCashflowsByType(String type) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cashflows?cashflowType=" + type))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Query by type failed: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Error querying cashflows by type", e);
        }
    }

    private void queryCashflowsByStatus(String status) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cashflows?status=" + status))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Query by status failed: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Error querying cashflows by status", e);
        }
    }

    private void queryCashflowsByCurrency(String currency) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cashflows?currency=" + currency))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Query by currency failed: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Error querying cashflows by currency", e);
        }
    }

    private void queryAllCashflows() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cashflows"))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                log.warn("Query all failed: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Error querying all cashflows", e);
        }
    }
}
