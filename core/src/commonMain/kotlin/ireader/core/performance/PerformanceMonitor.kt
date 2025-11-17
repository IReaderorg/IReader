package ireader.core.performance

import ireader.core.log.Log
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

/**
 * Performance monitoring utility following Mihon's patterns
 * Provides timing information for critical operations
 */
object PerformanceMonitor {
    
    /**
     * Measure and log the execution time of a database operation
     */
    suspend inline fun <T> measureDatabaseOperation(
        operationName: String,
        block: suspend () -> T
    ): T {
        val timedValue = measureTimedValue {
            block()
        }
        
        Log.debug("Database operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        // Log warning if operation takes too long
        if (timedValue.duration.inWholeMilliseconds > 1000) {
            Log.warn("Slow database operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        }
        
        return timedValue.value
    }
    
    /**
     * Measure and log the execution time of a network operation
     */
    suspend inline fun <T> measureNetworkOperation(
        operationName: String,
        url: String? = null,
        block: suspend () -> T
    ): T {
        val timedValue = measureTimedValue {
            block()
        }
        
        val urlInfo = url?.let { " (URL: $it)" } ?: ""
        Log.debug("Network operation '$operationName'$urlInfo took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        // Log warning if network operation takes too long
        if (timedValue.duration.inWholeMilliseconds > 5000) {
            Log.warn("Slow network operation '$operationName'$urlInfo took: ${timedValue.duration.inWholeMilliseconds}ms")
        }
        
        return timedValue.value
    }
    
    /**
     * Measure and log UI rendering time
     */
    inline fun <T> measureUIOperation(
        operationName: String,
        block: () -> T
    ): T {
        val timedValue = measureTimedValue {
            block()
        }
        
        Log.debug("UI operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms")
        
        // Log warning if UI operation takes too long (>16ms for 60fps)
        if (timedValue.duration.inWholeMilliseconds > 16) {
            Log.warn("Slow UI operation '$operationName' took: ${timedValue.duration.inWholeMilliseconds}ms (may affect 60fps)")
        }
        
        return timedValue.value
    }
    
    /**
     * Measure memory usage before and after an operation
     */
    inline fun <T> measureMemoryUsage(
        operationName: String,
        block: () -> T
    ): T {
        val runtime = Runtime.getRuntime()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        val result = block()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryDiff = memoryAfter - memoryBefore
        
        Log.debug("Memory usage for '$operationName': ${memoryDiff / 1024 / 1024}MB change")
        
        // Log warning if memory usage increases significantly
        if (memoryDiff > 50 * 1024 * 1024) { // 50MB
            Log.warn("High memory usage for '$operationName': ${memoryDiff / 1024 / 1024}MB increase")
        }
        
        return result
    }
    
    /**
     * Log performance metrics for batch operations
     */
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