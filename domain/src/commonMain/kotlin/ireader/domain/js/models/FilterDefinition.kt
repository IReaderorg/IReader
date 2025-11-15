package ireader.domain.js.models

/**
 * Sealed class representing different types of filter definitions from JavaScript plugins.
 * Filters allow users to refine browse/search results.
 */
sealed class FilterDefinition {
    
    /**
     * Picker filter - dropdown selection from predefined options.
     */
    data class Picker(
        val label: String,
        val options: List<FilterOption>,
        val defaultValue: String
    ) : FilterDefinition()
    
    /**
     * Text input filter - free-text entry.
     */
    data class TextInput(
        val label: String,
        val defaultValue: String
    ) : FilterDefinition()
    
    /**
     * Checkbox group filter - multiple selection from options.
     */
    data class CheckboxGroup(
        val label: String,
        val options: List<FilterOption>,
        val defaultValues: List<String>
    ) : FilterDefinition()
    
    /**
     * Excludable checkbox group filter - tri-state selection (included/excluded/none).
     */
    data class ExcludableCheckboxGroup(
        val label: String,
        val options: List<FilterOption>,
        val included: List<String>,
        val excluded: List<String>
    ) : FilterDefinition()
}

/**
 * Represents a single option in a filter.
 */
data class FilterOption(
    val label: String,
    val value: String
)
