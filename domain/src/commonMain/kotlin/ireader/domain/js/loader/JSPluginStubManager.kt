package ireader.domain.js.loader

import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.js.models.PluginMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manages stub/placeholder data for JS plugins to enable fast startup.
 * Stores metadata of previously loaded plugins and creates lightweight stubs
 * that can be displayed immediately while actual plugins load in the background.
 */
class JSPluginStubManager(
    private val preferenceStore: PreferenceStore
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val stubsPref = preferenceStore.getString("js_plugin_stubs", "")
    private val priorityPluginsPref = preferenceStore.getString("js_plugin_priority", "")
    
    /**
     * Save plugin metadata as stub for future fast loading.
     */
    fun savePluginStub(metadata: PluginMetadata, fileName: String) {
        try {
            val stubs = getPluginStubs().toMutableMap()
            stubs[metadata.id] = PluginStubData(
                metadata = metadata,
                fileName = fileName,
                lastLoaded = currentTimeToLong()
            )
            
            val serialized = json.encodeToString(stubs)
            stubsPref.set(serialized)
        } catch (e: Exception) {
            Log.error("JSPluginStubManager: Failed to save stub for ${metadata.id}", e)
        }
    }
    
    /**
     * Get all saved plugin stubs.
     */
    fun getPluginStubs(): Map<String, PluginStubData> {
        return try {
            val serialized = stubsPref.get()
            if (serialized.isNotEmpty()) {
                json.decodeFromString<Map<String, PluginStubData>>(serialized)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.error("JSPluginStubManager: Failed to load stubs", e)
            emptyMap()
        }
    }
    
    /**
     * Remove a plugin stub (when plugin is deleted or fails to load).
     */
    fun removePluginStub(pluginId: String) {
        try {
            val stubs = getPluginStubs().toMutableMap()
            stubs.remove(pluginId)
            
            val serialized = json.encodeToString(stubs)
            stubsPref.set(serialized)
        } catch (e: Exception) {
            Log.error("JSPluginStubManager: Failed to remove stub for $pluginId", e)
        }
    }
    
    /**
     * Mark a plugin as high priority (loads first).
     */
    fun setPriorityPlugin(pluginId: String, isPriority: Boolean) {
        try {
            val priorities = getPriorityPlugins().toMutableSet()
            if (isPriority) {
                priorities.add(pluginId)
            } else {
                priorities.remove(pluginId)
            }
            
            val serialized = json.encodeToString(priorities.toList())
            priorityPluginsPref.set(serialized)
        } catch (e: Exception) {
            Log.error("JSPluginStubManager: Failed to set priority for $pluginId", e)
        }
    }
    
    /**
     * Get list of high priority plugin IDs.
     */
    fun getPriorityPlugins(): Set<String> {
        return try {
            val serialized = priorityPluginsPref.get()
            if (serialized.isNotEmpty()) {
                json.decodeFromString<List<String>>(serialized).toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.error("JSPluginStubManager: Failed to load priority plugins", e)
            emptySet()
        }
    }
    
    /**
     * Clear all stubs (useful for debugging or reset).
     */
    fun clearAllStubs() {
        stubsPref.delete()
        priorityPluginsPref.delete()
    }
}

/**
 * Stub data for a JS plugin.
 */
@Serializable
data class PluginStubData(
    val metadata: PluginMetadata,
    val fileName: String,
    val lastLoaded: Long
)
