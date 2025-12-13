package ireader.plugin.api

/**
 * Plugin lifecycle callbacks.
 * Plugins can implement this interface to receive lifecycle events.
 */
interface PluginLifecycle {
    
    /**
     * Called when the plugin is first loaded.
     * Use this for one-time initialization.
     */
    suspend fun onLoad() {}
    
    /**
     * Called when the plugin is enabled.
     * Use this to start services, register listeners, etc.
     */
    suspend fun onEnable() {}
    
    /**
     * Called when the plugin is disabled.
     * Use this to stop services, unregister listeners, etc.
     */
    suspend fun onDisable() {}
    
    /**
     * Called when the plugin is about to be unloaded.
     * Use this for cleanup, saving state, etc.
     */
    suspend fun onUnload() {}
    
    /**
     * Called when the plugin is updated from a previous version.
     * @param previousVersion The version code of the previously installed version
     */
    suspend fun onUpdate(previousVersion: Int) {}
    
    /**
     * Called when the app configuration changes (e.g., language, theme).
     */
    suspend fun onConfigurationChanged() {}
}

/**
 * Plugin state that can be saved and restored.
 * Implement this to persist plugin state across sessions.
 */
interface PluginStateful {
    
    /**
     * Save plugin state to a map.
     * Called when the plugin is disabled or the app is closing.
     */
    fun saveState(): Map<String, String>
    
    /**
     * Restore plugin state from a map.
     * Called when the plugin is enabled.
     */
    fun restoreState(state: Map<String, String>)
}

/**
 * Plugin that can be configured by the user.
 */
interface PluginConfigurable {
    
    /**
     * Get the configuration schema for this plugin.
     * Used to generate settings UI.
     */
    fun getConfigSchema(): List<ConfigField>
    
    /**
     * Get current configuration values.
     */
    fun getConfig(): Map<String, Any?>
    
    /**
     * Update configuration values.
     */
    fun updateConfig(values: Map<String, Any?>)
    
    /**
     * Reset configuration to defaults.
     */
    fun resetConfig()
}

/**
 * Configuration field definition.
 */
sealed class ConfigField {
    abstract val key: String
    abstract val label: String
    abstract val description: String?
    abstract val required: Boolean
    
    data class Text(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val default: String = "",
        val maxLength: Int? = null,
        val placeholder: String? = null,
        val isPassword: Boolean = false
    ) : ConfigField()
    
    data class Number(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val default: Double = 0.0,
        val min: Double? = null,
        val max: Double? = null,
        val step: Double = 1.0
    ) : ConfigField()
    
    data class Toggle(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val default: Boolean = false
    ) : ConfigField()
    
    data class Select(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<SelectOption>,
        val default: String? = null
    ) : ConfigField()
    
    data class MultiSelect(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<SelectOption>,
        val default: List<String> = emptyList()
    ) : ConfigField()
}

/**
 * Option for select fields.
 */
data class SelectOption(
    val value: String,
    val label: String
)

/**
 * Plugin health check interface.
 * Implement this to provide health status information.
 */
interface PluginHealthCheck {
    
    /**
     * Check if the plugin is healthy and functioning correctly.
     */
    suspend fun checkHealth(): HealthStatus
}

/**
 * Health status result.
 */
data class HealthStatus(
    val isHealthy: Boolean,
    val message: String? = null,
    val details: Map<String, String> = emptyMap()
) {
    companion object {
        fun healthy(message: String? = null) = HealthStatus(true, message)
        fun unhealthy(message: String, details: Map<String, String> = emptyMap()) = 
            HealthStatus(false, message, details)
    }
}
