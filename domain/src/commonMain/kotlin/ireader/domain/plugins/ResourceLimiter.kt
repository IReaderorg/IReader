package ireader.domain.plugins

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Enforces resource limits for plugins
 * Implements throttling and suspension logic
 * Requirements: 4.4, 4.5, 4.6, 4.9
 */
class ResourceLimiter(
    private val resourceTracker: ResourceTracker,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    private val _violationEvents = MutableSharedFlow<ResourceViolation>()
    val violationEvents: SharedFlow<ResourceViolation> = _violationEvents.asSharedFlow()
    
    private val throttledPlugins = mutableSetOf<String>()
    private val suspendedPlugins = mutableSetOf<String>()
    
    private var enforcementJob: Job? = null
    private val checkIntervalMs = 5000L // Check every 5 seconds
    
    /**
     * Start enforcing limits
     * Requirements: 4.4
     */
    fun startEnforcement() {
        if (enforcementJob?.isActive == true) {
            return // Already running
        }
        
        enforcementJob = scope.launch {
            while (isActive) {
                checkAndEnforceLimits()
                delay(checkIntervalMs)
            }
        }
    }
    
    /**
     * Stop enforcing limits
     */
    fun stopEnforcement() {
        enforcementJob?.cancel()
        enforcementJob = null
    }
    
    /**
     * Check if a plugin is throttled
     * Requirements: 4.5
     */
    fun isThrottled(pluginId: String): Boolean {
        return throttledPlugins.contains(pluginId)
    }
    
    /**
     * Check if a plugin is suspended
     * Requirements: 4.6
     */
    fun isSuspended(pluginId: String): Boolean {
        return suspendedPlugins.contains(pluginId)
    }
    
    /**
     * Manually throttle a plugin
     * Requirements: 4.5
     */
    fun throttlePlugin(pluginId: String) {
        if (throttledPlugins.add(pluginId)) {
            scope.launch {
                _violationEvents.emit(
                    ResourceViolation(
                        pluginId = pluginId,
                        type = ViolationType.THROTTLED,
                        message = "Plugin throttled due to high resource usage"
                    )
                )
            }
        }
    }
    
    /**
     * Manually suspend a plugin
     * Requirements: 4.6
     */
    fun suspendPlugin(pluginId: String) {
        if (suspendedPlugins.add(pluginId)) {
            throttledPlugins.remove(pluginId) // Remove from throttled if present
            scope.launch {
                _violationEvents.emit(
                    ResourceViolation(
                        pluginId = pluginId,
                        type = ViolationType.SUSPENDED,
                        message = "Plugin suspended due to exceeding resource limits"
                    )
                )
            }
        }
    }
    
    /**
     * Resume a throttled plugin
     * Requirements: 4.5
     */
    fun resumeThrottled(pluginId: String) {
        if (throttledPlugins.remove(pluginId)) {
            scope.launch {
                _violationEvents.emit(
                    ResourceViolation(
                        pluginId = pluginId,
                        type = ViolationType.RESUMED,
                        message = "Plugin throttling removed"
                    )
                )
            }
        }
    }
    
    /**
     * Resume a suspended plugin
     * Requirements: 4.6
     */
    fun resumeSuspended(pluginId: String) {
        if (suspendedPlugins.remove(pluginId)) {
            scope.launch {
                _violationEvents.emit(
                    ResourceViolation(
                        pluginId = pluginId,
                        type = ViolationType.RESUMED,
                        message = "Plugin suspension removed"
                    )
                )
            }
        }
    }
    
    /**
     * Check and enforce limits for all tracked plugins
     * Requirements: 4.4, 4.5, 4.6
     */
    private suspend fun checkAndEnforceLimits() {
        val trackedPlugins = resourceTracker.getTrackedPlugins()
        
        for (pluginId in trackedPlugins) {
            // Skip if already suspended
            if (suspendedPlugins.contains(pluginId)) {
                continue
            }
            
            val monitor = resourceTracker.getMonitor(pluginId) ?: continue
            
            // Check if limits are exceeded
            if (monitor.hasExceededLimits()) {
                // Suspend the plugin
                suspendPlugin(pluginId)
                
                val usage = monitor.getCurrentUsage()
                val percentages = monitor.getUsagePercentages()
                
                _violationEvents.emit(
                    ResourceViolation(
                        pluginId = pluginId,
                        type = ViolationType.LIMIT_EXCEEDED,
                        message = buildLimitExceededMessage(usage, percentages),
                        usage = usage
                    )
                )
            } else if (monitor.shouldThrottle()) {
                // Throttle the plugin if not already throttled
                if (!throttledPlugins.contains(pluginId)) {
                    throttlePlugin(pluginId)
                    
                    val usage = monitor.getCurrentUsage()
                    val percentages = monitor.getUsagePercentages()
                    
                    _violationEvents.emit(
                        ResourceViolation(
                            pluginId = pluginId,
                            type = ViolationType.APPROACHING_LIMIT,
                            message = buildApproachingLimitMessage(percentages),
                            usage = usage
                        )
                    )
                }
            } else {
                // Resume if throttled and usage is back to normal
                if (throttledPlugins.contains(pluginId)) {
                    resumeThrottled(pluginId)
                }
            }
        }
    }
    
    /**
     * Build message for limit exceeded violation
     */
    private fun buildLimitExceededMessage(
        usage: PluginResourceUsage,
        percentages: ResourceUsagePercentages
    ): String {
        val violations = mutableListOf<String>()
        
        if (percentages.cpuPercent > 100) {
            violations.add("CPU: ${usage.cpuUsagePercent.format(1)}% (limit: 50%)")
        }
        if (percentages.memoryPercent > 100) {
            violations.add("Memory: ${usage.memoryUsageMB.format(1)} MB (limit: 64 MB)")
        }
        if (percentages.networkPercent > 100) {
            violations.add("Network: ${(usage.networkUsageBytes / (1024.0 * 1024.0)).format(1)} MB/min (limit: 10 MB/min)")
        }
        
        return "Resource limits exceeded: ${violations.joinToString(", ")}"
    }
    
    /**
     * Build message for approaching limit warning
     */
    private fun buildApproachingLimitMessage(percentages: ResourceUsagePercentages): String {
        val warnings = mutableListOf<String>()
        
        if (percentages.cpuPercent > 80) {
            warnings.add("CPU at ${percentages.cpuPercent.format(0)}%")
        }
        if (percentages.memoryPercent > 80) {
            warnings.add("Memory at ${percentages.memoryPercent.format(0)}%")
        }
        if (percentages.networkPercent > 80) {
            warnings.add("Network at ${percentages.networkPercent.format(0)}%")
        }
        
        return "Resource usage high: ${warnings.joinToString(", ")}"
    }
    
    /**
     * Cleanup resources
     */
    fun shutdown() {
        stopEnforcement()
        throttledPlugins.clear()
        suspendedPlugins.clear()
        scope.cancel()
    }
}

/**
 * Resource violation event
 * Requirements: 4.9
 */
data class ResourceViolation(
    val pluginId: String,
    val type: ViolationType,
    val message: String,
    val usage: PluginResourceUsage? = null,
    val timestamp: Long = currentTimeToLong()
)

/**
 * Types of resource violations
 */
enum class ViolationType {
    APPROACHING_LIMIT,
    LIMIT_EXCEEDED,
    THROTTLED,
    SUSPENDED,
    RESUMED
}

/**
 * Extension function to format doubles
 */
private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
