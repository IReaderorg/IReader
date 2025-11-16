package ireader.domain.plugins

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks and aggregates resource usage statistics for plugins
 * Updates statistics every 5 seconds in background coroutine
 * Requirements: 4.1, 4.2, 4.4, 4.7, 4.8
 */
class ResourceTracker(
    private val resourceMonitor: ResourceMonitor,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    private val monitors = mutableMapOf<String, PluginResourceMonitor>()
    private val _usageFlow = MutableStateFlow<Map<String, PluginResourceUsage>>(emptyMap())
    val usageFlow: StateFlow<Map<String, PluginResourceUsage>> = _usageFlow.asStateFlow()
    
    private var trackingJob: Job? = null
    private val updateIntervalMs = 5000L // 5 seconds
    
    /**
     * Start tracking a plugin
     * Requirements: 4.1
     */
    fun startTracking(pluginId: String, limits: PluginResourceLimits = PluginResourceLimits()) {
        if (monitors.containsKey(pluginId)) {
            return // Already tracking
        }
        
        val monitor = PluginResourceMonitor(pluginId, limits)
        monitors[pluginId] = monitor
        
        resourceMonitor.startMonitoring(pluginId)
        
        // Start background tracking if not already running
        if (trackingJob == null || trackingJob?.isActive != true) {
            startBackgroundTracking()
        }
    }
    
    /**
     * Stop tracking a plugin
     * Requirements: 4.1
     */
    fun stopTracking(pluginId: String) {
        monitors.remove(pluginId)
        resourceMonitor.stopMonitoring(pluginId)
        
        // Stop background tracking if no plugins are being tracked
        if (monitors.isEmpty()) {
            stopBackgroundTracking()
        }
    }
    
    /**
     * Get current resource usage for a plugin
     * Requirements: 4.2
     */
    fun getCurrentUsage(pluginId: String): PluginResourceUsage? {
        return monitors[pluginId]?.getCurrentUsage()
    }
    
    /**
     * Get resource monitor for a plugin
     * Requirements: 4.2
     */
    fun getMonitor(pluginId: String): PluginResourceMonitor? {
        return monitors[pluginId]
    }
    
    /**
     * Get all tracked plugins
     */
    fun getTrackedPlugins(): List<String> {
        return monitors.keys.toList()
    }
    
    /**
     * Record network usage for a plugin
     * Requirements: 4.3
     */
    fun recordNetworkUsage(pluginId: String, bytes: Long) {
        resourceMonitor.recordNetworkUsage(pluginId, bytes)
    }
    
    /**
     * Check if any plugin has exceeded limits
     * Requirements: 4.4
     */
    fun getPluginsExceedingLimits(): List<String> {
        return monitors.filter { (_, monitor) ->
            monitor.hasExceededLimits()
        }.keys.toList()
    }
    
    /**
     * Check if any plugin should be throttled
     * Requirements: 4.4
     */
    fun getPluginsToThrottle(): List<String> {
        return monitors.filter { (_, monitor) ->
            monitor.shouldThrottle()
        }.keys.toList()
    }
    
    /**
     * Get usage history for a plugin
     * Requirements: 4.8
     */
    fun getUsageHistory(pluginId: String): List<PluginResourceUsage> {
        return monitors[pluginId]?.getUsageHistory() ?: emptyList()
    }
    
    /**
     * Reset tracking for a plugin
     */
    fun resetTracking(pluginId: String) {
        monitors[pluginId]?.reset()
    }
    
    /**
     * Start background tracking coroutine
     * Updates statistics every 5 seconds
     * Requirements: 4.7
     */
    private fun startBackgroundTracking() {
        trackingJob = scope.launch {
            while (isActive) {
                updateAllStatistics()
                delay(updateIntervalMs)
            }
        }
    }
    
    /**
     * Stop background tracking coroutine
     */
    private fun stopBackgroundTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }
    
    /**
     * Update statistics for all tracked plugins
     * Requirements: 4.7
     */
    private suspend fun updateAllStatistics() {
        val usageMap = mutableMapOf<String, PluginResourceUsage>()
        
        monitors.forEach { (pluginId, monitor) ->
            try {
                // Get current resource usage from platform monitor
                val usage = resourceMonitor.getResourceUsage(pluginId)
                
                // Record in plugin monitor
                monitor.recordUsage(
                    cpuUsage = usage.cpuUsagePercent,
                    memoryUsage = usage.memoryUsageBytes,
                    networkUsage = usage.networkUsageBytes
                )
                
                usageMap[pluginId] = usage
            } catch (e: Exception) {
                // Log error but continue tracking other plugins
                println("Error tracking plugin $pluginId: ${e.message}")
            }
        }
        
        // Update flow
        _usageFlow.value = usageMap
    }
    
    /**
     * Cleanup resources
     */
    fun shutdown() {
        stopBackgroundTracking()
        monitors.keys.toList().forEach { pluginId ->
            resourceMonitor.stopMonitoring(pluginId)
        }
        monitors.clear()
        scope.cancel()
    }
}
