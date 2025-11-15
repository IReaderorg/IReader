package ireader.domain.js.util

import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.models.FilterOption

/**
 * Converter for JavaScript plugin filter definitions.
 * Translates between JavaScript filter format and Kotlin FilterDefinition objects.
 */
class JSFilterConverter {
    
    companion object {
        // Filter type constants from JavaScript
        private const val TYPE_PICKER = "Picker"
        private const val TYPE_TEXT_INPUT = "TextInput"
        private const val TYPE_CHECKBOX_GROUP = "CheckboxGroup"
        private const val TYPE_EXCLUDABLE_CHECKBOX_GROUP = "ExcludableCheckboxGroup"
    }
    
    /**
     * Converts JavaScript filter definitions to Kotlin FilterDefinition objects.
     * @param jsFilters Map of filter definitions from JavaScript plugin
     * @return Map of filter key to FilterDefinition
     */
    fun convertFilters(jsFilters: Map<String, Any>): Map<String, FilterDefinition> {
        val result = mutableMapOf<String, FilterDefinition>()
        
        for ((key, value) in jsFilters) {
            if (value !is Map<*, *>) continue
            
            @Suppress("UNCHECKED_CAST")
            val filterMap = value as Map<String, Any>
            
            val type = filterMap["type"] as? String ?: continue
            val label = filterMap["label"] as? String ?: key
            
            val filterDef = when (type) {
                TYPE_PICKER -> convertPicker(label, filterMap)
                TYPE_TEXT_INPUT -> convertTextInput(label, filterMap)
                TYPE_CHECKBOX_GROUP -> convertCheckboxGroup(label, filterMap)
                TYPE_EXCLUDABLE_CHECKBOX_GROUP -> convertExcludableCheckboxGroup(label, filterMap)
                else -> null
            }
            
            if (filterDef != null) {
                result[key] = filterDef
            }
        }
        
        return result
    }
    
    /**
     * Converts UI filter values back to JavaScript format for plugin calls.
     * @param filterDefinitions The filter definitions
     * @param userValues The user-selected values from UI
     * @return Map of filter values in JavaScript format
     */
    fun convertFilterValues(
        filterDefinitions: Map<String, FilterDefinition>,
        userValues: Map<String, Any>
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        for ((key, definition) in filterDefinitions) {
            val userValue = userValues[key]
            
            val jsValue = when (definition) {
                is FilterDefinition.Picker -> {
                    // Picker returns single string value
                    userValue as? String ?: definition.defaultValue
                }
                is FilterDefinition.TextInput -> {
                    // TextInput returns string value
                    userValue as? String ?: definition.defaultValue
                }
                is FilterDefinition.CheckboxGroup -> {
                    // CheckboxGroup returns List<String>
                    @Suppress("UNCHECKED_CAST")
                    userValue as? List<String> ?: definition.defaultValues
                }
                is FilterDefinition.ExcludableCheckboxGroup -> {
                    // ExcludableCheckboxGroup returns Map with "included" and "excluded" keys
                    @Suppress("UNCHECKED_CAST")
                    val valueMap = userValue as? Map<String, List<String>>
                    if (valueMap != null) {
                        mapOf(
                            "included" to (valueMap["included"] ?: emptyList<String>()),
                            "excluded" to (valueMap["excluded"] ?: emptyList<String>())
                        )
                    } else {
                        mapOf(
                            "included" to definition.included,
                            "excluded" to definition.excluded
                        )
                    }
                }
            }
            
            result[key] = jsValue
        }
        
        return result
    }
    
    private fun convertPicker(label: String, filterMap: Map<String, Any>): FilterDefinition.Picker? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        val options = optionsList.mapNotNull { optionMap ->
            val optionLabel = optionMap["label"] ?: return@mapNotNull null
            val optionValue = optionMap["value"] ?: return@mapNotNull null
            FilterOption(optionLabel, optionValue)
        }
        
        if (options.isEmpty()) return null
        
        val defaultValue = filterMap["defaultValue"] as? String ?: options.firstOrNull()?.value ?: ""
        
        return FilterDefinition.Picker(label, options, defaultValue)
    }
    
    private fun convertTextInput(label: String, filterMap: Map<String, Any>): FilterDefinition.TextInput {
        val defaultValue = filterMap["defaultValue"] as? String ?: ""
        return FilterDefinition.TextInput(label, defaultValue)
    }
    
    private fun convertCheckboxGroup(label: String, filterMap: Map<String, Any>): FilterDefinition.CheckboxGroup? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        val options = optionsList.mapNotNull { optionMap ->
            val optionLabel = optionMap["label"] ?: return@mapNotNull null
            val optionValue = optionMap["value"] ?: return@mapNotNull null
            FilterOption(optionLabel, optionValue)
        }
        
        if (options.isEmpty()) return null
        
        @Suppress("UNCHECKED_CAST")
        val defaultValues = filterMap["defaultValues"] as? List<String> ?: emptyList()
        
        return FilterDefinition.CheckboxGroup(label, options, defaultValues)
    }
    
    private fun convertExcludableCheckboxGroup(
        label: String,
        filterMap: Map<String, Any>
    ): FilterDefinition.ExcludableCheckboxGroup? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        val options = optionsList.mapNotNull { optionMap ->
            val optionLabel = optionMap["label"] ?: return@mapNotNull null
            val optionValue = optionMap["value"] ?: return@mapNotNull null
            FilterOption(optionLabel, optionValue)
        }
        
        if (options.isEmpty()) return null
        
        @Suppress("UNCHECKED_CAST")
        val included = filterMap["included"] as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val excluded = filterMap["excluded"] as? List<String> ?: emptyList()
        
        return FilterDefinition.ExcludableCheckboxGroup(label, options, included, excluded)
    }
}
