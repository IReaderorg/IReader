package ireader.core.system

import ireader.core.log.IReaderLog
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Diagnostic tools for troubleshooting and system information
 */
@OptIn(ExperimentalTime::class)
object DiagnosticTools {
    
    /**
     * Collect comprehensive system information
     */
    fun collectSystemInfo(): SystemInfo {
        val runtime = RuntimeCompat.getRuntime()
        
        return SystemInfo(
            osName = SystemCompat.getProperty("os.name") ?: "Unknown",
            osVersion = SystemCompat.getProperty("os.version") ?: "Unknown",
            osArch = SystemCompat.getProperty("os.arch") ?: "Unknown",
            javaVersion = SystemCompat.getProperty("java.version") ?: "Unknown",
            javaVendor = SystemCompat.getProperty("java.vendor") ?: "Unknown",
            availableProcessors = runtime.availableProcessors(),
            totalMemoryMB = runtime.totalMemory() / 1024 / 1024,
            freeMemoryMB = runtime.freeMemory() / 1024 / 1024,
            maxMemoryMB = runtime.maxMemory() / 1024 / 1024,
            timestamp = kotlin.time.Clock.System.now()
        )
    }
    
    /**
     * Generate a diagnostic report
     */
    fun generateDiagnosticReport(
        includeSystemInfo: Boolean = true,
        includeMemoryInfo: Boolean = true,
        includePerformanceStats: Boolean = true,
        healthMonitor: SystemHealthMonitor? = null
    ): String {
        return buildString {
            appendLine("=== IReader Diagnostic Report ===")
            appendLine("Generated: ${kotlin.time.Clock.System.now()}")
            appendLine()
            
            if (includeSystemInfo) {
                val systemInfo = collectSystemInfo()
                appendLine("System Information:")
                appendLine("  OS: ${systemInfo.osName} ${systemInfo.osVersion} (${systemInfo.osArch})")
                appendLine("  Java: ${systemInfo.javaVersion} (${systemInfo.javaVendor})")
                appendLine("  Processors: ${systemInfo.availableProcessors}")
                appendLine("  Memory: ${systemInfo.totalMemoryMB}MB total, ${systemInfo.freeMemoryMB}MB free, ${systemInfo.maxMemoryMB}MB max")
                appendLine()
            }
            
            if (includeMemoryInfo) {
                val memoryInfo = MemoryInfo.current()
                appendLine("Current Memory Usage:")
                appendLine("  Total: ${memoryInfo.totalMemoryMB}MB")
                appendLine("  Used: ${memoryInfo.usedMemoryMB}MB")
                appendLine("  Free: ${memoryInfo.freeMemoryMB}MB")
                appendLine()
            }
            
            if (includePerformanceStats && healthMonitor != null) {
                val metrics = healthMonitor.getHealthMetrics()
                appendLine("Performance Statistics:")
                appendLine("  Uptime: ${metrics.uptimeMs}ms")
                appendLine("  Crash Count: ${metrics.crashCount}")
                appendLine("  Memory Usage: ${metrics.currentMemoryUsageMB}MB / ${metrics.totalMemoryMB}MB")
                appendLine()
            }
        }
    }
    
    /**
     * Perform a health check
     */
    fun performHealthCheck(
        healthMonitor: SystemHealthMonitor? = null
    ): HealthCheckResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check memory usage
        val memoryInfo = MemoryInfo.current()
        val memoryUsagePercent = (memoryInfo.usedMemoryMB.toFloat() / memoryInfo.totalMemoryMB.toFloat()) * 100
        
        when {
            memoryUsagePercent > 90 -> issues.add("Critical memory usage: ${memoryUsagePercent.toInt()}%")
            memoryUsagePercent > 75 -> warnings.add("High memory usage: ${memoryUsagePercent.toInt()}%")
        }
        
        // Check crash count if health monitor is available
        if (healthMonitor != null) {
            val metrics = healthMonitor.getHealthMetrics()
            if (metrics.crashCount > 0) {
                warnings.add("Application has crashed ${metrics.crashCount} time(s)")
            }
        }
        
        val status = when {
            issues.isNotEmpty() -> HealthStatus.CRITICAL
            warnings.isNotEmpty() -> HealthStatus.WARNING
            else -> HealthStatus.HEALTHY
        }
        
        return HealthCheckResult(
            status = status,
            issues = issues,
            warnings = warnings,
            timestamp = kotlin.time.Clock.System.now()
        )
    }
    
    /**
     * Export diagnostic data to string
     */
    fun exportDiagnosticData(
        healthMonitor: SystemHealthMonitor? = null
    ): String {
        return generateDiagnosticReport(
            includeSystemInfo = true,
            includeMemoryInfo = true,
            includePerformanceStats = true,
            healthMonitor = healthMonitor
        )
    }
    
    /**
     * Log diagnostic information
     */
    fun logDiagnostics(
        tag: String = "Diagnostics",
        healthMonitor: SystemHealthMonitor? = null
    ) {
        val report = generateDiagnosticReport(
            includeSystemInfo = true,
            includeMemoryInfo = true,
            includePerformanceStats = true,
            healthMonitor = healthMonitor
        )
        IReaderLog.info(report, tag = tag)
    }
    
    /**
     * Measure operation performance
     */
    inline fun <T> measureOperation(
        operationName: String,
        block: () -> T
    ): Pair<T, Duration> {
        val startTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val result = block()
        val endTime = ireader.core.system.SystemCompat.currentTimeMillis()
        val duration = kotlin.time.Duration.parse("${endTime - startTime}ms")
        
        IReaderLog.debug(
            "Operation '$operationName' completed in ${duration.inWholeMilliseconds}ms",
            tag = "Performance"
        )
        
        return Pair(result, duration)
    }
}

/**
 * System information data class
 */
data class SystemInfo @OptIn(ExperimentalTime::class) constructor(
    val osName: String,
    val osVersion: String,
    val osArch: String,
    val javaVersion: String,
    val javaVendor: String,
    val availableProcessors: Int,
    val totalMemoryMB: Long,
    val freeMemoryMB: Long,
    val maxMemoryMB: Long,
    val timestamp: kotlin.time.Instant
)

/**
 * Health check result
 */
data class HealthCheckResult @OptIn(ExperimentalTime::class) constructor(
    val status: HealthStatus,
    val issues: List<String>,
    val warnings: List<String>,
    val timestamp: kotlin.time.Instant
)

/**
 * Health status enum
 */
enum class HealthStatus {
    HEALTHY,
    WARNING,
    CRITICAL
}
