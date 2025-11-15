package ireader.domain.monitoring

/**
 * Interface for tracking memory usage of plugins
 * Platform-specific implementations provide actual memory tracking
 */
interface MemoryTracker {
    /**
     * Get current memory usage for a specific plugin
     * @param pluginId The plugin identifier
     * @return Memory usage in bytes
     */
    fun getMemoryUsage(pluginId: String): Long
    
    /**
     * Get total memory usage of the application
     * @return Total memory usage in bytes
     */
    fun getTotalMemoryUsage(): Long
    
    /**
     * Get available memory
     * @return Available memory in bytes
     */
    fun getAvailableMemory(): Long
    
    /**
     * Get memory limit for the application
     * @return Memory limit in bytes
     */
    fun getMemoryLimit(): Long
    
    /**
     * Start tracking memory for a plugin
     * Records baseline memory before plugin operation
     * @param pluginId The plugin identifier
     */
    fun startTracking(pluginId: String)
    
    /**
     * Stop tracking memory for a plugin
     * Calculates memory used by the plugin since startTracking
     * @param pluginId The plugin identifier
     */
    fun stopTracking(pluginId: String)
}

/**
 * Snapshot of memory usage at a point in time
 */
data class MemorySnapshot(
    val pluginId: String,
    val usedMemory: Long,
    val timestamp: Long = System.currentTimeMillis()
)
