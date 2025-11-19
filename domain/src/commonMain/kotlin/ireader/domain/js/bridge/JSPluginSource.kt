package ireader.domain.js.bridge

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.PluginMetadata
import kotlinx.coroutines.runBlocking

/**
 * Implementation of IReader's HttpSource interface using a JavaScript plugin.
 * Delegates all operations to the JSPluginBridge.
 */
class JSPluginSource(
    private val bridge: JSPluginBridge,
    private val metadata: PluginMetadata,
    override val id: Long,
    override val name: String,
    override val lang: String,
    private val dependencies: ireader.core.source.Dependencies
) : ireader.core.source.HttpSource(dependencies) {
    
    /**
     * Base URL from the plugin metadata.
     * This allows opening book details in webview.
     */
    override val baseUrl: String
        get() = metadata.site
    
    /**
     * Gets detailed information about a manga/novel.
     */
    override suspend fun getMangaDetails(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): MangaInfo {
        return try {
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            sourceNovel.toMangaInfo()
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get manga details")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting manga details")
            throw JSPluginError.ExecutionError(metadata.id, "getMangaDetails", e)
        }
    }
    
    /**
     * Gets the list of chapters for a manga/novel.
     */
    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        return try {
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            sourceNovel.chapters.map { it.toChapterInfo() }
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get chapter list")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting chapter list")
            throw JSPluginError.ExecutionError(metadata.id, "getChapterList", e)
        }
    }
    
    /**
     * Gets the content pages for a chapter.
     * For novel sources, returns a single Text page with HTML content.
     */
    override suspend fun getPageList(
        chapter: ChapterInfo,
        commands: List<Command<*>>
    ): List<Page> {
        return try {
            val chapterPath = chapter.key
            val htmlContent = bridge.parseChapter(chapterPath)
            parseHtmlToParagraphs(htmlContent)
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get page list")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting page list")
            throw JSPluginError.ExecutionError(metadata.id, "getPageList", e)
        }
    }
    
    /**
     * Parses HTML content into a list of Text pages, one per paragraph.
     */
    private fun parseHtmlToParagraphs(html: String): List<Page> {
        if (html.isBlank()) return emptyList()
        
        return try {
            // Use jsoup to parse HTML and extract text from paragraphs
            val doc = org.jsoup.Jsoup.parse(html)
            val paragraphs = doc.select("p, div.paragraph, div.text")
                .mapNotNull { element ->
                    val text = element.text().trim()
                    if (text.isNotEmpty()) Text(text) else null
                }
            
            // If no paragraphs found, try to get all text content
            if (paragraphs.isEmpty()) {
                val allText = doc.text().trim()
                if (allText.isNotEmpty()) {
                    // Split by double newlines or long single newlines
                    val splits = allText.split(Regex("\n\n+|\n{3,}"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .map { Text(it) }
                    
                    if (splits.isNotEmpty()) return splits
                }
                
                // Last resort: return the HTML as-is
                listOf(Text(html))
            } else {
                paragraphs
            }
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to parse HTML to paragraphs")
            // Fallback: return HTML as single page
            listOf(Text(html))
        }
    }
    
    /**
     * Gets popular novels from the source.
     * Custom method for browsing popular content.
     */
    suspend fun getPopularNovels(page: Int, filters: Map<String, Any> = emptyMap()): List<MangaInfo> {
        return try {
            val novels = bridge.popularNovels(page, filters)
            novels.map { it.toMangaInfo() }
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get popular novels")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error getting popular novels")
            throw JSPluginError.ExecutionError(metadata.id, "getPopularNovels", e)
        }
    }
    
    /**
     * Searches for novels in the source.
     * Custom method for searching content.
     */
    suspend fun searchNovels(query: String, page: Int): List<MangaInfo> {
        return try {
            val novels = bridge.searchNovels(query, page)
            novels.map { it.toMangaInfo() }
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to search novels")
            throw e
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Unexpected error searching novels")
            throw JSPluginError.ExecutionError(metadata.id, "searchNovels", e)
        }
    }
    
    /**
     * Gets the filter definitions for this source (internal method).
     */
    suspend fun getPluginFilters(): Map<String, ireader.domain.js.models.FilterDefinition> {
        return try {
            bridge.getFilters()
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get filters")
            emptyMap()
        }
    }
    
    // CatalogSource interface implementation
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return try {
            // Skip if this is a Search listing (search is handled separately via searchNovels)
            if (sort?.name == "Search") {
                return MangasPageInfo(emptyList(), hasNextPage = false)
            }
            
            // Start with default filter values
            val filterValues = getDefaultFilterValues().toMutableMap()
            
            // Set sort based on listing type
            val sortValue = when (sort?.name) {
                "Latest" -> "latest"
                "Popular" -> ""  // Empty for popular/default
                else -> ""
            }
            if (sortValue.isNotEmpty()) {
                filterValues["m_orderby"] = mapOf("value" to sortValue)
            }
            
            val novels = bridge.popularNovels(page, filterValues)
            val mangaList = novels.map { it.toMangaInfo() }
            MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get manga list")
            MangasPageInfo(emptyList(), hasNextPage = false)
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            // Check if there's a search query in the filters
            val searchQuery = filters.filterIsInstance<Filter.Text>()
                .firstOrNull { it.name.equals("query", ignoreCase = true) || it.name.equals("search", ignoreCase = true) }
                ?.value
            
            // If there's a search query, use searchNovels instead
            if (!searchQuery.isNullOrBlank()) {
                val novels = bridge.searchNovels(searchQuery, page)
                val mangaList = novels.map { it.toMangaInfo() }
                return MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
            }
            
            // Start with default filter values and merge with user selections
            val defaultValues = getDefaultFilterValues()
            val userValues = convertFiltersToMap(filters)
            val filterValues = defaultValues.toMutableMap().apply { putAll(userValues) }
            
            val novels = bridge.popularNovels(page, filterValues)
            val mangaList = novels.map { it.toMangaInfo() }
            MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get manga list with filters")
            MangasPageInfo(emptyList(), hasNextPage = false)
        }
    }
    
    /**
     * Converts IReader FilterList to a map of filter values for the JS plugin.
     */
    private fun convertFiltersToMap(filters: FilterList): Map<String, Any> {
        val filterMap = mutableMapOf<String, Any>()
        
        filters.forEach { filter ->
            when (filter) {
                is Filter.Select -> {
                    // Get the selected option
                    val selectedOption = filter.options.getOrNull(filter.value)
                    if (selectedOption != null) {
                        filterMap[filter.name] = mapOf("value" to selectedOption)
                    }
                }
                is Filter.Text -> {
                    if (filter.value.isNotBlank()) {
                        filterMap[filter.name] = mapOf("value" to filter.value)
                    }
                }
                is Filter.Group -> {
                    // Handle checkbox groups
                    val checkedValues = mutableListOf<String>()
                    filter.filters.forEach { subFilter ->
                        if (subFilter is Filter.Check && subFilter.value == true) {
                            checkedValues.add(subFilter.name)
                        }
                    }
                    if (checkedValues.isNotEmpty()) {
                        filterMap[filter.name] = mapOf("value" to checkedValues)
                    }
                }
                is Filter.Check -> {
                    // Handle individual checkboxes
                    if (filter.value == true) {
                        filterMap[filter.name] = mapOf("value" to true)
                    }
                }
                is Filter.Sort -> {
                    // Handle sort filters
                    filter.value?.let { selection ->
                        val sortOption = filter.options.getOrNull(selection.index)
                        if (sortOption != null) {
                            filterMap[filter.name] = mapOf(
                                "value" to sortOption,
                                "ascending" to selection.ascending
                            )
                        }
                    }
                }
                is Filter.Note -> {
                    // Notes don't have values, skip them
                }
            }
        }
        
        return filterMap
    }
    
    /**
     * Gets default filter values from the plugin's filter definitions.
     * Returns a map where each filter has a "value" property with its default value.
     */
    private suspend fun getDefaultFilterValues(): Map<String, Any> {
        return try {
            val filterDefs = bridge.getFilters()
            
            val filterValues = filterDefs.mapValues { (key, def) -> 
                val value = when (def) {
                    is ireader.domain.js.models.FilterDefinition.Picker -> 
                        mapOf("value" to def.defaultValue)
                    is ireader.domain.js.models.FilterDefinition.TextInput -> 
                        mapOf("value" to def.defaultValue)
                    is ireader.domain.js.models.FilterDefinition.CheckboxGroup -> {
                        // Convert to ArrayList to ensure proper JavaScript array conversion
                        val arrayValue = ArrayList(def.defaultValues)
                        mapOf("value" to arrayValue)
                    }
                    is ireader.domain.js.models.FilterDefinition.ExcludableCheckboxGroup -> 
                        mapOf("included" to def.included, "excluded" to def.excluded)
                }
                value
            }

            filterValues
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get default filter values")
            emptyMap()
        }
    }
    
    override fun getListings(): List<Listing> {
        // Return listings including Search to indicate search support
        return listOf(
            object : Listing("Popular") {},
            object : Listing("Latest") {},
            object : Listing("Search") {}  // Add Search listing to indicate search support
        )
    }
    

    override fun getFilters(): FilterList {
        return try {
            // Get JS plugin filters synchronously (cached from plugin load)
            val jsFilters = runBlocking { getPluginFilters() }
            
            // Add search filter at the beginning
            val filters = mutableListOf<Filter<*>>()
            filters.add(Filter.Text("Search", ""))
            
            // Convert JS filters to IReader filters
            filters.addAll(
                jsFilters.map { (key, filterDef) ->
                    convertFilterDefinitionToIReaderFilter(key, filterDef)
                }.filterNotNull()
            )
            
            filters
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to convert filters")
            // Return at least the search filter
            listOf(Filter.Text("Search", ""))
        }
    }
    
    /**
     * Converts a JS FilterDefinition to an IReader Filter.
     */
    private fun convertFilterDefinitionToIReaderFilter(
        key: String,
        filterDef: ireader.domain.js.models.FilterDefinition
    ): Filter<*>? {
        return when (filterDef) {
            is ireader.domain.js.models.FilterDefinition.Picker -> {
                // Convert Picker to Select filter
                val options = filterDef.options.map { it.label }.toTypedArray()
                val defaultIndex = filterDef.options.indexOfFirst { it.value == filterDef.defaultValue }
                    .takeIf { it >= 0 } ?: 0
                Filter.Select(filterDef.label, options, defaultIndex)
            }
            is ireader.domain.js.models.FilterDefinition.TextInput -> {
                // Convert TextInput to Text filter
                Filter.Text(filterDef.label, filterDef.defaultValue)
            }
            is ireader.domain.js.models.FilterDefinition.CheckboxGroup -> {
                // Convert CheckboxGroup to Group of Check filters
                val checkFilters = filterDef.options.map { option ->
                    val isChecked = filterDef.defaultValues.contains(option.value)
                    Filter.Check(option.label, allowsExclusion = false, value = isChecked)
                }
                Filter.Group(filterDef.label, checkFilters)
            }
            is ireader.domain.js.models.FilterDefinition.ExcludableCheckboxGroup -> {
                // Convert ExcludableCheckboxGroup to Group of Check filters with exclusion
                val checkFilters = filterDef.options.map { option ->
                    val value = when {
                        filterDef.included.contains(option.value) -> true
                        filterDef.excluded.contains(option.value) -> false
                        else -> null
                    }
                    Filter.Check(option.label, allowsExclusion = true, value = value)
                }
                Filter.Group(filterDef.label, checkFilters)
            }
        }
    }
    
    override fun getCommands(): CommandList {
        // Return empty command list
        return emptyList()
    }
}
