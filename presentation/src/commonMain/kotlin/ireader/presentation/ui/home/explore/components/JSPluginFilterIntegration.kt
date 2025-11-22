package ireader.presentation.ui.home.explore.components

import androidx.compose.runtime.*
import ireader.core.source.CatalogSource
import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.util.JSFilterConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper for integrating JavaScript plugin filters into the browse screen.
 * TODO: Implement when CatalogSource API is stable
 */
object JSPluginFilterIntegration {
    
    /**
     * Checks if a source is a JavaScript plugin source.
     * 
     * API Status: CatalogSource is an interface, and JSPluginSource is a concrete implementation.
     * We can directly check if the catalogSource instance is a JSPluginSource using Kotlin's `is` operator.
     */
    fun isJSPluginSource(catalogSource: CatalogSource): Boolean {
        return catalogSource is JSPluginSource
    }
    
    /**
     * Gets filter definitions from a JavaScript plugin source.
     * 
     * TODO: Implement plugin filters in Zipline bridge
     * For now, returns null as filters are not yet implemented in the new Zipline approach.
     */
    suspend fun getFilterDefinitions(catalogSource: CatalogSource): Map<String, FilterDefinition>? {
        val jsSource = catalogSource as? JSPluginSource ?: return null
        return try {
            // TODO: Add getPluginFilters() to LNReaderPlugin interface and implement in adapter
            // jsSource.getPluginFilters()
            null // Filters not yet implemented in Zipline
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "[JSPluginFilterIntegration] Failed to get filter definitions")
            null
        }
    }
    
    /**
     * Converts user-selected filter values to JavaScript format.
     */
    fun convertFilterValuesForPlugin(
        filterDefinitions: Map<String, FilterDefinition>,
        userValues: Map<String, Any>
    ): Map<String, Any> {
        val converter = JSFilterConverter()
        return converter.convertFilterValues(filterDefinitions, userValues)
    }
}

/**
 * Composable state holder for JavaScript plugin filters.
 */
@Composable
fun rememberJSPluginFilterState(
    source: CatalogSource,
    filterStateManager: ireader.domain.filters.FilterStateManager? = null
): JSPluginFilterState {
    val filterState = remember(source) {
        JSPluginFilterState(source, filterStateManager)
    }
    
    LaunchedEffect(source) {
        filterState.loadFilters()
    }
    
    return filterState
}

/**
 * State holder for JavaScript plugin filters.
 */
class JSPluginFilterState(
    private val source: CatalogSource,
    private val filterStateManager: ireader.domain.filters.FilterStateManager? = null
) {
    var filterDefinitions by mutableStateOf<Map<String, FilterDefinition>?>(null)
        private set
    
    var filterValues by mutableStateOf<Map<String, Any>>(emptyMap())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    val isJSPluginSource: Boolean
        get() = JSPluginFilterIntegration.isJSPluginSource(source)
    
    suspend fun loadFilters() {
        if (!isJSPluginSource) return
        
        isLoading = true
        try {
            filterDefinitions = JSPluginFilterIntegration.getFilterDefinitions(source)
            
            // Try to load saved filter state first
            val savedState = filterStateManager?.loadFilterState(source.id) ?: emptyMap()
            
            // Initialize filter values with saved state or defaults
            filterDefinitions?.let { definitions ->
                val defaultValues = mutableMapOf<String, Any>()
                definitions.forEach { (key, definition) ->
                    // Use saved value if available, otherwise use default
                    val savedValue = savedState[key]
                    val value = if (savedValue != null) {
                        savedValue
                    } else {
                        when (definition) {
                            is FilterDefinition.Picker -> definition.defaultValue
                            is FilterDefinition.TextInput -> definition.defaultValue
                            is FilterDefinition.CheckboxGroup -> definition.defaultValues
                            is FilterDefinition.ExcludableCheckboxGroup -> mapOf(
                                "included" to definition.included,
                                "excluded" to definition.excluded
                            )
                        }
                    }
                    defaultValues[key] = value
                }
                filterValues = defaultValues
            }
        } finally {
            isLoading = false
        }
    }
    
    fun updateFilterValue(key: String, value: Any) {
        filterValues = filterValues + (key to value)
        // Save filter state when it changes
        CoroutineScope(Dispatchers.Default).launch {
            filterStateManager?.saveFilterState(source.id, filterValues)
        }
    }
    
    fun getConvertedFilters(): Map<String, Any> {
        val definitions = filterDefinitions ?: return emptyMap()
        return JSPluginFilterIntegration.convertFilterValuesForPlugin(definitions, filterValues)
    }
    
    fun resetFilters() {
        filterDefinitions?.let { definitions ->
            val defaultValues = mutableMapOf<String, Any>()
            definitions.forEach { (key, definition) ->
                when (definition) {
                    is FilterDefinition.Picker -> {
                        defaultValues[key] = definition.defaultValue
                    }
                    is FilterDefinition.TextInput -> {
                        defaultValues[key] = definition.defaultValue
                    }
                    is FilterDefinition.CheckboxGroup -> {
                        defaultValues[key] = definition.defaultValues
                    }
                    is FilterDefinition.ExcludableCheckboxGroup -> {
                        defaultValues[key] = mapOf(
                            "included" to definition.included,
                            "excluded" to definition.excluded
                        )
                    }
                }
            }
            filterValues = defaultValues
            // Clear saved filter state
            CoroutineScope(Dispatchers.Default).launch {
                filterStateManager?.clearFilterState(source.id)
            }
        }
    }
}
