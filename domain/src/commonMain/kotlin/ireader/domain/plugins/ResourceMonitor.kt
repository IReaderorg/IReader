package ireader.domain.plugins

/**
 * Interface for monitoring plugin resource usage
 * Platform-specific implementations provide actual resource tracking
 * Requirements: 4.1, 4.2, 4.3
 */
interface ResourceMonitor {
    /**
     * Get current CPU usage for a specific plugin
     * @param pluginId The plugin identifier
     * @return CPU usage as percentage (0.0 to 100.0)
     */
    fun getCpuUsage(pluginId: String): Double
    
    /**
     * Get current memory usage for a specific plugin
     * @param pluginId The plugin identifier
     * @return Memory usage in bytes
     */
    fun getMemoryUsage(pluginId: String): Long
    
    /**
     * Get current network usage for a specific plugin
     * @param pluginId The plugin identifier
     * @return Network usage in bytes
     */
    fun getNetworkUsage(pluginId: String): Long
    
    /**
     * Get complete resource usage snapshot for a plugin
     * @param pluginId The plugin identifier
     * @return PluginResourceUsage snapshot
     */
    fun getResourceUsage(pluginId: String): PluginResourceUsage {
        return PluginResourceUsage(
            cpuUsagePercent = getCpuUsage(pluginId),
            memoryUsageBytes = getMemoryUsage(pluginId),
            networkUsageBytes = getNetworkUsage(pluginId)
        )
    }
    
    /**
     * Start monitoring a plugin
     * @param pluginId The plugin identifier
     */
    fun startMonitoring(pluginId: String)
    
    /**
     * Stop monitoring a plugin
     * @param pluginId The plugin identifier
     */
    fun stopMonitoring(pluginId: String)
    
    /**
     * Record network bytes transferred by a plugin
     * @param pluginId The plugin identifier
     * @param bytes Number of bytes transferred
     */
    fun recordNetworkUsage(pluginId: String, bytes: Long)
}
