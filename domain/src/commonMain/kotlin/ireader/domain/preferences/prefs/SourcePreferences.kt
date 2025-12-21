package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Preferences for source/plugin management and error handling.
 */
class SourcePreferences(private val preferenceStore: PreferenceStore) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Set of source IDs that the user has chosen to skip the "unavailable" warning for.
     * These sources will still show a badge but won't show the full-screen warning.
     */
    fun skippedUnavailableSources(): Preference<Set<String>> {
        return preferenceStore.getStringSet("skipped_unavailable_sources", emptySet())
    }
    
    /**
     * Set of plugin IDs that the user has chosen to skip the "not working" warning for.
     */
    fun skippedBrokenPlugins(): Preference<Set<String>> {
        return preferenceStore.getStringSet("skipped_broken_plugins", emptySet())
    }
    
    /**
     * Whether to show unavailable source warnings.
     * If false, only badges will be shown without full-screen warnings.
     */
    fun showUnavailableSourceWarnings(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_unavailable_source_warnings", true)
    }
    
    /**
     * Whether to show plugin not working warnings.
     */
    fun showPluginNotWorkingWarnings(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_plugin_not_working_warnings", true)
    }
    
    /**
     * Map of source IDs to their last known error messages.
     * Used to display error information in the UI.
     */
    fun sourceErrors(): Preference<String> {
        return preferenceStore.getString("source_errors", "{}")
    }
    
    /**
     * Get source errors as a map.
     */
    fun getSourceErrorsMap(): Map<Long, String> {
        return try {
            json.decodeFromString<Map<Long, String>>(sourceErrors().get())
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Set error for a specific source.
     */
    fun setSourceError(sourceId: Long, error: String) {
        val current = getSourceErrorsMap().toMutableMap()
        current[sourceId] = error
        sourceErrors().set(json.encodeToString(current))
    }
    
    /**
     * Clear error for a specific source.
     */
    fun clearSourceError(sourceId: Long) {
        val current = getSourceErrorsMap().toMutableMap()
        current.remove(sourceId)
        sourceErrors().set(json.encodeToString(current))
    }
    
    /**
     * Clear all source errors.
     */
    fun clearAllSourceErrors() {
        sourceErrors().set("{}")
    }
    
    /**
     * Add a source to the skipped list.
     */
    fun skipUnavailableSource(sourceId: Long) {
        val current = skippedUnavailableSources().get().toMutableSet()
        current.add(sourceId.toString())
        skippedUnavailableSources().set(current)
    }
    
    /**
     * Remove a source from the skipped list.
     */
    fun unskipUnavailableSource(sourceId: Long) {
        val current = skippedUnavailableSources().get().toMutableSet()
        current.remove(sourceId.toString())
        skippedUnavailableSources().set(current)
    }
    
    /**
     * Check if a source is in the skipped list.
     */
    fun isSourceSkipped(sourceId: Long): Boolean {
        return sourceId.toString() in skippedUnavailableSources().get()
    }
    
    /**
     * Add a plugin to the skipped list.
     */
    fun skipBrokenPlugin(pluginId: String) {
        val current = skippedBrokenPlugins().get().toMutableSet()
        current.add(pluginId)
        skippedBrokenPlugins().set(current)
    }
    
    /**
     * Remove a plugin from the skipped list.
     */
    fun unskipBrokenPlugin(pluginId: String) {
        val current = skippedBrokenPlugins().get().toMutableSet()
        current.remove(pluginId)
        skippedBrokenPlugins().set(current)
    }
    
    /**
     * Check if a plugin is in the skipped list.
     */
    fun isPluginSkipped(pluginId: String): Boolean {
        return pluginId in skippedBrokenPlugins().get()
    }
    
    /**
     * Clear all skipped sources and plugins.
     */
    fun clearAllSkipped() {
        skippedUnavailableSources().set(emptySet())
        skippedBrokenPlugins().set(emptySet())
    }
}
