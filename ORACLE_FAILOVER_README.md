# Oracle Master-Slave Auto-Reconnect Solution

This document describes the Oracle master-slave automatic reconnection implementation that ensures service continuity during database failover scenarios.

## Overview

The solution implements Oracle Transparent Application Failover (TAF) with enhanced connection pooling to automatically reconnect to the available Oracle instance when the primary database becomes unavailable.

## Key Features

- **Automatic Failover**: Uses Oracle TAF configuration for seamless failover
- **Connection Pooling**: Enhanced HikariCP configuration with validation and retry logic
- **Health Monitoring**: Real-time database connection health checks
- **Load Balancing**: TNS configuration supports multiple Oracle instances
- **Comprehensive Testing**: Automated failover testing scripts

## Architecture Changes

### 1. TNS Configuration Updates

**Files Modified:**
- `config/oracle-master/tnsnames.ora`
- `config/oracle-slave/tnsnames.ora`

**Key Changes:**
- Added multiple ADDRESS entries for both master and slave
- Implemented FAILOVER_MODE with SELECT type and BASIC method
- Configured retry attempts (180) and delay (5 seconds)

```sql
ORCL =
  (DESCRIPTION =
    (ADDRESS_LIST =
      (ADDRESS = (PROTOCOL = TCP)(HOST = oracle-master)(PORT = 1521))
      (ADDRESS = (PROTOCOL = TCP)(HOST = oracle-slave)(PORT = 1521))
    )
    (CONNECT_DATA =
      (SERVICE_NAME = orcl)
      (FAILOVER_MODE =
        (TYPE = SELECT)
        (METHOD = BASIC)
        (RETRIES = 180)
        (DELAY = 5)
      )
    )
  )
```

### 2. Spring Datasource Configuration

**Files Modified:**
- `app-account-service/src/main/resources/application-oracle.yml`
- `app-transaction-service/src/main/resources/application-oracle.yml`

**Key Enhancements:**
- Enhanced HikariCP connection pool settings
- Connection validation with test queries
- Oracle-specific timeout and retry properties
- TAF-compatible configuration

### 3. Docker Compose Updates

**File Modified:**
- `docker-compose.yml`

**Changes:**
- Updated datasource URLs to use TNS names (`@ORCL`) instead of direct host references
- This enables automatic failover through TNS configuration

### 4. Database Health Check Component

**New File:**
- `lib-common/src/main/kotlin/com/hugogu/balance/common/database/DatabaseHealthCheck.kt`

**Features:**
- Spring Boot Actuator health indicator
- Real-time connection status monitoring
- Current database host detection
- Connection validation and forced reconnection

## Testing Scripts

### 1. Failover Test Script

**File:** `test-oracle-failover.sh`

**Features:**
- Automated master/slave failover simulation
- Service health monitoring during failover
- Load testing during failover scenarios
- Comprehensive reporting with success rates

**Usage:**
```bash
./test-oracle-failover.sh
```

**Test Phases:**
1. Initial connectivity verification
2. Master failover simulation
3. Slave failover simulation
4. Load testing during failover
5. Final connectivity verification

### 2. Connection Monitoring Script

**File:** `monitor-oracle-connections.sh`

**Features:**
- Real-time connection status monitoring
- Oracle container status checking
- Connection statistics tracking
- Continuous monitoring with logging

**Usage:**
```bash
# Continuous monitoring
./monitor-oracle-connections.sh monitor

# Quick health check
./monitor-oracle-connections.sh check

# Help
./monitor-oracle-connections.sh help
```

## Configuration Details

### HikariCP Settings

| Setting | Value | Purpose |
|---------|-------|---------|
| maximum-pool-size | 20 | Maximum connections in pool |
| minimum-idle | 5 | Minimum idle connections |
| connection-timeout | 30000ms | Connection acquisition timeout |
| idle-timeout | 600000ms | Idle connection timeout |
| max-lifetime | 1800000ms | Maximum connection lifetime |
| leak-detection-threshold | 60000ms | Connection leak detection |
| connection-test-query | SELECT 1 FROM DUAL | Connection validation query |
| validation-timeout | 5000ms | Validation query timeout |

### Oracle-Specific Properties

| Property | Value | Purpose |
|----------|-------|---------|
| oracle.net.CONNECT_TIMEOUT | 10000ms | Initial connection timeout |
| oracle.jdbc.ReadTimeout | 30000ms | Read operation timeout |
| oracle.net.READ_TIMEOUT | 30000ms | Network read timeout |
| oracle.jdbc.implicitStatementCacheSize | 20 | Statement cache size |
| oracle.jdbc.defaultRowPrefetch | 20 | Row prefetch size |

### TAF Configuration

| Parameter | Value | Description |
|-----------|-------|-------------|
| TYPE | SELECT | Failover for SELECT statements |
| METHOD | BASIC | Basic failover method |
| RETRIES | 180 | Maximum retry attempts |
| DELAY | 5 | Delay between retries (seconds) |

## Deployment Instructions

### 1. Prerequisites

- Docker and Docker Compose installed
- `jq` and `curl` utilities for testing scripts
- Oracle 11g images available

### 2. Deployment Steps

1. **Start the services:**
   ```bash
   docker-compose up -d
   ```

2. **Verify initial connectivity:**
   ```bash
   ./monitor-oracle-connections.sh check
   ```

3. **Run failover tests:**
   ```bash
   ./test-oracle-failover.sh
   ```

4. **Monitor connections (optional):**
   ```bash
   ./monitor-oracle-connections.sh monitor
   ```

### 3. Health Check Endpoints

- Account Service: `http://localhost:8080/actuator/health`
- Transaction Service: `http://localhost:8082/actuator/health`

## Failover Scenarios

### Scenario 1: Master Database Failure

1. **Detection**: Connection validation fails on master
2. **Failover**: TNS automatically tries slave address
3. **Reconnection**: New connections established to slave
4. **Recovery**: When master returns, new connections can use master

### Scenario 2: Slave Database Failure

1. **Detection**: Slave becomes unavailable
2. **Continuity**: Master continues serving requests
3. **Recovery**: Slave rejoins when available

### Scenario 3: Network Partition

1. **Detection**: Connection timeouts trigger failover
2. **Retry Logic**: Automatic retry with configured delays
3. **Fallback**: Alternative addresses attempted

## Monitoring and Alerting

### Health Check Response Example

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "Oracle",
        "currentHost": "oracle-master",
        "instanceName": "orcl",
        "connectionTime": 45
      }
    }
  }
}
```

### Log Monitoring

Monitor application logs for:
- Connection validation failures
- Failover events
- Reconnection attempts
- Performance degradation

## Troubleshooting

### Common Issues

1. **Connection Pool Exhaustion**
   - Increase `maximum-pool-size`
   - Check for connection leaks
   - Monitor `leak-detection-threshold`

2. **Slow Failover**
   - Reduce `connection-timeout`
   - Adjust TAF `DELAY` setting
   - Check network latency

3. **Failed Health Checks**
   - Verify Oracle container status
   - Check TNS configuration
   - Validate network connectivity

### Debug Commands

```bash
# Check Oracle container logs
docker-compose logs oracle-master
docker-compose logs oracle-slave

# Test TNS connectivity
docker-compose exec oracle-master tnsping ORCL

# Check service logs
docker-compose logs account-service-api
docker-compose logs transaction-service-api
```

## Performance Considerations

### Connection Pool Tuning

- Monitor connection usage patterns
- Adjust pool sizes based on load
- Consider connection lifetime settings

### Network Optimization

- Minimize network latency between services and databases
- Use appropriate timeout values
- Consider connection compression

### Database Optimization

- Ensure proper Oracle configuration
- Monitor database performance during failover
- Consider read/write splitting for better performance

## Security Considerations

- Use secure connection strings
- Implement proper authentication
- Monitor for unauthorized access attempts
- Regular security updates for Oracle images

## Maintenance

### Regular Tasks

1. **Monitor connection statistics**
2. **Test failover scenarios monthly**
3. **Update Oracle images regularly**
4. **Review and tune configuration**
5. **Backup TNS configurations**

### Capacity Planning

- Monitor connection pool utilization
- Plan for peak load scenarios
- Consider scaling strategies

## Conclusion

This Oracle master-slave auto-reconnect solution provides robust failover capabilities with minimal service disruption. The combination of TNS failover configuration, enhanced connection pooling, and comprehensive monitoring ensures high availability for your applications.

For questions or issues, refer to the troubleshooting section or check the monitoring scripts for real-time status information.
