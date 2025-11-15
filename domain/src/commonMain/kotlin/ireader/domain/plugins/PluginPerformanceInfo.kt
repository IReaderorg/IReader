package ireader.domain.plugins

/**
 * Performance metrics for a plugin.
 * TODO: Implement actual memory tracking
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
)
