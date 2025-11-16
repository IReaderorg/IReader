package ireader.domain.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Example usage of the Plugin Resource Monitoring System
 * This demonstrates how to integrate resource monitoring for plugins
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10
 */
class ResourceMonitoringExample(
    private val resourceMonitor: ResourceMonitor
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Create resource tracker
    private val resourceTracker = ResourceTracker(resourceMonitor, scope)
    
    // Create resource limiter
    private val resourceLimiter = ResourceLimiter(resourceTracker, scope)
    
    // Create violation notifier
    private val violationNotifier = ResourceViolationNotifier(
        resourceLimiter = resourceLimiter,
        scope = scope,
        onViolation = { violation ->
            handleResourceViolation(violation)
        }
    )
    
    /**
     * Start monitoring a plugin
     */
    fun startMonitoringPlugin(pluginId: String) {
        // Define custom limits if needed
        val limits = PluginResourceLimits(
            maxCpuPercent = 50.0,
            maxMemoryBytes = 64L * 1024 * 1024, // 64 MB
            maxNetworkBytesPerMinute = 10L * 1024 * 1024 // 10 MB/min
        )
        
        // Start tracking
        resourceTracker.startTracking(pluginId, limits)
        
        // Start enforcement
        resourceLimiter.startEnforcement()
        
        // Start observing violations
        violationNotifier.startObserving()
        
        // Observe usage updates
        resourceTracker.usageFlow
            .onEach { usageMap ->
                usageMap[pluginId]?.let { usage ->
                    println("Plugin $pluginId usage: CPU=${usage.cpuUsagePercent}%, Memory=${usage.memoryUsageMB}MB")
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Stop monitoring a plugin
     */
    fun stopMonitoringPlugin(pluginId: String) {
        resourceTracker.stopTracking(pluginId)
    }
    
    /**
     * Record network usage for a plugin
     */
    fun recordNetworkUsage(pluginId: String, bytes: Long) {
        resourceTracker.recordNetworkUsage(pluginId, bytes)
    }
    
    /**
     * Get current usage for a plugin
     */
    fun getCurrentUsage(pluginId: String): PluginResourceUsage? {
        return resourceTracker.getCurrentUsage(pluginId)
    }
    
    /**
     * Get usage history for a plugin
     */
    fun getUsageHistory(pluginId: String): List<PluginResourceUsage> {
        return resourceTracker.getUsageHistory(pluginId)
    }
    
    /**
     * Check if plugin is throttled
     */
    fun isPluginThrottled(pluginId: String): Boolean {
        return resourceLimiter.isThrottled(pluginId)
    }
    
    /**
     * Check if plugin is suspended
     */
    fun isPluginSuspended(pluginId: String): Boolean {
        return resourceLimiter.isSuspended(pluginId)
    }
    
    /**
     * Handle resource violation
     */
    private fun handleResourceViolation(violation: ResourceViolation) {
        when (violation.type) {
            ViolationType.APPROACHING_LIMIT -> {
                // Show warning to user
                println("WARNING: ${violation.message}")
            }
            ViolationType.LIMIT_EXCEEDED -> {
                // Show error to user
                println("ERROR: ${violation.message}")
            }
            ViolationType.THROTTLED -> {
                // Notify user that plugin is throttled
                println("INFO: Plugin ${violation.pluginId} has been throttled")
            }
            ViolationType.SUSPENDED -> {
                // Notify user that plugin is suspended
                println("CRITICAL: Plugin ${violation.pluginId} has been suspended")
            }
            ViolationType.RESUMED -> {
                // Notify user that plugin is back to normal
                println("INFO: Plugin ${violation.pluginId} has been resumed")
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun shutdown() {
        violationNotifier.stopObserving()
        resourceLimiter.stopEnforcement()
        resourceTracker.shutdown()
        resourceLimiter.shutdown()
    }
}

/**
 * Example usage in a ViewModel or Service
 */
fun exampleUsage(resourceMonitor: ResourceMonitor) {
    val monitoring = ResourceMonitoringExample(resourceMonitor)
    
    // Start monitoring a plugin
    monitoring.startMonitoringPlugin("com.example.plugin")
    
    // Record network usage when plugin makes requests
    monitoring.recordNetworkUsage("com.example.plugin", 1024 * 100) // 100 KB
    
    // Get current usage
    val usage = monitoring.getCurrentUsage("com.example.plugin")
    println("Current usage: $usage")
    
    // Check if throttled
    if (monitoring.isPluginThrottled("com.example.plugin")) {
        println("Plugin is throttled - reduce operations")
    }
    
    // Check if suspended
    if (monitoring.isPluginSuspended("com.example.plugin")) {
        println("Plugin is suspended - stop all operations")
    }
    
    // Get usage history
    val history = monitoring.getUsageHistory("com.example.plugin")
    println("Usage history: ${history.size} entries")
    
    // Stop monitoring when done
    monitoring.stopMonitoringPlugin("com.example.plugin")
    
    // Cleanup
    monitoring.shutdown()
}
