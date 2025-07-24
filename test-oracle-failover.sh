#!/bin/bash

# Oracle Master-Slave Failover Test Script
# This script tests automatic reconnection during Oracle failover scenarios

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ACCOUNT_SERVICE_URL="http://localhost:8080"
TRANSACTION_SERVICE_URL="http://localhost:8082"
HEALTH_CHECK_ENDPOINT="/actuator/health"
TEST_DURATION=300  # 5 minutes
CHECK_INTERVAL=5   # 5 seconds

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}[$(date '+%Y-%m-%d %H:%M:%S')] ${message}${NC}"
}

# Function to check service health
check_service_health() {
    local service_name=$1
    local service_url=$2
    
    response=$(curl -s -w "%{http_code}" -o /tmp/health_response.json "${service_url}${HEALTH_CHECK_ENDPOINT}" || echo "000")
    
    if [ "$response" = "200" ]; then
        db_status=$(jq -r '.components.db.status // .status' /tmp/health_response.json 2>/dev/null || echo "UNKNOWN")
        db_host=$(jq -r '.components.db.details.currentHost // "unknown"' /tmp/health_response.json 2>/dev/null || echo "unknown")
        
        if [ "$db_status" = "UP" ]; then
            print_status $GREEN "$service_name: HEALTHY (DB: $db_host)"
            return 0
        else
            print_status $RED "$service_name: UNHEALTHY (DB Status: $db_status)"
            return 1
        fi
    else
        print_status $RED "$service_name: SERVICE DOWN (HTTP: $response)"
        return 1
    fi
}

# Function to test database connectivity
test_database_connectivity() {
    print_status $BLUE "Testing database connectivity..."
    
    # Test account service
    account_healthy=$(check_service_health "Account Service" $ACCOUNT_SERVICE_URL && echo "true" || echo "false")
    
    # Test transaction service
    transaction_healthy=$(check_service_health "Transaction Service" $TRANSACTION_SERVICE_URL && echo "true" || echo "false")
    
    if [ "$account_healthy" = "true" ] && [ "$transaction_healthy" = "true" ]; then
        return 0
    else
        return 1
    fi
}

# Function to simulate master failure
simulate_master_failure() {
    print_status $YELLOW "Simulating Oracle master failure..."
    docker-compose stop oracle-master
    print_status $YELLOW "Oracle master stopped"
}

# Function to simulate master recovery
simulate_master_recovery() {
    print_status $YELLOW "Simulating Oracle master recovery..."
    docker-compose start oracle-master
    print_status $YELLOW "Oracle master started"
    
    # Wait for Oracle to be ready
    print_status $BLUE "Waiting for Oracle master to be ready..."
    sleep 30
}

# Function to simulate slave failure
simulate_slave_failure() {
    print_status $YELLOW "Simulating Oracle slave failure..."
    docker-compose stop oracle-slave
    print_status $YELLOW "Oracle slave stopped"
}

# Function to simulate slave recovery
simulate_slave_recovery() {
    print_status $YELLOW "Simulating Oracle slave recovery..."
    docker-compose start oracle-slave
    print_status $YELLOW "Oracle slave started"
    
    # Wait for Oracle to be ready
    print_status $BLUE "Waiting for Oracle slave to be ready..."
    sleep 30
}

# Function to monitor services during failover
monitor_services() {
    local test_name=$1
    local duration=$2
    
    print_status $BLUE "Monitoring services during $test_name for ${duration}s..."
    
    local start_time=$(date +%s)
    local end_time=$((start_time + duration))
    local failure_count=0
    local success_count=0
    
    while [ $(date +%s) -lt $end_time ]; do
        if test_database_connectivity; then
            ((success_count++))
        else
            ((failure_count++))
        fi
        
        sleep $CHECK_INTERVAL
    done
    
    local total_checks=$((success_count + failure_count))
    local success_rate=$((success_count * 100 / total_checks))
    
    print_status $BLUE "$test_name Results:"
    print_status $BLUE "  Total checks: $total_checks"
    print_status $BLUE "  Successful: $success_count"
    print_status $BLUE "  Failed: $failure_count"
    print_status $BLUE "  Success rate: ${success_rate}%"
    
    if [ $success_rate -ge 80 ]; then
        print_status $GREEN "$test_name: PASSED (Success rate: ${success_rate}%)"
        return 0
    else
        print_status $RED "$test_name: FAILED (Success rate: ${success_rate}%)"
        return 1
    fi
}

# Function to run load test during failover
run_load_test() {
    local test_name=$1
    
    print_status $BLUE "Running load test during $test_name..."
    
    # Simple load test using curl
    for i in {1..10}; do
        curl -s -o /dev/null -w "%{http_code}" "$ACCOUNT_SERVICE_URL/actuator/health" &
        curl -s -o /dev/null -w "%{http_code}" "$TRANSACTION_SERVICE_URL/actuator/health" &
    done
    
    wait
    print_status $BLUE "Load test completed"
}

# Main test execution
main() {
    print_status $BLUE "Starting Oracle Master-Slave Failover Test"
    print_status $BLUE "============================================"
    
    # Check if services are running
    if ! docker-compose ps | grep -q "Up"; then
        print_status $RED "Services are not running. Please start with: docker-compose up -d"
        exit 1
    fi
    
    # Initial connectivity test
    print_status $BLUE "Phase 1: Initial Connectivity Test"
    if ! test_database_connectivity; then
        print_status $RED "Initial connectivity test failed. Please check your setup."
        exit 1
    fi
    print_status $GREEN "Initial connectivity test passed"
    
    # Test 1: Master Failover Test
    print_status $BLUE "Phase 2: Master Failover Test"
    simulate_master_failure
    sleep 10  # Allow time for failover detection
    
    if ! monitor_services "Master Failover" 60; then
        print_status $RED "Master failover test failed"
    fi
    
    simulate_master_recovery
    
    # Test 2: Slave Failover Test
    print_status $BLUE "Phase 3: Slave Failover Test"
    simulate_slave_failure
    sleep 10  # Allow time for failover detection
    
    if ! monitor_services "Slave Failover" 60; then
        print_status $RED "Slave failover test failed"
    fi
    
    simulate_slave_recovery
    
    # Test 3: Load Test During Failover
    print_status $BLUE "Phase 4: Load Test During Failover"
    simulate_master_failure &
    sleep 5
    run_load_test "Master Failover with Load"
    simulate_master_recovery
    
    # Final connectivity test
    print_status $BLUE "Phase 5: Final Connectivity Test"
    sleep 30  # Allow time for full recovery
    if test_database_connectivity; then
        print_status $GREEN "Final connectivity test passed"
        print_status $GREEN "All failover tests completed successfully!"
    else
        print_status $RED "Final connectivity test failed"
        exit 1
    fi
    
    print_status $BLUE "============================================"
    print_status $GREEN "Oracle Master-Slave Failover Test Complete"
}

# Cleanup function
cleanup() {
    print_status $YELLOW "Cleaning up..."
    docker-compose start oracle-master oracle-slave 2>/dev/null || true
    rm -f /tmp/health_response.json
}

# Set up signal handlers
trap cleanup EXIT INT TERM

# Check dependencies
if ! command -v jq &> /dev/null; then
    print_status $RED "jq is required but not installed. Please install jq first."
    exit 1
fi

if ! command -v curl &> /dev/null; then
    print_status $RED "curl is required but not installed. Please install curl first."
    exit 1
fi

# Run main function
main "$@"
