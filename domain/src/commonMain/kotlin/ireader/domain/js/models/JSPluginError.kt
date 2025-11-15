package ireader.domain.js.models

/**
 * Sealed class representing different types of errors that can occur in JavaScript plugins.
 */
sealed class JSPluginError : Exception() {
    
    abstract val pluginId: String
    
    /**
     * Error loading the plugin.
     */
    data class LoadError(
        override val pluginId: String,
        override val cause: Throwable?
    ) : JSPluginError() {
        override val message: String
            get() = "Failed to load plugin '$pluginId': ${cause?.message}"
    }
    
    /**
     * Error executing a plugin method.
     */
    data class ExecutionError(
        override val pluginId: String,
        val method: String,
        override val cause: Throwable?
    ) : JSPluginError() {
        override val message: String
            get() = "Failed to execute '$method' in plugin '$pluginId': ${cause?.message}"
    }
    
    /**
     * Plugin method execution timed out.
     */
    data class TimeoutError(
        override val pluginId: String,
        val method: String
    ) : JSPluginError() {
        override val message: String
            get() = "Timeout executing '$method' in plugin '$pluginId'"
    }
    
    /**
     * Plugin validation failed.
     */
    data class ValidationError(
        override val pluginId: String,
        val reason: String
    ) : JSPluginError() {
        override val message: String
            get() = "Plugin '$pluginId' validation failed: $reason"
    }
    
    /**
     * Network request failed.
     */
    data class NetworkError(
        override val pluginId: String,
        val url: String,
        override val cause: Throwable?
    ) : JSPluginError() {
        override val message: String
            get() = "Network request failed in plugin '$pluginId' for URL '$url': ${cause?.message}"
    }
}

/**
 * Converts a JSPluginError to a user-friendly message.
 */
fun JSPluginError.toUserMessage(): String {
    return when (this) {
        is JSPluginError.LoadError -> "Failed to load plugin. Please check if the plugin file is valid."
        is JSPluginError.ExecutionError -> "An error occurred while fetching data from the source."
        is JSPluginError.TimeoutError -> "The request took too long. Please try again."
        is JSPluginError.ValidationError -> "Plugin validation failed: $reason"
        is JSPluginError.NetworkError -> "Network error. Please check your connection and try again."
    }
}
