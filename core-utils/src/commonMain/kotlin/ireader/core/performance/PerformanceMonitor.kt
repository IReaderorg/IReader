package ireader.core.performance

import ireader.core.log.Log
import kotlin.time.Duration
import kotlin.time.measureTimedValue

/**
 * Performance monitoring utility following Mihon's patterns
 * Provides timing information for critical operations
 */
object PerformanceMonitor {
    
    suspend inline fun <T> measureDatabaseOperation(
        operationName: String,
        block: suspend () -> T
    ): T {
        val timedValue = measureTimedValue { block() }
        
        Log.debug("Database operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        if (timedValue.duration.inWholeMilliseconds > 1000) {
            Log.warn("Slow database operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        }
        
        return timedValue.value
    }
    
    suspend inline fun <T> measureNetworkOperation(
        operationName: String,
        url: String? = null,
        block: suspend () -> T
    ): T {
        val timedValue = measureTimedValue { block() }
        
        val urlInfo = url?.let { " (URL: $it)" } ?: ""
        Log.debug("Network operation '$operationName'$urlInfo took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        if (timedValue.duration.inWholeMilliseconds > 5000) {
            Log.warn("Slow network operation '$operationName'$urlInfo took: ${timedValue.duration.inWholeMilliseconds}ms")
        }
        
        return timedValue.value
    }
    
    inline fun <T> measureUIOperation(
        operationName: String,
        block: () -> T
    ): T {
        val timedValue = measureTimedValue { block() }
        
        Log.debug("UI operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        if (timedValue.duration.inWholeMilliseconds > 16) {
            Log.warn("Slow UI operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms (may affect 60fps)")
        }
        
        return timedValue.value
    }
    
    /**
     * Measure memory usage - platform specific implementation needed
     * On iOS/Native, this is a no-op as Runtime is JVM-only
     */
    inline fun <T> measureMemoryUsage(
        operationName: String,
        block: () -> T
    ): T {
        // Memory measurement is platform-specific
        // On JVM, use Runtime.getRuntime()
        // On iOS/Native, would need platform-specific APIs
        return block()
    }
    
    fun logBatchOperationMetrics(
        operationName: String,
        itemCount: Int,
        duration: Duration,
        successCount: Int = itemCount,
        errorCount: Int = 0
    ) {
        val avgTimePerItem = if (itemCount > 0) duration.inWholeMilliseconds / itemCount else 0
        
        Log.debug(
            "Batch operation '$operationName' completed: " +
            "$itemCount items in ${duration.inWholeMilliseconds}ms " +
            "(avg: ${avgTimePerItem}ms/item, success: $successCount, errors: $errorCount)"
        )
        
        if (avgTimePerItem > 100) {
            Log.warn("Slow batch operation '$operationName': ${avgTimePerItem}ms per item")
        }
    }
}
