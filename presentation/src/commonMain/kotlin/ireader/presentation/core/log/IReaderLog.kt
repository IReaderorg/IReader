package ireader.presentation.core.log

import ireader.core.log.Log

/**
 * IReader logging utility following Mihon's logcat pattern.
 * Provides structured logging with proper priority levels and tagging.
 */
object IReaderLog {
    
    /**
     * Log a debug message
     */
    fun debug(message: String, tag: String = "IReader") {
        Log.debug("[$tag] $message")
    }
    
    /**
     * Log an info message
     */
    fun info(message: String, tag: String = "IReader") {
        Log.info("[$tag] $message")
    }
    
    /**
     * Log a warning message
     */
    fun warn(message: String, throwable: Throwable? = null, tag: String = "IReader") {
        if (throwable != null) {
            Log.warn(throwable, "[$tag] $message")
        } else {
            Log.warn("[$tag] $message")
        }
    }
    
    /**
     * Log an error message
     */
    fun error(message: String, throwable: Throwable? = null, tag: String = "IReader") {
        if (throwable != null) {
            Log.error(throwable, "[$tag] $message")
        } else {
            Log.error("[$tag] $message")
        }
    }
}