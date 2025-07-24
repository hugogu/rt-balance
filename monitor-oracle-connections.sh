#!/bin/bash

# Oracle Connection Monitoring Script
# Continuously monitors Oracle master-slave connections and provides real-time status

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
MONITOR_INTERVAL=10  # 10 seconds
LOG_FILE="oracle-connection-monitor.log"

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${color}[${timestamp}] ${message}${NC}"
    echo "[${timestamp}] ${message}" >> "$LOG_FILE"
}

# Function to get detailed service status
get_service_status() {
    local service_name=$1
    local service_url=$2
    
    local response=$(curl -s -w "%{http_code}" -o /tmp/health_response.json "${service_url}${HEALTH_CHECK_ENDPOINT}" 2>/dev/null || echo "000")
    
    if [ "$response" = "200" ]; then
        local db_status=$(jq -r '.components.db.status // .status' /tmp/health_response.json 2>/dev/null || echo "UNKNOWN")
        local db_host=$(jq -r '.components.db.details.currentHost // "unknown"' /tmp/health_response.json 2>/dev/null || echo "unknown")
        local instance_name=$(jq -r '.components.db.details.instanceName // "unknown"' /tmp/health_response.json 2>/dev/null || echo "unknown")
        local connection_time=$(jq -r '.components.db.details.connectionTime // "unknown"' /tmp/health_response.json 2>/dev/null || echo "unknown")
        
        if [ "$db_status" = "UP" ]; then
            print_status $GREEN "$service_name: HEALTHY"
            print_status $BLUE "  └─ Database Host: $db_host"
            print_status $BLUE "  └─ Instance: $instance_name"
            print_status $BLUE "  └─ Connection Time: ${connection_time}ms"
            return 0
        else
            print_status $RED "$service_name: UNHEALTHY"
            print_status $RED "  └─ Database Status: $db_status"
            return 1
        fi
    else
        print_status $RED "$service_name: SERVICE DOWN"
        print_status $RED "  └─ HTTP Response: $response"
        return 1
    fi
}

# Function to check Oracle container status
check_oracle_containers() {
    print_status $BLUE "Oracle Container Status:"
    
    local master_status=$(docker-compose ps oracle-master | grep -v "Name" | awk '{print $4}' || echo "Down")
    local slave_status=$(docker-compose ps oracle-slave | grep -v "Name" | awk '{print $4}' || echo "Down")
    
    if [[ "$master_status" == *"Up"* ]]; then
        print_status $GREEN "  └─ Oracle Master: UP"
    else
        print_status $RED "  └─ Oracle Master: DOWN"
    fi
    
    if [[ "$slave_status" == *"Up"* ]]; then
        print_status $GREEN "  └─ Oracle Slave: UP"
    else
        print_status $RED "  └─ Oracle Slave: DOWN"
    fi
}

# Function to display connection statistics
display_connection_stats() {
    local total_checks=$1
    local successful_checks=$2
    local failed_checks=$3
    
    local success_rate=0
    if [ $total_checks -gt 0 ]; then
        success_rate=$((successful_checks * 100 / total_checks))
    fi
    
    print_status $BLUE "Connection Statistics:"
    print_status $BLUE "  └─ Total Checks: $total_checks"
    print_status $BLUE "  └─ Successful: $successful_checks"
    print_status $BLUE "  └─ Failed: $failed_checks"
    print_status $BLUE "  └─ Success Rate: ${success_rate}%"
}

# Function to monitor connections
monitor_connections() {
    local total_checks=0
    local successful_checks=0
    local failed_checks=0
    
    print_status $BLUE "Starting Oracle Connection Monitor..."
    print_status $BLUE "Press Ctrl+C to stop monitoring"
    print_status $BLUE "Log file: $LOG_FILE"
    echo "=========================================="
    
    while true; do
        clear
        echo -e "${BLUE}Oracle Master-Slave Connection Monitor${NC}"
        echo "=========================================="
        echo
        
        # Check Oracle containers
        check_oracle_containers
        echo
        
        # Check services
        local account_healthy=false
        local transaction_healthy=false
        
        if get_service_status "Account Service" $ACCOUNT_SERVICE_URL; then
            account_healthy=true
        fi
        
        echo
        
        if get_service_status "Transaction Service" $TRANSACTION_SERVICE_URL; then
            transaction_healthy=true
        fi
        
        echo
        
        # Update statistics
        ((total_checks++))
        if [ "$account_healthy" = true ] && [ "$transaction_healthy" = true ]; then
            ((successful_checks++))
        else
            ((failed_checks++))
        fi
        
        # Display statistics
        display_connection_stats $total_checks $successful_checks $failed_checks
        
        echo
        echo "Next check in ${MONITOR_INTERVAL} seconds..."
        sleep $MONITOR_INTERVAL
    done
}

# Function to run quick health check
quick_health_check() {
    print_status $BLUE "Running Quick Health Check..."
    echo "=========================================="
    
    check_oracle_containers
    echo
    
    get_service_status "Account Service" $ACCOUNT_SERVICE_URL
    echo
    
    get_service_status "Transaction Service" $TRANSACTION_SERVICE_URL
    echo
    
    print_status $BLUE "Quick health check completed"
}

# Cleanup function
cleanup() {
    print_status $YELLOW "Stopping monitor..."
    rm -f /tmp/health_response.json
    exit 0
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

# Main execution
case "${1:-monitor}" in
    "monitor")
        monitor_connections
        ;;
    "check")
        quick_health_check
        ;;
    "help")
        echo "Usage: $0 [monitor|check|help]"
        echo "  monitor  - Start continuous monitoring (default)"
        echo "  check    - Run a quick health check"
        echo "  help     - Show this help message"
        ;;
    *)
        echo "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
