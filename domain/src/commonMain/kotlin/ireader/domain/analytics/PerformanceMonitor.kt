package ireader.domain.analytics

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Performance monitor for tracking various performance metrics
 * Extends beyond TTS to track UI, network, and database performance
 */
class PerformanceMonitor(
    private val privacyMode: PrivacyMode = PrivacyMode.BALANCED
) {
    val metricsStore = PerformanceMetricsStore()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    /**
     * Record TTS synthesis time
     */
    fun recordSynthesisTime(durationMs: Long, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.SYNTHESIS_TIME,
                    value = durationMs.toDouble(),
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record synthesis time: ${e.message}" }
        }
    }
    
    /**
     * Record UI frame rendering time
     */
    fun recordFrameTime(durationMs: Double, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.UI_FRAME_TIME,
                    value = durationMs,
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record frame time: ${e.message}" }
        }
    }
    
    /**
     * Record network request latency
     */
    fun recordNetworkLatency(durationMs: Long, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.NETWORK_LATENCY,
                    value = durationMs.toDouble(),
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record network latency: ${e.message}" }
        }
    }
    
    /**
     * Record database query time
     */
    fun recordDatabaseQueryTime(durationMs: Long, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.DB_QUERY_TIME,
                    value = durationMs.toDouble(),
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record database query time: ${e.message}" }
        }
    }
    
    /**
     * Record memory usage
     */
    fun recordMemoryUsage(bytes: Long, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.MEMORY_USAGE,
                    value = bytes.toDouble(),
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record memory usage: ${e.message}" }
        }
    }
    
    /**
     * Record CPU usage
     */
    fun recordCpuUsage(percentage: Double, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.CPU_USAGE,
                    value = percentage,
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record CPU usage: ${e.message}" }
        }
    }
    
    /**
     * Record disk I/O time
     */
    fun recordDiskIOTime(durationMs: Long, context: Map<String, String> = emptyMap()) {
        if (privacyMode == PrivacyMode.STRICT) return
        
        try {
            metricsStore.record(
                PerformanceMetrics(
                    metricType = MetricType.DISK_IO_TIME,
                    value = durationMs.toDouble(),
                    context = context
                )
            )
        } catch (e: Exception) {
            Log.error { "Failed to record disk I/O time: ${e.message}" }
        }
    }
    
    /**
     * Get statistics for a specific metric type
     */
    fun getStatistics(type: MetricType): PerformanceStatistics? {
        return try {
            metricsStore.getStatistics(type)
        } catch (e: Exception) {
            Log.error { "Failed to get statistics: ${e.message}" }
            null
        }
    }
    
    /**
     * Get statistics for a time range
     */
    fun getStatistics(type: MetricType, startTime: Long, endTime: Long): PerformanceStatistics? {
        return try {
            metricsStore.getStatistics(type, startTime, endTime)
        } catch (e: Exception) {
            Log.error { "Failed to get statistics for time range: ${e.message}" }
            null
        }
    }
    
    /**
     * Get all metrics for a specific type
     */
    fun getMetrics(type: MetricType): List<PerformanceMetrics> {
        return try {
            metricsStore.getMetrics(type)
        } catch (e: Exception) {
            Log.error { "Failed to get metrics: ${e.message}" }
            emptyList()
        }
    }
    
    /**
     * Clear all metrics
     */
    fun clear() {
        try {
            metricsStore.clear()
        } catch (e: Exception) {
            Log.error { "Failed to clear metrics: ${e.message}" }
        }
    }
    
    /**
     * Measure execution time of a block
     */
    inline fun <T> measureTime(
        metricType: MetricType,
        context: Map<String, String> = emptyMap(),
        block: () -> T
    ): T {
        val startTime = currentTimeToLong()
        try {
            return block()
        } finally {
            val duration = currentTimeToLong() - startTime
            when (metricType) {
                MetricType.SYNTHESIS_TIME -> recordSynthesisTime(duration, context)
                MetricType.NETWORK_LATENCY -> recordNetworkLatency(duration, context)
                MetricType.DB_QUERY_TIME -> recordDatabaseQueryTime(duration, context)
                MetricType.DISK_IO_TIME -> recordDiskIOTime(duration, context)
                else -> {
                    // For other types, record directly
                    metricsStore.record(
                        PerformanceMetrics(
                            metricType = metricType,
                            value = duration.toDouble(),
                            context = context
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Measure execution time of a suspend block
     */
    suspend inline fun <T> measureTimeSuspend(
        metricType: MetricType,
        context: Map<String, String> = emptyMap(),
        crossinline block: suspend () -> T
    ): T {
        val startTime = currentTimeToLong()
        try {
            return block()
        } finally {
            val duration = currentTimeToLong() - startTime
            when (metricType) {
                MetricType.SYNTHESIS_TIME -> recordSynthesisTime(duration, context)
                MetricType.NETWORK_LATENCY -> recordNetworkLatency(duration, context)
                MetricType.DB_QUERY_TIME -> recordDatabaseQueryTime(duration, context)
                MetricType.DISK_IO_TIME -> recordDiskIOTime(duration, context)
                else -> {
                    metricsStore.record(
                        PerformanceMetrics(
                            metricType = metricType,
                            value = duration.toDouble(),
                            context = context
                        )
                    )
                }
            }
        }
    }
}
