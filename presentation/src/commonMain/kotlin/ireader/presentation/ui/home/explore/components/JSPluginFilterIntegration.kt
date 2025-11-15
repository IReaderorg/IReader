package ireader.presentation.ui.home.explore.components

import androidx.compose.runtime.*
import ireader.core.source.CatalogSource
import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.util.JSFilterConverter

/**
 * Helper for integrating JavaScript plugin filters into the browse screen.
 * TODO: Implement when CatalogSource API is stable
 */
object JSPluginFilterIntegration {
    
    /**
     * Checks if a source is a JavaScript plugin source.
     * TODO: Fix when CatalogSource.source property is available
     */
    fun isJSPluginSource(catalogSource: CatalogSource): Boolean {
        // TODO: catalogSource doesn't have a source property yet
        return false
        // val innerSource = catalogSource.source
        // return innerSource is JSPluginSource
    }
    
    /**
     * Gets filter definitions from a JavaScript plugin source.
     * TODO: Fix when CatalogSource.source property is available
     */
    suspend fun getFilterDefinitions(catalogSource: CatalogSource): Map<String, FilterDefinition>? {
        // TODO: catalogSource doesn't have a source property yet
        return null
        // val innerSource = catalogSource.source
        // val jsSource = innerSource as? JSPluginSource ?: return null
        // return try {
        //     jsSource.getFilters()
        // } catch (e: Exception) {
        //     null
        // }
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
    source: CatalogSource
): JSPluginFilterState {
    val filterState = remember(source) {
        JSPluginFilterState(source)
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
    private val source: CatalogSource
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
            
            // Initialize filter values with defaults
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
            }
        } finally {
            isLoading = false
        }
    }
    
    fun updateFilterValue(key: String, value: Any) {
        filterValues = filterValues + (key to value)
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
        }
    }
}
