package ireader.domain.js.util

import ireader.core.log.Log
import ireader.domain.js.models.JSPluginError

/**
 * Logger for JavaScript plugin operations.
 * All log messages are prefixed with "[JSPlugin]" for easy filtering.
 */
object JSPluginLogger {
    
    private const val PREFIX = "[JSPlugin]"
    
    /**
     * Logs plugin loading operation.
     * @param pluginId The plugin identifier
     * @param success Whether the load was successful
     * @param duration Load duration in milliseconds
     */
    fun logPluginLoad(pluginId: String, success: Boolean, duration: Long) {
        if (success) {
            Log.info { "$PREFIX Plugin '$pluginId' loaded successfully in ${duration}ms" }
        } else {
            Log.error { "$PREFIX Plugin '$pluginId' failed to load after ${duration}ms" }
        }
    }
    
    /**
     * Logs plugin method call.
     * @param pluginId The plugin identifier
     * @param method The method name
     * @param duration Execution duration in milliseconds
     * @param success Whether the call was successful
     */
    fun logMethodCall(pluginId: String, method: String, duration: Long, success: Boolean) {
        if (success) {
            Log.debug { "$PREFIX Plugin '$pluginId' executed '$method' in ${duration}ms" }
        } else {
            Log.warn { "$PREFIX Plugin '$pluginId' failed to execute '$method' after ${duration}ms" }
        }
    }
    
    /**
     * Logs plugin error.
     * @param pluginId The plugin identifier
     * @param error The error that occurred
     */
    fun logError(pluginId: String, error: JSPluginError) {
        Log.error(error, "$PREFIX Error in plugin '$pluginId': ${error.message}")
    }
    
    /**
     * Logs debug message for plugin.
     * @param pluginId The plugin identifier
     * @param message The debug message
     */
    fun logDebug(pluginId: String, message: String) {
        Log.debug { "$PREFIX [$pluginId] $message" }
    }
    
    /**
     * Logs info message for plugin.
     * @param pluginId The plugin identifier
     * @param message The info message
     */
    fun logInfo(pluginId: String, message: String) {
        Log.info { "$PREFIX [$pluginId] $message" }
    }
    
    /**
     * Logs warning message for plugin.
     * @param pluginId The plugin identifier
     * @param message The warning message
     */
    fun logWarn(pluginId: String, message: String) {
        Log.warn { "$PREFIX [$pluginId] $message" }
    }
}
