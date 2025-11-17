package ireader.core.log

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * Enhanced logging system following Mihon's logcat patterns
 * Provides structured logging with proper priority levels and performance monitoring
 */
object IReaderLog {
    
    private const val DEFAULT_TAG = "IReader"
    private val logger = Logger.withTag(DEFAULT_TAG)
    
    /**
     * Log debug messages for development and troubleshooting
     */
    fun debug(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        Logger.withTag(tag).d(throwable) { message }
    }
    
    /**
     * Log informational messages
     */
    fun info(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        Logger.withTag(tag).i(throwable) { message }
    }
    
    /**
     * Log warning messages for potential issues
     */
    fun warn(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        Logger.withTag(tag).w(throwable) { message }
    }
    
    /**
     * Log error messages for serious issues
     */
    fun error(message: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        Logger.withTag(tag).e(throwable) { message }
    }
    
    /**
     * Log database operations with timing information
     */
    fun database(operation: String, duration: Long, tag: String = "IReader-DB") {
        val message = "Database operation '$operation' completed in ${duration}ms"
        if (duration > 1000) {
            warn(message, tag = tag)
        } else {
            debug(message, tag = tag)
        }
    }
    
    /**
     * Log network operations with timing and URL information
     */
    fun network(operation: String, url: String?, duration: Long, tag: String = "IReader-Network") {
        val urlInfo = url?.let { " (URL: $it)" } ?: ""
        val message = "Network operation '$operation'$urlInfo completed in ${duration}ms"
        if (duration > 5000) {
            warn(message, tag = tag)
        } else {
            debug(message, tag = tag)
        }
    }
    
    /**
     * Log UI operations with performance implications
     */
    fun ui(operation: String, duration: Long, tag: String = "IReader-UI") {
        val message = "UI operation '$operation' completed in ${duration}ms"
        if (duration > 16) { // 60fps threshold
            warn(message, tag = tag)
        } else {
            debug(message, tag = tag)
        }
    }
    
    /**
     * Log memory usage information
     */
    fun memory(operation: String, memoryChange: Long, tag: String = "IReader-Memory") {
        val memoryMB = memoryChange / 1024 / 1024
        val message = "Memory usage for '$operation': ${memoryMB}MB change"
        if (memoryMB > 50) {
            warn(message, tag = tag)
        } else {
            debug(message, tag = tag)
        }
    }
    
    /**
     * Log accessibility-related information
     */
    fun accessibility(message: String, tag: String = "IReader-A11y") {
        info(message, tag = tag)
    }
    
    /**
     * Log performance benchmarks
     */
    fun benchmark(operation: String, metrics: Map<String, Any> = emptyMap(), tag: String = "IReader-Benchmark") {
        val metricsString = if (metrics.isNotEmpty()) {
            metrics.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        } else {
            "no metrics"
        }
        info("Benchmark '$operation': $metricsString", tag = tag)
    }
}
