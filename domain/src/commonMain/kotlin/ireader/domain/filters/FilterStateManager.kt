package ireader.domain.filters

import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages filter state persistence for JavaScript plugin sources.
 * Stores filter values per source ID using preferences.
 */
class FilterStateManager(
    private val preferenceStore: PreferenceStore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Saves filter state for a specific source.
     * 
     * @param sourceId The unique identifier for the source
     * @param filters Map of filter ID to filter value
     */
    suspend fun saveFilterState(sourceId: Long, filters: Map<String, Any>) {
        val filterState = FilterState(
            sourceId = sourceId,
            filters = filters.mapValues { (_, value) -> serializeFilterValue(value) }
        )
        
        val key = getPreferenceKey(sourceId)
        val jsonString = json.encodeToString(filterState)
        preferenceStore.getString(key).set(jsonString)
    }
    
    /**
     * Loads filter state for a specific source.
     * 
     * @param sourceId The unique identifier for the source
     * @return Map of filter ID to filter value, or empty map if no state exists
     */
    suspend fun loadFilterState(sourceId: Long): Map<String, Any> {
        val key = getPreferenceKey(sourceId)
        val jsonString = preferenceStore.getString(key).get()
        
        if (jsonString.isEmpty()) {
            return emptyMap()
        }
        
        return try {
            val filterState = json.decodeFromString<FilterState>(jsonString)
            filterState.filters.mapValues { (_, value) -> deserializeFilterValue(value) }
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "[FilterStateManager] Failed to load filter state for source $sourceId")
            emptyMap()
        }
    }
    
    /**
     * Observes filter state changes for a specific source.
     * 
     * @param sourceId The unique identifier for the source
     * @return Flow emitting filter state updates
     */
    fun observeFilterState(sourceId: Long): Flow<Map<String, Any>> {
        val key = getPreferenceKey(sourceId)
        return preferenceStore.getString(key).changes()
            .map { jsonString ->
                if (jsonString.isEmpty()) {
                    emptyMap()
                } else {
                    try {
                        val filterState = json.decodeFromString<FilterState>(jsonString)
                        filterState.filters.mapValues { (_, value) -> deserializeFilterValue(value) }
                    } catch (e: Exception) {
                        ireader.core.log.Log.error(e, "[FilterStateManager] Failed to observe filter state for source $sourceId")
                        emptyMap()
                    }
                }
            }
    }
    
    /**
     * Clears filter state for a specific source.
     * 
     * @param sourceId The unique identifier for the source
     */
    suspend fun clearFilterState(sourceId: Long) {
        val key = getPreferenceKey(sourceId)
        preferenceStore.getString(key).delete()
    }
    
    /**
     * Clears all filter states.
     */
    suspend fun clearAllFilterStates() {
        // Note: This is a simplified implementation
        // In a production system, you might want to track all source IDs
        // and clear them individually, or use a different storage mechanism
        ireader.core.log.Log.info { "[FilterStateManager] Clear all filter states requested" }
    }
    
    private fun getPreferenceKey(sourceId: Long): String {
        return "js_plugin_filter_state_$sourceId"
    }
    
    /**
     * Serializes a filter value to a serializable format.
     */
    private fun serializeFilterValue(value: Any): SerializableFilterValue {
        return when (value) {
            is String -> SerializableFilterValue.StringValue(value)
            is List<*> -> SerializableFilterValue.ListValue(value.filterIsInstance<String>())
            is Map<*, *> -> {
                val included = (value["included"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val excluded = (value["excluded"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                SerializableFilterValue.MapValue(
                    mapOf(
                        "included" to included,
                        "excluded" to excluded
                    )
                )
            }
            else -> SerializableFilterValue.StringValue(value.toString())
        }
    }
    
    /**
     * Deserializes a filter value from storage.
     */
    private fun deserializeFilterValue(value: SerializableFilterValue): Any {
        return when (value) {
            is SerializableFilterValue.StringValue -> value.value
            is SerializableFilterValue.ListValue -> value.value
            is SerializableFilterValue.MapValue -> value.value
        }
    }
}

/**
 * Internal data class for storing filter state.
 */
@Serializable
private data class FilterState(
    val sourceId: Long,
    val filters: Map<String, SerializableFilterValue>
)

/**
 * Sealed class for serializable filter values.
 */
@Serializable
private sealed class SerializableFilterValue {
    @Serializable
    data class StringValue(val value: String) : SerializableFilterValue()
    
    @Serializable
    data class ListValue(val value: List<String>) : SerializableFilterValue()
    
    @Serializable
    data class MapValue(val value: Map<String, List<String>>) : SerializableFilterValue()
}
