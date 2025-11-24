package ireader.domain.js.util

import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.domain.js.models.FilterDefinition
import ireader.domain.js.models.FilterOption

/**
 * Converter for JavaScript plugin filter definitions.
 * Translates between JavaScript filter format and Kotlin FilterDefinition objects,
 * and converts LNReader filters to IReader Filter objects.
 */
class JSFilterConverter {
    
    companion object {
        // Filter type constants from JavaScript
        private const val TYPE_PICKER = "Picker"
        private const val TYPE_TEXT_INPUT = "TextInput"
        private const val TYPE_TEXT = "Text"
        private const val TYPE_CHECKBOX_GROUP = "CheckboxGroup"
        private const val TYPE_EXCLUDABLE_CHECKBOX_GROUP = "ExcludableCheckboxGroup"
        private const val TYPE_SWITCH = "Switch"
        private const val TYPE_CHECKBOX = "Checkbox"
        private const val TYPE_EXCLUDABLE_CHECKBOX = "ExcludableCheckbox"
        private const val TYPE_XCHECBOX = "XCheckbox"
        private const val TYPE_TRISTATE = "TriState"
        private const val TYPE_SORT = "Sort"
        private const val TYPE_TITLE = "Title"
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
                TYPE_TEXT_INPUT, TYPE_TEXT -> convertTextInput(label, filterMap)
                TYPE_CHECKBOX_GROUP -> convertCheckboxGroup(label, filterMap)
                TYPE_EXCLUDABLE_CHECKBOX_GROUP, TYPE_XCHECBOX -> convertExcludableCheckboxGroup(label, filterMap)
                else -> null
            }
            
            if (filterDef != null) {
                result[key] = filterDef
            }
        }
        
        return result
    }
    
    /**
     * Converts LNReader filter definitions to IReader Filter objects.
     * This allows JavaScript plugins to work with IReader's native filter system.
     * 
     * @param jsFilters Map of filter definitions from JavaScript plugin
     * @return FilterList containing IReader Filter objects
     */
    fun convertToIReaderFilters(jsFilters: Map<String, Any>): FilterList {
        val filters = mutableListOf<Filter<*>>()
        
        for ((key, value) in jsFilters) {
            if (value !is Map<*, *>) continue
            
            @Suppress("UNCHECKED_CAST")
            val filterMap = value as Map<String, Any>
            
            val type = filterMap["type"] as? String ?: continue
            val label = filterMap["label"] as? String ?: key
            
            val filter = when (type) {
                TYPE_PICKER -> convertPickerToIReader(label, filterMap)
                TYPE_TEXT_INPUT, TYPE_TEXT -> convertTextToIReader(label, filterMap)
                TYPE_TITLE -> Filter.Title(label)
                TYPE_SWITCH, TYPE_CHECKBOX -> convertCheckboxToIReader(label, filterMap)
                TYPE_CHECKBOX_GROUP -> convertCheckboxGroupToIReader(label, filterMap)
                TYPE_EXCLUDABLE_CHECKBOX, TYPE_EXCLUDABLE_CHECKBOX_GROUP, TYPE_XCHECBOX -> 
                    convertExcludableCheckboxToIReader(label, filterMap)
                TYPE_TRISTATE -> convertTriStateToIReader(label, filterMap)
                TYPE_SORT -> convertSortToIReader(label, filterMap)
                else -> null
            }
            
            if (filter != null) {
                filters.add(filter)
            }
        }
        
        return filters
    }
    
    /**
     * Converts IReader filter values back to LNReader format for plugin calls.
     * 
     * @param filters FilterList from IReader
     * @return Map of filter values in LNReader JavaScript format
     */
    fun convertIReaderFiltersToJS(filters: FilterList): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        for (filter in filters) {
            val value = when (filter) {
                is Filter.Text -> filter.value
                is Filter.Select -> filter.getSelectedOption() ?: ""
                is Filter.Check -> when (filter.value) {
                    true -> if (filter.allowsExclusion) "included" else true
                    false -> if (filter.allowsExclusion) "excluded" else false
                    null -> "none"
                }
                is Filter.Sort -> filter.value?.let { selection ->
                    mapOf(
                        "index" to selection.index,
                        "ascending" to selection.ascending
                    )
                } ?: mapOf("index" to 0, "ascending" to true)
                is Filter.Group -> {
                    // Recursively convert group filters
                    convertIReaderFiltersToJS(filter.filters)
                }
                else -> continue
            }
            
            result[filter.name] = value
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
    
    // IReader Filter conversion methods
    
    private fun convertPickerToIReader(label: String, filterMap: Map<String, Any>): Filter.Select? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        val options = optionsList.mapNotNull { it["label"] }.toTypedArray()
        
        if (options.isEmpty()) return null
        
        val defaultValue = filterMap["defaultValue"] as? String
        val defaultIndex = if (defaultValue != null) {
            optionsList.indexOfFirst { it["value"] == defaultValue }.takeIf { it >= 0 } ?: 0
        } else {
            0
        }
        
        return Filter.Select(label, options, defaultIndex)
    }
    
    private fun convertTextToIReader(label: String, filterMap: Map<String, Any>): Filter.Text {
        val defaultValue = filterMap["defaultValue"] as? String ?: ""
        return Filter.Text(label, defaultValue)
    }
    
    private fun convertCheckboxToIReader(label: String, filterMap: Map<String, Any>): Filter.Check {
        val defaultValue = filterMap["defaultValue"] as? Boolean
        val allowsExclusion = filterMap["allowsExclusion"] as? Boolean ?: false
        return Filter.Check(label, allowsExclusion, defaultValue)
    }
    
    private fun convertCheckboxGroupToIReader(label: String, filterMap: Map<String, Any>): Filter.Group? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        
        @Suppress("UNCHECKED_CAST")
        val defaultValues = filterMap["defaultValues"] as? List<String> ?: emptyList()
        
        val checkboxes = optionsList.map { optionMap ->
            val optionLabel = optionMap["label"] ?: ""
            val optionValue = optionMap["value"] ?: ""
            val isChecked = defaultValues.contains(optionValue)
            Filter.Check(optionLabel, false, isChecked)
        }
        
        return Filter.Group(label, checkboxes)
    }
    
    private fun convertExcludableCheckboxToIReader(label: String, filterMap: Map<String, Any>): Filter.Group? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        
        @Suppress("UNCHECKED_CAST")
        val included = filterMap["included"] as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val excluded = filterMap["excluded"] as? List<String> ?: emptyList()
        
        val checkboxes = optionsList.map { optionMap ->
            val optionLabel = optionMap["label"] ?: ""
            val optionValue = optionMap["value"] ?: ""
            val state = when {
                included.contains(optionValue) -> true
                excluded.contains(optionValue) -> false
                else -> null
            }
            Filter.Check(optionLabel, true, state)
        }
        
        return Filter.Group(label, checkboxes)
    }
    
    private fun convertTriStateToIReader(label: String, filterMap: Map<String, Any>): Filter.Check {
        val defaultValue = filterMap["defaultValue"] as? Int
        val state = when (defaultValue) {
            1 -> true
            2 -> false
            else -> null
        }
        return Filter.Check(label, true, state)
    }
    
    private fun convertSortToIReader(label: String, filterMap: Map<String, Any>): Filter.Sort? {
        @Suppress("UNCHECKED_CAST")
        val optionsList = filterMap["options"] as? List<Map<String, String>> ?: return null
        val options = optionsList.mapNotNull { it["label"] }.toTypedArray()
        
        if (options.isEmpty()) return null
        
        val defaultValue = filterMap["defaultValue"] as? Map<*, *>
        val selection = if (defaultValue != null) {
            val index = (defaultValue["index"] as? Number)?.toInt() ?: 0
            val ascending = defaultValue["ascending"] as? Boolean ?: true
            Filter.Sort.Selection(index, ascending)
        } else {
            null
        }
        
        return Filter.Sort(label, options, selection)
    }
}
