package ireader.domain.js.examples

import ireader.core.source.model.Filter
import ireader.domain.js.util.JSFilterConverter

/**
 * Example demonstrating LNReader to IReader filter conversion.
 */
fun main() {
    val converter = JSFilterConverter()
    
    // Example 1: LNReader filter definition from JavaScript plugin
    val lnReaderFilters = mapOf(
        "status" to mapOf(
            "type" to "Picker",
            "label" to "Status",
            "options" to listOf(
                mapOf("label" to "All", "value" to "all"),
                mapOf("label" to "Ongoing", "value" to "ongoing"),
                mapOf("label" to "Completed", "value" to "completed")
            ),
            "defaultValue" to "all"
        ),
        "author" to mapOf(
            "type" to "TextInput",
            "label" to "Author Name",
            "defaultValue" to ""
        ),
        "genres" to mapOf(
            "type" to "CheckboxGroup",
            "label" to "Genres",
            "options" to listOf(
                mapOf("label" to "Action", "value" to "action"),
                mapOf("label" to "Romance", "value" to "romance"),
                mapOf("label" to "Fantasy", "value" to "fantasy")
            ),
            "defaultValues" to emptyList<String>()
        )
    )
    
    // Convert to IReader filters
    val iReaderFilters = converter.convertToIReaderFilters(lnReaderFilters)
    
    println("Converted ${iReaderFilters.size} filters:")
    iReaderFilters.forEach { filter ->
        when (filter) {
            is Filter.Select -> {
                println("  - Select: ${filter.name}")
                println("    Options: ${filter.options.joinToString(", ")}")
                println("    Selected: ${filter.getSelectedOption()}")
            }
            is Filter.Text -> {
                println("  - Text: ${filter.name}")
                println("    Value: '${filter.value}'")
            }
            is Filter.Group -> {
                println("  - Group: ${filter.name}")
                println("    Filters: ${filter.filters.size}")
            }
            else -> {
                println("  - ${filter.name}")
            }
        }
    }
    
    // Example 2: User modifies filters in UI
    val modifiedFilters = iReaderFilters.toMutableList()
    
    // User selects "Ongoing" status
    val statusFilter = modifiedFilters.find { it.name == "Status" } as? Filter.Select
    statusFilter?.value = 1 // Index of "Ongoing"
    
    // User enters author name
    val authorFilter = modifiedFilters.find { it.name == "Author Name" } as? Filter.Text
    authorFilter?.value = "John Doe"
    
    // User selects genres
    val genresFilter = modifiedFilters.find { it.name == "Genres" } as? Filter.Group
    genresFilter?.filters?.forEach { checkbox ->
        if (checkbox is Filter.Check && checkbox.name == "Action") {
            checkbox.value = true
        }
    }
    
    // Convert back to LNReader format for plugin call
    val filterValues = converter.convertIReaderFiltersToJS(modifiedFilters)
    
    println("\nFilter values for plugin:")
    filterValues.forEach { (key, value) ->
        println("  $key: $value")
    }
    
    // Example 3: Excludable checkbox group (tri-state)
    val advancedFilters = mapOf(
        "genresAdvanced" to mapOf(
            "type" to "ExcludableCheckboxGroup",
            "label" to "Advanced Genres",
            "options" to listOf(
                mapOf("label" to "Action", "value" to "action"),
                mapOf("label" to "Romance", "value" to "romance"),
                mapOf("label" to "Horror", "value" to "horror")
            ),
            "included" to listOf("action"),
            "excluded" to listOf("horror")
        )
    )
    
    val advancedIReaderFilters = converter.convertToIReaderFilters(advancedFilters)
    
    println("\nAdvanced filter (tri-state):")
    val advancedGroup = advancedIReaderFilters.first() as Filter.Group
    advancedGroup.filters.forEach { checkbox ->
        if (checkbox is Filter.Check) {
            val state = when (checkbox.value) {
                true -> "INCLUDED"
                false -> "EXCLUDED"
                null -> "NONE"
            }
            println("  ${checkbox.name}: $state")
        }
    }
    
    // Example 4: Sort filter
    val sortFilters = mapOf(
        "sort" to mapOf(
            "type" to "Sort",
            "label" to "Sort By",
            "options" to listOf(
                mapOf("label" to "Latest", "value" to "latest"),
                mapOf("label" to "Popular", "value" to "popular"),
                mapOf("label" to "Rating", "value" to "rating")
            ),
            "defaultValue" to mapOf(
                "index" to 1,
                "ascending" to false
            )
        )
    )
    
    val sortIReaderFilters = converter.convertToIReaderFilters(sortFilters)
    val sortFilter = sortIReaderFilters.first() as Filter.Sort
    
    println("\nSort filter:")
    println("  Options: ${sortFilter.options.joinToString(", ")}")
    println("  Selected: ${sortFilter.options[sortFilter.value?.index ?: 0]}")
    println("  Ascending: ${sortFilter.value?.ascending}")
}

/**
 * Example of using filters in a plugin source.
 */
class ExamplePluginSource {
    private val converter = JSFilterConverter()
    private var cachedFilters: List<Filter<*>>? = null
    
    fun getFilters(): List<Filter<*>> {
        // Return cached filters if available
        if (cachedFilters != null) {
            return cachedFilters!!
        }
        
        // Get filters from plugin
        val jsFilters = getPluginFilters()
        
        // Convert to IReader filters
        val iReaderFilters = converter.convertToIReaderFilters(jsFilters)
        
        // Add search filter at the beginning
        cachedFilters = listOf(Filter.Title()) + iReaderFilters
        
        return cachedFilters!!
    }
    
    suspend fun getMangaList(filters: List<Filter<*>>, page: Int): List<Novel> {
        // Check for search query
        val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
        
        if (query.isNotBlank()) {
            // Use search
            return searchNovels(query, page)
        }
        
        // Convert filters to LNReader format
        val filterValues = converter.convertIReaderFiltersToJS(filters)
        
        // Call plugin with filters
        return getPopularNovelsWithFilters(page, filterValues)
    }
    
    // Mock methods
    private fun getPluginFilters(): Map<String, Any> = emptyMap()
    private suspend fun searchNovels(query: String, page: Int): List<Novel> = emptyList()
    private suspend fun getPopularNovelsWithFilters(page: Int, filters: Map<String, Any>): List<Novel> = emptyList()
}

data class Novel(val name: String, val url: String)
