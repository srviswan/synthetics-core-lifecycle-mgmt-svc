#!/bin/bash

# Comprehensive Stress Test Script for Cashflow Management Service
# This script runs various stress tests against the running service

set -e

BASE_URL="http://localhost:8083/api/v1"
RESULTS_DIR="./test-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create results directory
mkdir -p "$RESULTS_DIR"

echo "=== CASHFLOW MANAGEMENT SERVICE STRESS TEST SUITE ==="
echo "Timestamp: $(date)"
echo "Base URL: $BASE_URL"
echo "Results will be saved to: $RESULTS_DIR"
echo ""

# Function to check if service is running
check_service() {
    echo "Checking service health..."
    if curl -s "$BASE_URL/health" > /dev/null; then
        echo "✅ Service is running and healthy"
    else
        echo "❌ Service is not responding"
        exit 1
    fi
}

# Function to run load test with curl
run_load_test() {
    local test_name="$1"
    local requests="$2"
    local concurrency="$3"
    local endpoint="$4"
    local payload="$5"
    
    echo "🚀 Running $test_name..."
    echo "   Requests: $requests, Concurrency: $concurrency"
    
    # Create temporary file for results
    local result_file="$RESULTS_DIR/${test_name// /_}_${TIMESTAMP}.txt"
    
    if [ -n "$payload" ]; then
        # POST request with payload
        echo "$payload" > /tmp/test_payload.json
        ab -n "$requests" -c "$concurrency" -p /tmp/test_payload.json -T 'application/json' \
           "$BASE_URL$endpoint" > "$result_file" 2>&1
    else
        # GET request
        ab -n "$requests" -c "$concurrency" "$BASE_URL$endpoint" > "$result_file" 2>&1
    fi
    
    # Extract key metrics
    local rps=$(grep "Requests per second" "$result_file" | awk '{print $4}')
    local mean_time=$(grep "Time per request" "$result_file" | head -1 | awk '{print $4}')
    local failed=$(grep "Failed requests" "$result_file" | awk '{print $3}')
    
    echo "   Results: $rps req/sec, ${mean_time}ms avg, $failed failed"
    echo ""
}

# Function to run database stress test
run_database_stress_test() {
    echo "🗄️  Running Database Stress Test..."
    
    # Generate test data first
    echo "   Generating test data..."
    for i in {1..50}; do
        curl -s -X POST "$BASE_URL/lifecycle-events/simple" \
             -H "Content-Type: application/json" \
             -d "{\"eventId\":\"DB-STRESS-$i\",\"tradeId\":\"TRD-DB-STRESS-$i\",\"eventType\":\"NEW_TRADE\"}" \
             > /dev/null &
        
        if [ $((i % 10)) -eq 0 ]; then
            wait  # Wait for batch to complete
            echo "   Generated $i events..."
        fi
    done
    wait
    
    echo "   Running concurrent database queries..."
    
    # Run concurrent queries
    run_load_test "Database Query All" 100 10 "/cashflows" ""
    run_load_test "Database Query by Type" 100 10 "/cashflows?cashflowType=INTEREST" ""
    run_load_test "Database Query with Pagination" 100 10 "/cashflows?page=0&size=20" ""
}

# Function to run parallel processing stress test
run_parallel_stress_test() {
    echo "⚡ Running Parallel Processing Stress Test..."
    
    local payload='[
        {"eventId":"PAR-STRESS-1","tradeId":"TRD-PAR-STRESS-1","eventType":"NEW_TRADE"},
        {"eventId":"PAR-STRESS-2","tradeId":"TRD-PAR-STRESS-2","eventType":"NEW_TRADE"},
        {"eventId":"PAR-STRESS-3","tradeId":"TRD-PAR-STRESS-3","eventType":"NEW_TRADE"},
        {"eventId":"PAR-STRESS-4","tradeId":"TRD-PAR-STRESS-4","eventType":"NEW_TRADE"},
        {"eventId":"PAR-STRESS-5","tradeId":"TRD-PAR-STRESS-5","eventType":"NEW_TRADE"}
    ]'
    
    run_load_test "Parallel Processing" 50 5 "/lifecycle-events/parallel" "$payload"
}

# Function to run memory stress test
run_memory_stress_test() {
    echo "🧠 Running Memory Stress Test..."
    
    echo "   Testing large payload processing..."
    
    # Create large payload with many lots
    local large_payload='{
        "eventId":"MEMORY-STRESS-001",
        "tradeId":"TRD-MEMORY-STRESS-001",
        "eventType":"NEW_TRADE",
        "lots":['
    
    for i in {1..1000}; do
        large_payload+="{\"lotId\":\"LOT-$i\",\"positionId\":\"POS-001\",\"quantity\":1000,\"unitPrice\":150.50,\"currency\":\"USD\"}"
        if [ $i -lt 1000 ]; then
            large_payload+=","
        fi
    done
    
    large_payload+=']}'
    
    echo "   Sending large payload (1000 lots)..."
    local start_time=$(date +%s%3N)
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/lifecycle-events/simple" \
                    -H "Content-Type: application/json" \
                    -d "$large_payload")
    
    local end_time=$(date +%s%3N)
    local duration=$((end_time - start_time))
    
    echo "   Large payload processed in ${duration}ms"
    echo "   Response code: ${response: -3}"
}

# Function to run sustained load test
run_sustained_load_test() {
    echo "⏱️  Running Sustained Load Test..."
    
    local duration_seconds=60
    local requests_per_second=10
    local total_requests=$((duration_seconds * requests_per_second))
    
    echo "   Duration: ${duration_seconds}s, Rate: ${requests_per_second} req/sec"
    
    local payload='{"eventId":"SUSTAINED-%%d","tradeId":"TRD-SUSTAINED-%%d","eventType":"NEW_TRADE"}'
    
    # Use a simple loop with sleep for sustained load
    for i in $(seq 1 $total_requests); do
        local current_payload=$(printf "$payload" $i $i)
        
        curl -s -X POST "$BASE_URL/lifecycle-events/simple" \
             -H "Content-Type: application/json" \
             -d "$current_payload" > /dev/null &
        
        # Control rate
        if [ $((i % requests_per_second)) -eq 0 ]; then
            sleep 1
            echo "   Processed $i/$total_requests requests..."
        fi
    done
    
    wait
    echo "   Sustained load test completed"
}

# Function to monitor system resources
monitor_resources() {
    echo "📊 System Resource Monitoring (during tests)"
    
    # Monitor for 30 seconds
    for i in {1..6}; do
        echo "   Sample $i/6:"
        
        # Memory usage
        local memory_usage=$(ps aux | grep java | grep cashflow | awk '{print $6}' | head -1)
        if [ -n "$memory_usage" ]; then
            echo "     Memory: ${memory_usage}KB"
        fi
        
        # CPU usage
        local cpu_usage=$(ps aux | grep java | grep cashflow | awk '{print $3}' | head -1)
        if [ -n "$cpu_usage" ]; then
            echo "     CPU: ${cpu_usage}%"
        fi
        
        # Database connections
        local db_connections=$(curl -s "$BASE_URL/health" | jq -r '.components.database.status' 2>/dev/null || echo "N/A")
        echo "     Database: $db_connections"
        
        sleep 5
    done
}

# Function to generate summary report
generate_summary_report() {
    echo ""
    echo "📋 STRESS TEST SUMMARY REPORT"
    echo "=============================="
    echo "Test Run: $TIMESTAMP"
    echo "Service URL: $BASE_URL"
    echo ""
    
    # Count result files
    local result_count=$(ls -1 "$RESULTS_DIR"/*_${TIMESTAMP}.txt 2>/dev/null | wc -l)
    echo "Test Results Generated: $result_count files"
    
    # Check current database state
    echo ""
    echo "📊 Final Database State:"
    local cashflow_count=$(curl -s "$BASE_URL/cashflows" | jq -r '.totalElements' 2>/dev/null || echo "N/A")
    echo "   Total Cashflows: $cashflow_count"
    
    # Check service health
    echo ""
    echo "🏥 Service Health After Tests:"
    curl -s "$BASE_URL/health" | jq -r '.status' 2>/dev/null || echo "Service not responding"
    
    echo ""
    echo "📁 Detailed results available in: $RESULTS_DIR"
    echo "✅ Stress test suite completed!"
}

# Main execution
main() {
    echo "Starting Cashflow Management Service Stress Test Suite..."
    echo ""
    
    # Check prerequisites
    if ! command -v ab &> /dev/null; then
        echo "❌ Apache Bench (ab) is required but not installed"
        echo "   Install with: brew install apache2-utils (macOS) or apt-get install apache2-utils (Ubuntu)"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo "❌ jq is required but not installed"
        echo "   Install with: brew install jq (macOS) or apt-get install jq (Ubuntu)"
        exit 1
    fi
    
    # Run tests
    check_service
    
    echo "🎯 PHASE 1: Basic Load Tests"
    run_load_test "Simple Event Load" 200 20 "/lifecycle-events/simple" '{"eventId":"LOAD-%%d","tradeId":"TRD-LOAD-%%d","eventType":"NEW_TRADE"}'
    
    echo "🎯 PHASE 2: Database Stress Tests"
    run_database_stress_test
    
    echo "🎯 PHASE 3: Parallel Processing Tests"
    run_parallel_stress_test
    
    echo "🎯 PHASE 4: Memory Stress Tests"
    run_memory_stress_test
    
    echo "🎯 PHASE 5: Sustained Load Tests"
    run_sustained_load_test
    
    echo "🎯 PHASE 6: Resource Monitoring"
    monitor_resources
    
    # Generate final report
    generate_summary_report
}

# Run main function
main "$@"
