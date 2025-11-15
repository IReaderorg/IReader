package ireader.domain.plugins

/**
 * Errors that can occur during plugin loading and validation
 * Requirements: 17.1, 17.2, 17.3, 17.4, 17.5
 */
sealed class PluginError {
    /**
     * Plugin manifest is invalid or malformed
     */
    data class InvalidManifest(val reason: String) : PluginError()
    
    /**
     * Plugin requires a newer version of IReader
     */
    data class IncompatibleVersion(val required: String, val current: String) : PluginError()
    
    /**
     * Plugin requires permissions that are not available
     */
    data class MissingPermissions(val permissions: List<PluginPermission>) : PluginError()
    
    /**
     * Failed to load plugin from package
     */
    data class LoadFailed(val exception: Throwable) : PluginError()
    
    /**
     * Plugin initialization failed
     */
    data class InitializationFailed(val exception: Throwable) : PluginError()
    
    /**
     * Plugin not found in registry or filesystem
     */
    data object PluginNotFound : PluginError()
}

/**
 * Convert plugin error to user-friendly message
 */
fun PluginError.toUserMessage(): String = when (this) {
    is PluginError.InvalidManifest -> "Plugin manifest is invalid: $reason"
    is PluginError.IncompatibleVersion -> "Plugin requires IReader $required or higher (current: $current)"
    is PluginError.MissingPermissions -> "Plugin requires permissions: ${permissions.joinToString { it.name }}"
    is PluginError.LoadFailed -> "Failed to load plugin: ${exception.message ?: "Unknown error"}"
    is PluginError.InitializationFailed -> "Failed to initialize plugin: ${exception.message ?: "Unknown error"}"
    is PluginError.PluginNotFound -> "Plugin not found"
}
