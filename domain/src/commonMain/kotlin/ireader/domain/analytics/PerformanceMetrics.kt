package ireader.domain.analytics

/**
 * Performance metrics data class
 * Stores time-series performance data with timestamps
 */
data class PerformanceMetrics(
    val metricType: MetricType,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val context: Map<String, String> = emptyMap()
)

/**
 * Types of performance metrics tracked
 */
enum class MetricType {
    SYNTHESIS_TIME,      // TTS synthesis time in ms
    UI_FRAME_TIME,       // UI frame rendering time in ms
    NETWORK_LATENCY,     // Network request latency in ms
    DB_QUERY_TIME,       // Database query time in ms
    MEMORY_USAGE,        // Memory usage in bytes
    CPU_USAGE,           // CPU usage percentage
    DISK_IO_TIME         // Disk I/O time in ms
}

/**
 * Aggregated performance statistics
 */
data class PerformanceStatistics(
    val metricType: MetricType,
    val count: Int,
    val average: Double,
    val p50: Double,      // 50th percentile (median)
    val p95: Double,      // 95th percentile
    val p99: Double,      // 99th percentile
    val min: Double,
    val max: Double,
    val startTime: Long,
    val endTime: Long
) {
    companion object {
        /**
         * Calculate statistics from a list of metrics
         */
        fun from(metrics: List<PerformanceMetrics>): PerformanceStatistics? {
            if (metrics.isEmpty()) return null
            
            val values = metrics.map { it.value }.sorted()
            val count = values.size
            
            return PerformanceStatistics(
                metricType = metrics.first().metricType,
                count = count,
                average = values.average(),
                p50 = percentile(values, 0.50),
                p95 = percentile(values, 0.95),
                p99 = percentile(values, 0.99),
                min = values.first(),
                max = values.last(),
                startTime = metrics.minOf { it.timestamp },
                endTime = metrics.maxOf { it.timestamp }
            )
        }
        
        private fun percentile(sortedValues: List<Double>, percentile: Double): Double {
            if (sortedValues.isEmpty()) return 0.0
            val index = (sortedValues.size * percentile).toInt().coerceIn(0, sortedValues.size - 1)
            return sortedValues[index]
        }
    }
}

/**
 * Time-series storage for performance metrics
 */
class PerformanceMetricsStore {
    private val metrics = mutableMapOf<MetricType, MutableList<PerformanceMetrics>>()
    private val maxMetricsPerType = 10000 // Limit to prevent memory issues
    
    /**
     * Record a performance metric
     */
    fun record(metric: PerformanceMetrics) {
        val list = metrics.getOrPut(metric.metricType) { mutableListOf() }
        list.add(metric)
        
        // Trim old metrics if exceeding limit
        if (list.size > maxMetricsPerType) {
            list.removeAt(0)
        }
    }
    
    /**
     * Get all metrics for a specific type
     */
    fun getMetrics(type: MetricType): List<PerformanceMetrics> {
        return metrics[type]?.toList() ?: emptyList()
    }
    
    /**
     * Get metrics within a time range
     */
    fun getMetrics(type: MetricType, startTime: Long, endTime: Long): List<PerformanceMetrics> {
        return metrics[type]?.filter { it.timestamp in startTime..endTime } ?: emptyList()
    }
    
    /**
     * Get statistics for a specific metric type
     */
    fun getStatistics(type: MetricType): PerformanceStatistics? {
        val metricsList = metrics[type] ?: return null
        return PerformanceStatistics.from(metricsList)
    }
    
    /**
     * Get statistics for a time range
     */
    fun getStatistics(type: MetricType, startTime: Long, endTime: Long): PerformanceStatistics? {
        val metricsList = getMetrics(type, startTime, endTime)
        return PerformanceStatistics.from(metricsList)
    }
    
    /**
     * Clear all metrics
     */
    fun clear() {
        metrics.clear()
    }
    
    /**
     * Clear metrics for a specific type
     */
    fun clear(type: MetricType) {
        metrics.remove(type)
    }
}
