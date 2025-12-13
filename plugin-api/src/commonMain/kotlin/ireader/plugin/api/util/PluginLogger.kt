package ireader.plugin.api.util

/**
 * Simple logging utility for plugins.
 * Provides consistent logging across all plugin types.
 */
object PluginLogger {
    
    /**
     * Log level enum
     */
    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Current minimum log level (can be configured)
     */
    var minLevel: Level = Level.DEBUG
    
    /**
     * Log listeners for custom log handling
     */
    private val listeners = mutableListOf<(Level, String, String, Throwable?) -> Unit>()
    
    /**
     * Add a log listener
     */
    fun addListener(listener: (Level, String, String, Throwable?) -> Unit) {
        listeners.add(listener)
    }
    
    /**
     * Remove a log listener
     */
    fun removeListener(listener: (Level, String, String, Throwable?) -> Unit) {
        listeners.remove(listener)
    }
    
    /**
     * Log a debug message
     */
    fun d(tag: String, message: String) {
        log(Level.DEBUG, tag, message, null)
    }
    
    /**
     * Log an info message
     */
    fun i(tag: String, message: String) {
        log(Level.INFO, tag, message, null)
    }
    
    /**
     * Log a warning message
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.WARN, tag, message, throwable)
    }
    
    /**
     * Log an error message
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }
    
    private fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
        if (level.ordinal < minLevel.ordinal) return
        
        // Notify listeners
        listeners.forEach { it(level, tag, message, throwable) }
        
        // Default console output
        val prefix = when (level) {
            Level.DEBUG -> "[D]"
            Level.INFO -> "[I]"
            Level.WARN -> "[W]"
            Level.ERROR -> "[E]"
        }
        println("$prefix [$tag] $message")
        throwable?.printStackTrace()
    }
}

/**
 * Extension to create a tagged logger for a plugin
 */
fun createPluginLogger(pluginId: String): TaggedLogger {
    return TaggedLogger(pluginId)
}

/**
 * Tagged logger that automatically includes plugin ID
 */
class TaggedLogger(private val tag: String) {
    fun d(message: String) = PluginLogger.d(tag, message)
    fun i(message: String) = PluginLogger.i(tag, message)
    fun w(message: String, throwable: Throwable? = null) = PluginLogger.w(tag, message, throwable)
    fun e(message: String, throwable: Throwable? = null) = PluginLogger.e(tag, message, throwable)
}
