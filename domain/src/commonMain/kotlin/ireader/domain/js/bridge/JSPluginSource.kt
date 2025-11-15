package ireader.domain.js.bridge

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.PluginMetadata
import kotlinx.coroutines.runBlocking

/**
 * Implementation of IReader's CatalogSource interface using a JavaScript plugin.
 * Delegates all operations to the JSPluginBridge.
 */
class JSPluginSource(
    private val bridge: JSPluginBridge,
    private val metadata: PluginMetadata,
    override val id: Long,
    override val name: String,
    override val lang: String
) : CatalogSource {
    
    /**
     * Gets detailed information about a manga/novel.
     */
    override suspend fun getMangaDetails(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): MangaInfo {
        return try {
            Log.debug { "[JSPluginSource] Getting manga details for: ${manga.title}" }
            val startTime = System.currentTimeMillis()
            
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            val mangaInfo = sourceNovel.toMangaInfo()
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got manga details in ${duration}ms" }
            
            mangaInfo
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get manga details: ${e.message}")
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
            Log.debug { "[JSPluginSource] Getting chapter list for: ${manga.title}" }
            val startTime = System.currentTimeMillis()
            
            val novelPath = manga.key
            val sourceNovel = bridge.parseNovel(novelPath)
            val chapters = sourceNovel.chapters.map { it.toChapterInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got ${chapters.size} chapters in ${duration}ms" }
            
            chapters
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get chapter list: ${e.message}")
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
            Log.debug { "[JSPluginSource] Getting page list for chapter: ${chapter.name}" }
            val startTime = System.currentTimeMillis()
            
            val chapterPath = chapter.key
            val htmlContent = bridge.parseChapter(chapterPath)
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got chapter content (${htmlContent.length} chars) in ${duration}ms" }
            
            // Parse HTML and create a Text page for each paragraph
            val pages = parseHtmlToParagraphs(htmlContent)
            Log.debug { "[JSPluginSource] Parsed ${pages.size} paragraphs from HTML" }
            
            pages
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get page list: ${e.message}")
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
            Log.debug { "[JSPluginSource] Getting popular novels, page: $page" }
            val startTime = System.currentTimeMillis()
            
            val novels = bridge.popularNovels(page, filters)
            val mangaList = novels.map { it.toMangaInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Got ${mangaList.size} popular novels in ${duration}ms" }
            
            mangaList
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to get popular novels: ${e.message}")
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
            Log.debug { "[JSPluginSource] Searching novels, query: $query, page: $page" }
            val startTime = System.currentTimeMillis()
            
            val novels = bridge.searchNovels(query, page)
            val mangaList = novels.map { it.toMangaInfo() }
            
            val duration = System.currentTimeMillis() - startTime
            Log.info { "[JSPluginSource] Found ${mangaList.size} novels in ${duration}ms" }
            
            mangaList
        } catch (e: JSPluginError) {
            Log.error(e, "[JSPluginSource] Failed to search novels: ${e.message}")
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
            Log.debug { "[JSPluginSource] Getting manga list with listing: ${sort?.name}, page: $page" }
            val filterValues = getDefaultFilterValues().toMutableMap()
            
            // Override sort filter to use "latest" by default
            if (filterValues.containsKey("sort")) {
                filterValues["sort"] = mapOf("value" to "latest")
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
            Log.debug { "[JSPluginSource] Getting manga list with filters, page: $page" }
            val filterValues = getDefaultFilterValues().toMutableMap()
            
            // Override sort filter to use "latest" by default
            if (filterValues.containsKey("sort")) {
                filterValues["sort"] = mapOf("value" to "latest")
            }
            
            val novels = bridge.popularNovels(page, filterValues)
            val mangaList = novels.map { it.toMangaInfo() }
            MangasPageInfo(mangaList, hasNextPage = mangaList.isNotEmpty())
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get manga list with filters")
            MangasPageInfo(emptyList(), hasNextPage = false)
        }
    }
    
    /**
     * Gets default filter values from the plugin's filter definitions.
     * Returns a map where each filter has a "value" property with its default value.
     */
    private suspend fun getDefaultFilterValues(): Map<String, Any> {
        return try {
            val filterDefs = bridge.getFilters()
            Log.debug { "[JSPluginSource] Got ${filterDefs.size} filter definitions" }
            
            val filterValues = filterDefs.mapValues { (key, def) -> 
                val value = when (def) {
                    is ireader.domain.js.models.FilterDefinition.Picker -> 
                        mapOf("value" to def.defaultValue)
                    is ireader.domain.js.models.FilterDefinition.TextInput -> 
                        mapOf("value" to def.defaultValue)
                    is ireader.domain.js.models.FilterDefinition.CheckboxGroup -> {
                        // Convert to ArrayList to ensure proper JavaScript array conversion
                        val arrayValue = ArrayList(def.defaultValues)
                        Log.debug { "[JSPluginSource] CheckboxGroup '$key' defaultValues: $arrayValue (${arrayValue.javaClass.simpleName})" }
                        mapOf("value" to arrayValue)
                    }
                    is ireader.domain.js.models.FilterDefinition.ExcludableCheckboxGroup -> 
                        mapOf("included" to def.included, "excluded" to def.excluded)
                }
                Log.debug { "[JSPluginSource] Filter '$key' default value: $value" }
                value
            }
            
            Log.debug { "[JSPluginSource] Final filter values: $filterValues" }
            filterValues
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to get default filter values")
            emptyMap()
        }
    }
    
    override fun getListings(): List<Listing> {
        // Return a default "Popular" listing
        return listOf(
            object : Listing("Popular") {},
            object : Listing("Latest") {}
        )
    }
    

    override fun getFilters(): FilterList {
        return try {
            // Get JS plugin filters synchronously (cached from plugin load)
            val jsFilters = runBlocking { getPluginFilters() }
            
            // Convert JS filters to IReader filters
            jsFilters.map { (key, filterDef) ->
                convertFilterDefinitionToIReaderFilter(key, filterDef)
            }.filterNotNull()
        } catch (e: Exception) {
            Log.error(e, "[JSPluginSource] Failed to convert filters")
            emptyList()
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
