package ireader.domain.plugins

/**
 * Performance metrics for a plugin.
 */
data class PluginPerformanceInfo(
    val pluginId: String,
    val memoryUsageMB: Double = 0.0,  // Current memory usage in MB
    val memoryLimitMB: Double = 64.0,  // Memory limit in MB
    val loadTime: Long = 0L,           // Plugin load time in ms
    val avgExecutionTime: Long = 0L,   // Average method execution time in ms
    val maxExecutionTime: Long = 0L,   // Maximum method execution time in ms
    val errorRate: Float = 0.0f,       // Error rate (0.0 to 1.0)
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Memory usage as a percentage of the limit
     */
    val memoryUsagePercentage: Float
        get() = (memoryUsageMB / memoryLimitMB * 100).toFloat()

    /**
     * Whether memory usage is in warning range (>80%)
     */
    val isMemoryWarning: Boolean
        get() = memoryUsagePercentage > 80

    /**
     * Whether memory usage is critical (>95%)
     */
    val isMemoryCritical: Boolean
        get() = memoryUsagePercentage > 95
}
