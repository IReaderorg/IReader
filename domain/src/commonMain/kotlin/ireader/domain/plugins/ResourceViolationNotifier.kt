package ireader.domain.plugins

import ireader.domain.utils.extensions.formatDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Handles resource violation notifications
 * Requirements: 4.9, 4.10
 */
class ResourceViolationNotifier(
    private val resourceLimiter: ResourceLimiter,
    private val scope: CoroutineScope,
    private val onViolation: (ResourceViolation) -> Unit
) {
    
    private var observerJob: kotlinx.coroutines.Job? = null
    
    /**
     * Start observing resource violations
     */
    fun startObserving() {
        observerJob = resourceLimiter.violationEvents
            .onEach { violation ->
                handleViolation(violation)
            }
            .launchIn(scope)
    }
    
    /**
     * Stop observing resource violations
     */
    fun stopObserving() {
        observerJob?.cancel()
        observerJob = null
    }
    
    /**
     * Handle a resource violation
     */
    private fun handleViolation(violation: ResourceViolation) {
        // Notify the callback
        onViolation(violation)
        
        // Log the violation
        logViolation(violation)
    }
    
    /**
     * Log a resource violation
     */
    private fun logViolation(violation: ResourceViolation) {
        val timestamp = violation.timestamp.formatDateTime()
        
        println("[$timestamp] Plugin Resource Violation:")
        println("  Plugin ID: ${violation.pluginId}")
        println("  Type: ${violation.type}")
        println("  Message: ${violation.message}")
        
        violation.usage?.let { usage ->
            println("  CPU: ${usage.cpuUsagePercent}%")
            println("  Memory: ${usage.memoryUsageMB} MB")
            println("  Network: ${usage.networkUsageBytes / (1024.0 * 1024.0)} MB")
        }
    }
}

/**
 * Extension to get violation events flow
 */
val ResourceLimiter.violations: SharedFlow<ResourceViolation>
    get() = violationEvents
