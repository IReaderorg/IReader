package ireader.domain.plugins

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Preferences for plugin system configuration
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
class PluginPreferences(private val preferenceStore: PreferenceStore) {
    
    /**
     * Set of enabled plugin IDs
     */
    fun enabledPlugins(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_plugins", emptySet())
    }
    
    /**
     * Whether to automatically update plugins
     */
    fun autoUpdatePlugins(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_update_plugins", true)
    }
    
    /**
     * Interval between plugin update checks in milliseconds
     * Default: 24 hours (86400000ms)
     */
    fun pluginUpdateCheckInterval(): Preference<Long> {
        return preferenceStore.getLong("plugin_update_interval", 86400000L)
    }
    
    /**
     * Get API key for a specific plugin
     * Requirements: 4.2
     */
    fun getPluginApiKey(pluginId: String): Preference<String> {
        return preferenceStore.getString("plugin_api_key_$pluginId", "")
    }
    
    /**
     * Set API key for a specific plugin
     * Requirements: 4.2
     */
    fun setPluginApiKey(pluginId: String, apiKey: String) {
        preferenceStore.getString("plugin_api_key_$pluginId", "").set(apiKey)
    }
}
