package com.synthetics.core.cashflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

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

/**
 * Comprehensive Load, Performance, and Stress Tests for Cashflow Management Service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class LoadTestRunner {

    private static final String BASE_URL = "http://localhost:8083/api/v1";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        // Reset counters
        requestCounter.set(0);
        totalResponseTime.set(0);
        successCount.set(0);
        errorCount.set(0);
    }

    @Test
    @DisplayName("Load Test: 100 Concurrent Simple Lifecycle Events")
    void loadTest_100ConcurrentLifecycleEvents() throws Exception {
        log.info("Starting Load Test: 100 Concurrent Simple Lifecycle Events");
        
        int numberOfRequests = 100;
        int concurrency = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    sendSimpleLifecycleEvent("LOAD-TEST-" + requestId, "TRD-LOAD-" + requestId);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        
        // Print results
        printTestResults("Load Test (100 Concurrent Events)", numberOfRequests, 
                        endTime - startTime, completed);
    }

    @Test
    @DisplayName("Performance Test: Parallel Processing Throughput")
    void performanceTest_ParallelProcessingThroughput() throws Exception {
        log.info("Starting Performance Test: Parallel Processing Throughput");
        
        int batchSize = 50;
        int numberOfBatches = 5;
        
        long startTime = System.currentTimeMillis();
        
        for (int batch = 0; batch < numberOfBatches; batch++) {
            List<Map<String, Object>> events = new ArrayList<>();
            
            for (int i = 0; i < batchSize; i++) {
                Map<String, Object> event = new HashMap<>();
                event.put("eventId", "PERF-BATCH-" + batch + "-" + i);
                event.put("tradeId", "TRD-PERF-" + batch + "-" + i);
                event.put("eventType", "NEW_TRADE");
                events.add(event);
            }
            
            sendParallelLifecycleEvents(events);
        }
        
        long endTime = System.currentTimeMillis();
        
        // Print results
        printTestResults("Performance Test (Parallel Processing)", 
                        batchSize * numberOfBatches, endTime - startTime, true);
    }

    @Test
    @DisplayName("Stress Test: High Volume Sustained Load")
    void stressTest_HighVolumeSustainedLoad() throws Exception {
        log.info("Starting Stress Test: High Volume Sustained Load");
        
        int requestsPerSecond = 20;
        int durationSeconds = 30;
        int totalRequests = requestsPerSecond * durationSeconds;
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(requestsPerSecond);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Schedule requests at regular intervals
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            long delay = (i / requestsPerSecond) * 1000; // Spread over time
            
            scheduler.schedule(() -> {
                try {
                    sendSimpleLifecycleEvent("STRESS-TEST-" + requestId, "TRD-STRESS-" + requestId);
                } finally {
                    latch.countDown();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        
        boolean completed = latch.await(durationSeconds + 30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        scheduler.shutdown();
        
        // Print results
        printTestResults("Stress Test (Sustained Load)", totalRequests, 
                        endTime - startTime, completed);
    }

    @Test
    @DisplayName("Memory Test: Large Payload Processing")
    void memoryTest_LargePayloadProcessing() throws Exception {
        log.info("Starting Memory Test: Large Payload Processing");
        
        int numberOfLargeEvents = 10;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfLargeEvents; i++) {
            // Create event with many lots
            List<Map<String, Object>> lots = new ArrayList<>();
            for (int j = 0; j < 1000; j++) { // 1000 lots per event
                Map<String, Object> lot = new HashMap<>();
                lot.put("lotId", "LOT-" + i + "-" + j);
                lot.put("positionId", "POS-" + i);
                lot.put("quantity", 1000 + j);
                lot.put("unitPrice", 150.50 + j * 0.01);
                lot.put("currency", "USD");
                lots.add(lot);
            }
            
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "MEMORY-TEST-" + i);
            event.put("tradeId", "TRD-MEMORY-" + i);
            event.put("eventType", "NEW_TRADE");
            event.put("lots", lots);
            
            sendSimpleLifecycleEventWithLots(event);
        }
        
        long endTime = System.currentTimeMillis();
        
        // Print results
        printTestResults("Memory Test (Large Payloads)", numberOfLargeEvents, 
                        endTime - startTime, true);
    }

    @Test
    @DisplayName("Database Performance Test: Query and Filter Operations")
    void databasePerformanceTest_QueryAndFilterOperations() throws Exception {
        log.info("Starting Database Performance Test: Query and Filter Operations");
        
        int numberOfQueries = 200;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numberOfQueries);
        
        long startTime = System.currentTimeMillis();
        
        // Mix of different query types
        for (int i = 0; i < numberOfQueries; i++) {
            final int queryId = i;
            executor.submit(() -> {
                try {
                    switch (queryId % 5) {
                        case 0 -> queryCashflowsWithPagination(queryId % 10, 20);
                        case 1 -> queryCashflowsByType("INTEREST");
                        case 2 -> queryCashflowsByStatus("ACCRUAL");
                        case 3 -> queryCashflowsByCurrency("USD");
                        case 4 -> queryAllCashflows();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        
        // Print results
        printTestResults("Database Performance Test", numberOfQueries, 
                        endTime - startTime, completed);
    }

    @Test
    @DisplayName("Endurance Test: Extended Operation Period")
    void enduranceTest_ExtendedOperation() throws Exception {
        log.info("Starting Endurance Test: Extended Operation Period");
        
        int durationMinutes = 5;
        int requestsPerMinute = 60;
        int totalRequests = durationMinutes * requestsPerMinute;
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Schedule requests over extended period
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            long delay = (i * 60000L) / requestsPerMinute; // Spread over minutes
            
            scheduler.schedule(() -> {
                try {
                    if (requestId % 10 == 0) {
                        // Every 10th request is a parallel batch
                        List<Map<String, Object>> events = createBatchEvents(3, "ENDURANCE-BATCH-" + requestId);
                        sendParallelLifecycleEvents(events);
                    } else {
                        sendSimpleLifecycleEvent("ENDURANCE-" + requestId, "TRD-ENDURANCE-" + requestId);
                    }
                } finally {
                    latch.countDown();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        
        boolean completed = latch.await(durationMinutes + 2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        
        scheduler.shutdown();
        
        // Print results
        printTestResults("Endurance Test", totalRequests, 
                        endTime - startTime, completed);
    }

    // Helper methods for sending requests
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
                log.warn("Request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
                log.warn("Large payload request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
                log.warn("Parallel request failed: {} - {}", response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
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
            
            long requestStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long requestEnd = System.currentTimeMillis();
            
            requestCounter.incrementAndGet();
            totalResponseTime.addAndGet(requestEnd - requestStart);
            
            if (response.statusCode() == 200) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("Error querying all cashflows", e);
        }
    }

    private List<Map<String, Object>> createBatchEvents(int size, String prefix) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", prefix + "-" + i);
            event.put("tradeId", "TRD-" + prefix + "-" + i);
            event.put("eventType", "NEW_TRADE");
            events.add(event);
        }
        
        return events;
    }

    private void printTestResults(String testName, int totalRequests, long durationMs, boolean completed) {
        double durationSeconds = durationMs / 1000.0;
        double requestsPerSecond = totalRequests / durationSeconds;
        double averageResponseTime = totalResponseTime.get() / (double) requestCounter.get();
        double successRate = (successCount.get() / (double) requestCounter.get()) * 100;
        
        log.info("=== {} RESULTS ===", testName);
        log.info("Total Requests: {}", totalRequests);
        log.info("Completed: {}", completed ? "YES" : "NO");
        log.info("Success Count: {}", successCount.get());
        log.info("Error Count: {}", errorCount.get());
        log.info("Success Rate: {:.2f}%", successRate);
        log.info("Duration: {:.2f} seconds", durationSeconds);
        log.info("Throughput: {:.2f} requests/second", requestsPerSecond);
        log.info("Average Response Time: {:.2f} ms", averageResponseTime);
        log.info("=== END RESULTS ===");
        
        // Assert success criteria
        assert completed : "Test did not complete within timeout";
        assert successRate > 95.0 : "Success rate below 95%: " + successRate + "%";
        assert averageResponseTime < 5000 : "Average response time too high: " + averageResponseTime + "ms";
    }
}
