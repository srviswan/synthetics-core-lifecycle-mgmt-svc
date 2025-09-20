#!/bin/bash

# Quick Performance Test Script for Cashflow Management Service
# Simple script to quickly test performance without dependencies

set -e

BASE_URL="http://localhost:8083/api/v1"

echo "🚀 QUICK PERFORMANCE TEST FOR CASHFLOW MANAGEMENT SERVICE"
echo "=========================================================="
echo "Service URL: $BASE_URL"
echo "Start Time: $(date)"
echo ""

# Function to check service health
check_service() {
    echo "🏥 Checking service health..."
    if curl -s "$BASE_URL/health" > /dev/null; then
        echo "✅ Service is healthy and responding"
    else
        echo "❌ Service is not responding - please start the service first"
        exit 1
    fi
    echo ""
}

# Function to test single event processing
test_single_event_performance() {
    echo "📊 TEST 1: Single Event Processing Performance"
    echo "----------------------------------------------"
    
    local total_requests=50
    local success_count=0
    local total_time=0
    
    for i in $(seq 1 $total_requests); do
        local start_time=$(date +%s%3N)
        
        local response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/lifecycle-events/simple" \
                             -H "Content-Type: application/json" \
                             -d "{\"eventId\":\"PERF-$i\",\"tradeId\":\"TRD-PERF-$i\",\"eventType\":\"NEW_TRADE\"}")
        
        local end_time=$(date +%s%3N)
        local duration=$((end_time - start_time))
        total_time=$((total_time + duration))
        
        local status_code="${response: -3}"
        if [ "$status_code" = "200" ]; then
            success_count=$((success_count + 1))
        fi
        
        if [ $((i % 10)) -eq 0 ]; then
            echo "   Processed $i/$total_requests events..."
        fi
    done
    
    local avg_time=$((total_time / total_requests))
    local success_rate=$((success_count * 100 / total_requests))
    local throughput=$(echo "scale=2; $total_requests * 1000 / $total_time" | bc)
    
    echo "   Results:"
    echo "   - Total Requests: $total_requests"
    echo "   - Success Rate: $success_rate%"
    echo "   - Average Response Time: ${avg_time}ms"
    echo "   - Throughput: ${throughput} req/sec"
    echo ""
}

# Function to test parallel processing
test_parallel_processing_performance() {
    echo "⚡ TEST 2: Parallel Processing Performance"
    echo "-----------------------------------------"
    
    local batch_sizes=(3 5 10 15 20)
    
    for batch_size in "${batch_sizes[@]}"; do
        echo "   Testing batch size: $batch_size"
        
        # Create batch payload
        local payload="["
        for i in $(seq 1 $batch_size); do
            payload+="{\"eventId\":\"PAR-BATCH-$batch_size-$i\",\"tradeId\":\"TRD-PAR-BATCH-$batch_size-$i\",\"eventType\":\"NEW_TRADE\"}"
            if [ $i -lt $batch_size ]; then
                payload+=","
            fi
        done
        payload+="]"
        
        local start_time=$(date +%s%3N)
        
        local response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/lifecycle-events/parallel" \
                             -H "Content-Type: application/json" \
                             -d "$payload")
        
        local end_time=$(date +%s%3N)
        local duration=$((end_time - start_time))
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            local throughput=$(echo "scale=2; $batch_size * 1000 / $duration" | bc)
            echo "     ✅ ${duration}ms total, ${throughput} events/sec"
        else
            echo "     ❌ Failed with status: $status_code"
        fi
    done
    echo ""
}

# Function to test database query performance
test_database_query_performance() {
    echo "🗄️  TEST 3: Database Query Performance"
    echo "------------------------------------"
    
    # Test different query types
    declare -A queries=(
        ["All Cashflows"]="/cashflows"
        ["Filter by Type"]="/cashflows?cashflowType=INTEREST"
        ["Filter by Status"]="/cashflows?status=ACCRUAL"
        ["Filter by Currency"]="/cashflows?currency=USD"
        ["Pagination"]="/cashflows?page=0&size=10"
        ["Complex Filter"]="/cashflows?cashflowType=PRINCIPAL&currency=USD&page=0&size=5"
    )
    
    for query_name in "${!queries[@]}"; do
        local endpoint="${queries[$query_name]}"
        
        local start_time=$(date +%s%3N)
        local response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint")
        local end_time=$(date +%s%3N)
        
        local duration=$((end_time - start_time))
        local status_code="${response: -3}"
        
        if [ "$status_code" = "200" ]; then
            echo "   $query_name: ${duration}ms ✅"
        else
            echo "   $query_name: Failed ($status_code) ❌"
        fi
    done
    echo ""
}

# Function to test concurrent load
test_concurrent_load() {
    echo "🔄 TEST 4: Concurrent Load Test"
    echo "------------------------------"
    
    local concurrent_requests=20
    local pids=()
    
    echo "   Starting $concurrent_requests concurrent requests..."
    
    local start_time=$(date +%s%3N)
    
    # Start concurrent requests
    for i in $(seq 1 $concurrent_requests); do
        (
            curl -s -X POST "$BASE_URL/lifecycle-events/simple" \
                 -H "Content-Type: application/json" \
                 -d "{\"eventId\":\"CONCURRENT-$i\",\"tradeId\":\"TRD-CONCURRENT-$i\",\"eventType\":\"NEW_TRADE\"}" \
                 > /dev/null
        ) &
        pids+=($!)
    done
    
    # Wait for all to complete
    local success_count=0
    for pid in "${pids[@]}"; do
        if wait $pid; then
            success_count=$((success_count + 1))
        fi
    done
    
    local end_time=$(date +%s%3N)
    local total_duration=$((end_time - start_time))
    local throughput=$(echo "scale=2; $concurrent_requests * 1000 / $total_duration" | bc)
    
    echo "   Results:"
    echo "   - Concurrent Requests: $concurrent_requests"
    echo "   - Successful: $success_count"
    echo "   - Total Duration: ${total_duration}ms"
    echo "   - Throughput: ${throughput} req/sec"
    echo ""
}

# Function to test service under stress
test_stress_conditions() {
    echo "💪 TEST 5: Stress Test (High Load)"
    echo "---------------------------------"
    
    local duration_seconds=30
    local requests_per_second=5
    local total_requests=$((duration_seconds * requests_per_second))
    
    echo "   Running stress test for ${duration_seconds}s at ${requests_per_second} req/sec"
    echo "   Total requests: $total_requests"
    
    local success_count=0
    local start_time=$(date +%s)
    
    for i in $(seq 1 $total_requests); do
        local response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/lifecycle-events/simple" \
                             -H "Content-Type: application/json" \
                             -d "{\"eventId\":\"STRESS-$i\",\"tradeId\":\"TRD-STRESS-$i\",\"eventType\":\"NEW_TRADE\"}")
        
        local status_code="${response: -3}"
        if [ "$status_code" = "200" ]; then
            success_count=$((success_count + 1))
        fi
        
        # Control rate (sleep for remaining time in second)
        if [ $((i % requests_per_second)) -eq 0 ]; then
            local current_time=$(date +%s)
            local elapsed=$((current_time - start_time))
            local expected_time=$((i / requests_per_second))
            
            if [ $elapsed -lt $expected_time ]; then
                sleep $((expected_time - elapsed))
            fi
            
            echo "   Progress: $i/$total_requests requests (${success_count} successful)"
        fi
    done
    
    local end_time=$(date +%s)
    local actual_duration=$((end_time - start_time))
    local success_rate=$((success_count * 100 / total_requests))
    
    echo "   Results:"
    echo "   - Total Requests: $total_requests"
    echo "   - Successful: $success_count"
    echo "   - Success Rate: $success_rate%"
    echo "   - Actual Duration: ${actual_duration}s"
    echo ""
}

# Function to check final service state
check_final_state() {
    echo "📋 FINAL SERVICE STATE"
    echo "---------------------"
    
    # Get total cashflows
    local cashflow_response=$(curl -s "$BASE_URL/cashflows")
    local total_cashflows=$(echo "$cashflow_response" | jq -r '.totalElements' 2>/dev/null || echo "N/A")
    
    # Get service health
    local health_response=$(curl -s "$BASE_URL/health")
    local health_status=$(echo "$health_response" | jq -r '.status' 2>/dev/null || echo "N/A")
    
    # Get metrics
    local metrics_response=$(curl -s "$BASE_URL/metrics")
    local trades_processed=$(echo "$metrics_response" | jq -r '.totalTradesProcessed' 2>/dev/null || echo "N/A")
    
    echo "   Service Status: $health_status"
    echo "   Total Cashflows in DB: $total_cashflows"
    echo "   Trades Processed (metric): $trades_processed"
    echo ""
}

# Main execution
main() {
    echo "Starting Quick Performance Test Suite..."
    echo ""
    
    # Check if bc (calculator) is available
    if ! command -v bc &> /dev/null; then
        echo "⚠️  Warning: 'bc' calculator not found. Some calculations may not work."
        echo "   Install with: brew install bc (macOS) or apt-get install bc (Ubuntu)"
    fi
    
    # Run all tests
    check_service
    test_single_event_performance
    test_parallel_processing_performance
    test_database_query_performance
    test_concurrent_load
    test_stress_conditions
    check_final_state
    
    echo "🎉 PERFORMANCE TEST SUITE COMPLETED!"
    echo "End Time: $(date)"
    echo ""
    echo "💡 For more detailed analysis, consider running:"
    echo "   - JMeter test plan: scripts/jmeter-test-plan.jmx"
    echo "   - Java load tests: mvn test -Dtest=LoadTestRunner"
    echo "   - Full stress test: bash scripts/stress-test.sh"
}

# Run main function
main "$@"
