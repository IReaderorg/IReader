package ireader.domain.plugins

/**
 * Resource usage data for a plugin
 * TODO: Implement actual memory tracking using platform-specific APIs
 */
data class PluginResourceUsage(
    val cpuUsagePercent: Double = 0.0,
    val memoryUsageBytes: Long = 0L,
    val networkUsageBytes: Long = 0L
) {
    /**
     * Memory usage in MB for display purposes
     */
    val memoryUsageMB: Double
        get() = memoryUsageBytes / (1024.0 * 1024.0)
    
    /**
     * Memory limit in MB (default 64 MB)
     */
    val memoryLimitMB: Double = 64.0
}

/**
 * Resource limits for plugins
 */
data class PluginResourceLimits(
    val maxCpuPercent: Double = 50.0,
    val maxMemoryBytes: Long = 64L * 1024 * 1024, // 64 MB
    val maxNetworkBytesPerMinute: Long = 10L * 1024 * 1024 // 10 MB per minute
)
