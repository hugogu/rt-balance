package com.hugogu.balance.common.database

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Database health check component that monitors Oracle master-slave connection
 * and provides failover status information
 */
@Component
class DatabaseHealthCheck @Autowired constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val dataSource: DataSource
) : HealthIndicator {

    private val logger = LoggerFactory.getLogger(DatabaseHealthCheck::class.java)

    override fun health(): Health {
        return try {
            val connectionInfo = checkDatabaseConnection()
            Health.up()
                .withDetail("database", "Oracle")
                .withDetail("status", "Connected")
                .withDetail("currentHost", connectionInfo.host)
                .withDetail("instanceName", connectionInfo.instanceName)
                .withDetail("connectionTime", connectionInfo.connectionTime)
                .build()
        } catch (ex: Exception) {
            logger.error("Database health check failed", ex)
            Health.down()
                .withDetail("database", "Oracle")
                .withDetail("status", "Disconnected")
                .withDetail("error", ex.message)
                .build()
        }
    }

    private fun checkDatabaseConnection(): ConnectionInfo {
        val startTime = System.currentTimeMillis()
        
        // Test basic connectivity
        jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Int::class.java)
        
        // Get current connection information
        val hostInfo = jdbcTemplate.queryForObject(
            "SELECT SYS_CONTEXT('USERENV', 'SERVER_HOST') FROM DUAL", 
            String::class.java
        ) ?: "unknown"
        
        val instanceName = jdbcTemplate.queryForObject(
            "SELECT SYS_CONTEXT('USERENV', 'INSTANCE_NAME') FROM DUAL", 
            String::class.java
        ) ?: "unknown"
        
        val connectionTime = System.currentTimeMillis() - startTime
        
        return ConnectionInfo(hostInfo, instanceName, connectionTime)
    }

    /**
     * Force connection validation and reconnection if needed
     */
    fun validateAndReconnect(): Boolean {
        return try {
            dataSource.connection.use { connection ->
                if (!connection.isValid(5)) {
                    logger.warn("Connection validation failed, attempting reconnection...")
                    // Connection pool will handle reconnection automatically
                    false
                } else {
                    logger.debug("Connection validation successful")
                    true
                }
            }
        } catch (ex: SQLException) {
            logger.error("Connection validation failed", ex)
            false
        }
    }

    data class ConnectionInfo(
        val host: String,
        val instanceName: String,
        val connectionTime: Long
    )
}
