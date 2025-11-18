package ireader.core.system

import ireader.core.log.IReaderLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * System health monitoring for tracking app performance and stability
 */
@OptIn(ExperimentalTime::class)
class SystemHealthMonitor {
    
    private val _healthMetrics = MutableStateFlow(HealthMetrics())
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()
    
    /**
     * Record a performance metric
     */
    fun recordPerformanceMetric(
        operationName: String,
        durationMs: Long,
        category: String = "General"
    ) {
        IReaderLog.debug(
            "Performance: $operationName took ${durationMs}ms",
            tag = "HealthMonitor"
        )
        
        _healthMetrics.value = _healthMetrics.value.copy(
            lastOperationName = operationName,
            lastOperationDurationMs = durationMs,
            timestamp = kotlin.time.Clock.System.now()
        )
    }
    
    /**
     * Record memory usage
     */
    fun recordMemoryUsage() {
        val memoryInfo = MemoryInfo.current()
        
        _healthMetrics.value = _healthMetrics.value.copy(
            currentMemoryUsageMB = memoryInfo.usedMemoryMB,
            totalMemoryMB = memoryInfo.totalMemoryMB,
            lastMemoryCheck = kotlin.time.Clock.System.now()
        )
        
        // Log warning if memory usage is high
        val usagePercent = (memoryInfo.usedMemoryMB.toFloat() / memoryInfo.totalMemoryMB.toFloat()) * 100
        if (usagePercent > 80) {
            IReaderLog.warn(
                "High memory usage: ${usagePercent.toInt()}% (${memoryInfo.usedMemoryMB}MB / ${memoryInfo.totalMemoryMB}MB)",
                tag = "HealthMonitor"
            )
        }
    }
    
    /**
     * Record a crash
     */
    fun recordCrash(exception: Throwable) {
        IReaderLog.error("Application crash recorded", exception, tag = "HealthMonitor")
        
        _healthMetrics.value = _healthMetrics.value.copy(
            crashCount = _healthMetrics.value.crashCount + 1,
            lastCrashTime = kotlin.time.Clock.System.now()
        )
    }
    
    /**
     * Get current health metrics
     */
    fun getHealthMetrics(): HealthMetrics {
        updateMetrics()
        return _healthMetrics.value
    }
    
    /**
     * Reset health metrics
     */
    fun reset() {
        _healthMetrics.value = HealthMetrics()
        IReaderLog.info("Health metrics reset", tag = "HealthMonitor")
    }
    
    /**
     * Check if system is healthy
     */
    fun isHealthy(): Boolean {
        val metrics = _healthMetrics.value
        val memoryInfo = MemoryInfo.current()
        val memoryUsagePercent = (memoryInfo.usedMemoryMB.toFloat() / memoryInfo.totalMemoryMB.toFloat()) * 100
        
        return metrics.crashCount == 0 && memoryUsagePercent < 90
    }
    
    /**
     * Get health status
     */
    fun getHealthStatus(): HealthStatus {
        val metrics = _healthMetrics.value
        val memoryInfo = MemoryInfo.current()
        val memoryUsagePercent = (memoryInfo.usedMemoryMB.toFloat() / memoryInfo.totalMemoryMB.toFloat()) * 100
        
        return when {
            metrics.crashCount > 5 || memoryUsagePercent > 95 -> HealthStatus.CRITICAL
            metrics.crashCount > 0 || memoryUsagePercent > 80 -> HealthStatus.WARNING
            else -> HealthStatus.HEALTHY
        }
    }
    
    /**
     * Get health report
     */
    fun getHealthReport(): String {
        val metrics = _healthMetrics.value
        val memoryInfo = MemoryInfo.current()
        val status = getHealthStatus()
        
        return buildString {
            appendLine("=== System Health Report ===")
            appendLine("Status: $status")
            appendLine("Uptime: ${metrics.uptimeMs}ms")
            appendLine("Crash Count: ${metrics.crashCount}")
            appendLine("Memory: ${memoryInfo.usedMemoryMB}MB / ${memoryInfo.totalMemoryMB}MB")
            appendLine("Last Operation: ${metrics.lastOperationName} (${metrics.lastOperationDurationMs}ms)")
            appendLine("Last Check: ${metrics.timestamp}")
        }
    }
    
    /**
     * Log health status
     */
    fun logHealthStatus() {
        val report = getHealthReport()
        IReaderLog.info(report, tag = "HealthMonitor")
    }
    
    /**
     * Update metrics with current uptime
     */
    private fun updateMetrics() {
        val currentTime = kotlin.time.Clock.System.now()
        val startTime = _healthMetrics.value.startTime
        val uptimeMs = (currentTime.toEpochMilliseconds() - startTime.toEpochMilliseconds())
        
        _healthMetrics.value = _healthMetrics.value.copy(
            uptimeMs = uptimeMs,
            timestamp = currentTime
        )
    }
    
    /**
     * Start monitoring
     */
    fun startMonitoring() {
        IReaderLog.info("Health monitoring started", tag = "HealthMonitor")
        _healthMetrics.value = HealthMetrics(startTime = kotlin.time.Clock.System.now())
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        updateMetrics()
        IReaderLog.info("Health monitoring stopped. Final uptime: ${_healthMetrics.value.uptimeMs}ms", tag = "HealthMonitor")
    }
    
    /**
     * Measure and record operation
     */
    inline fun <T> measureAndRecord(
        operationName: String,
        category: String = "General",
        block: () -> T
    ): T {
        val startTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val result = block()
        val endTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val duration = endTime - startTime
        
        recordPerformanceMetric(operationName, duration, category)
        return result
    }
}

/**
 * Health metrics data class
 */
data class HealthMetrics @OptIn(ExperimentalTime::class) constructor(
    val startTime: Instant = kotlin.time.Clock.System.now(),
    val uptimeMs: Long = 0,
    val crashCount: Int = 0,
    val currentMemoryUsageMB: Long = 0,
    val totalMemoryMB: Long = 0,
    val lastMemoryCheck: kotlin.time.Instant? = null,
    val lastOperationName: String? = null,
    val lastOperationDurationMs: Long = 0,
    val lastCrashTime: kotlin.time.Instant? = null,
    val timestamp: kotlin.time.Instant = kotlin.time.Clock.System.now()
)
