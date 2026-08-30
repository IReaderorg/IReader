package ireader.domain.monitoring

import ireader.domain.plugins.PluginPerformanceInfo
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for tracking and aggregating plugin performance metrics
 */
class PerformanceMetricsManager(
    private val memoryTracker: MemoryTracker
) {
    private val lock = Any()
    private val metricsMap = mutableMapOf<String, PluginPerformanceMetrics>()

    /**
     * Start tracking an operation for a plugin
     */
    fun startOperation(pluginId: String, operation: String) {
        memoryTracker.startTracking(pluginId)

        val metrics = synchronized(lock) {
            metricsMap.getOrPut(pluginId) {
                PluginPerformanceMetrics(pluginId)
            }
        }

        metrics.startOperation(operation)
    }

    /**
     * End tracking an operation for a plugin
     */
    fun endOperation(pluginId: String, operation: String, success: Boolean) {
        memoryTracker.stopTracking(pluginId)

        val metrics = synchronized(lock) { metricsMap[pluginId] } ?: return
        metrics.endOperation(operation, success)

        // Update memory usage
        val memoryUsage = memoryTracker.getMemoryUsage(pluginId)
        metrics.updateMemoryUsage(memoryUsage)
    }

    /**
     * Record an error for a plugin
     */
    fun recordError(pluginId: String, error: Throwable) {
        val metrics = synchronized(lock) {
            metricsMap.getOrPut(pluginId) {
                PluginPerformanceMetrics(pluginId)
            }
        }
        metrics.recordError(error)
    }

    /**
     * Get performance metrics for a specific plugin
     */
    fun getMetrics(pluginId: String): PluginPerformanceInfo {
        val metrics = synchronized(lock) { metricsMap[pluginId] } ?: return PluginPerformanceInfo(pluginId)
        return metrics.toPerformanceInfo()
    }

    /**
     * Get performance metrics for all tracked plugins
     */
    fun getAllMetrics(): List<PluginPerformanceInfo> {
        val allMetrics = synchronized(lock) { metricsMap.values.toList() }
        return allMetrics.map { it.toPerformanceInfo() }
    }

    /**
     * Clear metrics for a specific plugin
     */
    fun clearMetrics(pluginId: String) {
        synchronized(lock) {
            metricsMap.remove(pluginId)
        }
    }

    /**
     * Clear all metrics
     */
    fun clearAllMetrics() {
        synchronized(lock) {
            metricsMap.clear()
        }
    }
}

/**
 * Internal class for tracking plugin performance metrics
 */
private class PluginPerformanceMetrics(
    val pluginId: String
) {
    private val lock = Any()
    private val operationTimes = mutableListOf<Long>()
    private val operationStarts = mutableMapOf<String, Long>()
    private var errorCount = 0
    private var totalOperations = 0
    private var memoryUsage = 0L
    private var maxMemoryUsage = 0L
    private var loadTime = 0L

    fun startOperation(operation: String) {
        synchronized(lock) {
            operationStarts[operation] = currentTimeToLong()
        }
    }

    fun endOperation(operation: String, success: Boolean) {
        val (startTime, duration) = synchronized(lock) {
            val start = operationStarts.remove(operation) ?: return
            val dur = currentTimeToLong() - start
            operationTimes.add(dur)
            totalOperations++
            if (!success) {
                errorCount++
            }
            if (operation == "load") {
                loadTime = dur
            }
            start to dur
        }
    }

    fun recordError(error: Throwable) {
        synchronized(lock) {
            errorCount++
        }
    }

    fun updateMemoryUsage(usage: Long) {
        synchronized(lock) {
            memoryUsage = usage
            maxMemoryUsage = maxOf(maxMemoryUsage, usage)
        }
    }

    fun toPerformanceInfo(): PluginPerformanceInfo {
        return synchronized(lock) {
            val avgTime = if (operationTimes.isNotEmpty()) {
                operationTimes.average().toLong()
            } else 0L

            val maxTime = operationTimes.maxOrNull() ?: 0L

            val errorRate = if (totalOperations > 0) {
                errorCount.toFloat() / totalOperations.toFloat()
            } else 0f

            PluginPerformanceInfo(
                pluginId = pluginId,
                memoryUsageMB = memoryUsage / (1024.0 * 1024.0),
                memoryLimitMB = 64.0, // Configurable limit
                loadTime = loadTime,
                avgExecutionTime = avgTime,
                maxExecutionTime = maxTime,
                errorRate = errorRate,
                lastUpdated = currentTimeToLong()
            )
        }
    }
}
