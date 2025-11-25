package ireader.domain.js.bridge

import ireader.core.log.Log
import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.SourceHelpers
import ireader.core.source.model.*
import ireader.domain.js.models.PluginMetadata
import ireader.domain.js.util.JSFilterConverter

/**
 * Wrapper that adapts a Zipline LNReaderPlugin to IReader's HttpSource interface.
 * This bridges the JavaScript plugin with the native Kotlin source system.
 */
class JSPluginSource(
    private val plugin: LNReaderPlugin,
    val metadata: PluginMetadata,
    dependencies: Dependencies
) : HttpSource(dependencies) {
    
    override val name: String = metadata.name
    override val lang: String = metadata.lang
    override val baseUrl: String = metadata.site.takeIf { 
        it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://"))
    } ?: ""
    class LatestListing() : Listing(name = "Latest")
    class PopularListing() : Listing(name = "Popular")
    
    private val filterConverter = JSFilterConverter()
    private var cachedFilters: FilterList? = null

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        Log.info("JSPluginSource: [$name] getMangaList(sort) called - sort=$sort, page=$page")
        
        return try {
            // Map IReader's Listing to LNReader plugin methods
            val novels = when (sort) {
                is PopularListing -> {
                    Log.debug("JSPluginSource: Calling plugin.popularNovels($page)")
                    plugin.popularNovels(page)
                }
                is LatestListing -> {
                    Log.debug("JSPluginSource: Calling plugin.latestNovels($page)")
                    plugin.latestNovels(page)
                }
                else -> {
                    // Default to popular if no listing specified
                    Log.debug("JSPluginSource: No listing specified, defaulting to popularNovels($page)")
                    plugin.popularNovels(page)
                }
            }
            
            Log.info("JSPluginSource: Got ${novels.size} novels from plugin")
            if (novels.isEmpty()) {
                Log.warn("JSPluginSource: Plugin returned empty list!")
            } else {
                Log.debug("JSPluginSource: First novel: ${novels.first().name}")
            }
            
            MangasPageInfo(
                mangas = novels.map { it.toMangaInfo() },
                hasNextPage = novels.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getMangaList(sort): ${e.message}", e)
            e.printStackTrace()
            MangasPageInfo(emptyList(), false)
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            // Check if there's a search query in filters
            val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
            

            
            val novels = if (query.isNotBlank()) {
                plugin.searchNovels(query, page)
            } else if (filters.size > 1 || (filters.size == 1 && filters.first() !is Filter.Title)) {
                val jsFilters = filterConverter.convertIReaderFiltersToJS(filters)
                plugin.popularNovels(page, jsFilters)
            } else {
                plugin.popularNovels(page)
            }
            
            Log.info("JSPluginSource: Got ${novels.size} novels from plugin")
            if (novels.isEmpty()) {
                Log.warn("JSPluginSource: Plugin returned empty list!")
            } else {
                Log.debug("JSPluginSource: First novel: ${novels.first().name}")
            }
            
            MangasPageInfo(
                mangas = novels.map { it.toMangaInfo() },
                hasNextPage = novels.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getMangaList(filters): ${e.message}", e)
            e.printStackTrace()
            MangasPageInfo(emptyList(), false)
        }
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return try {
            Log.info("JSPluginSource: [$name] getMangaDetails called for ${manga.key}")
            
            val details = plugin.getNovelDetails(manga.key)
            
            // FIXED: Ensure cover URL is absolute
            val absoluteCover = if (details.cover.isNotBlank()) {
                SourceHelpers.buildAbsoluteUrl(baseUrl, details.cover)
            } else {
                ""
            }
            
            manga.copy(
                title = details.name,
                cover = absoluteCover,
                author = details.author ?: "",
                description = details.description ?: "",
                genres = details.genres,
                status = parseStatus(details.status)
            )
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getMangaDetails", e)
            manga
        }
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            Log.info("JSPluginSource: [$name] getChapterList called for ${manga.key}")
            
            val chapters = plugin.getChapters(manga.key)
            
            Log.info("JSPluginSource: [$name] Got ${chapters.size} chapters from plugin")
            
            // FIXED: Ensure chapter URLs are absolute by prepending baseUrl if needed
            chapters.mapIndexed { index, chapter ->
                ChapterInfo(
                    key = SourceHelpers.buildAbsoluteUrl(baseUrl, chapter.url),
                    name = chapter.name,
                    number = (index + 1).toFloat(),
                    dateUpload = parseDate(chapter.releaseTime)
                )
            }
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getChapterList", e)
            emptyList()
        }
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return try {
            Log.info("JSPluginSource: [$name] getPageList called for ${chapter.key}")
            
            val content = plugin.getChapterContent(chapter.key)
            
            if (content.isBlank()) {
                Log.warn("JSPluginSource: Empty content returned for ${chapter.key}")
                return emptyList()
            }
            
            Log.info("JSPluginSource: Got ${content.length} chars of content")
            
            // Parse HTML and extract text paragraphs
            val pages = parseHtmlToPages(content)
            
            if (pages.isEmpty()) {
                Log.warn("JSPluginSource: No paragraphs extracted from HTML")
                // Fallback to single page with raw content
                return listOf(Text(content))
            }
            
            Log.info("JSPluginSource: Extracted ${pages.size} paragraphs")
            pages
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getPageList", e)
            emptyList()
        }
    }
    
    /**
     * Parse HTML content and extract readable text paragraphs.
     */
    private fun parseHtmlToPages(html: String): List<Page> {
        return try {
            val doc = org.jsoup.Jsoup.parse(html)
            
            // Remove script and style elements
            doc.select("script, style").remove()
            
            // Extract text from paragraph-like elements
            val paragraphs = mutableListOf<String>()
            
            // Try common content selectors first
            val contentElements = doc.select("p, div.chapter-content p, div.text p, div.content p")
            
            if (contentElements.isNotEmpty()) {
                contentElements.forEach { element ->
                    val text = element.text().trim()
                    if (text.isNotEmpty() && text.length > 10) { // Filter out very short text
                        paragraphs.add(text)
                    }
                }
            } else {
                // Fallback: split by line breaks
                val text = doc.body().text()
                text.split("\n").forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && trimmed.length > 10) {
                        paragraphs.add(trimmed)
                    }
                }
            }
            
            // Convert paragraphs to Page objects
            paragraphs.map { Text(it) }
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error parsing HTML: ${e.message}", e)
            emptyList()
        }
    }
    
    override fun getFilters(): FilterList {
        // Return cached filters if available
        if (cachedFilters != null) {
            return cachedFilters!!
        }
        
        return try {
            // Try to get filters from plugin
            val jsFilters = plugin.getFilters()
            
            if (jsFilters.isNotEmpty()) {
                // Convert LNReader filters to IReader filters
                val convertedFilters = filterConverter.convertToIReaderFilters(jsFilters)
                
                // Add search filter at the beginning
                cachedFilters = listOf(Filter.Title()) + convertedFilters
                
                cachedFilters!!
            } else {
                // No filters available, return just search
                listOf(Filter.Title())
            }
        } catch (e: Exception) {
            Log.warn("JSPluginSource: [$name] Failed to load filters: ${e.message}")
            // Fallback to search filter only
            listOf(Filter.Title())
        }
    }
    
    override fun getListings(): List<Listing> {
        // Return available listing types that map to LNReader plugin methods
        return listOf(
            PopularListing(),  // Maps to plugin.popularNovels()
            LatestListing()    // Maps to plugin.latestNovels()
        )
    }
    
    /**
     * Convert plugin novel to MangaInfo
     * FIXED: Ensure URLs are absolute by prepending baseUrl if needed
     */
    private fun PluginNovel.toMangaInfo(): MangaInfo {
        return MangaInfo(
            key = SourceHelpers.buildAbsoluteUrl(baseUrl, this.url),
            title = this.name,
            cover = if (this.cover.isNotBlank()) SourceHelpers.buildAbsoluteUrl(baseUrl, this.cover) else ""
        )
    }
    
    /**
     * Parse status string to status code
     */
    private fun parseStatus(statusText: String?): Long {
        if (statusText == null) return MangaInfo.UNKNOWN
        
        return when {
            statusText.contains("ongoing", ignoreCase = true) -> MangaInfo.ONGOING
            statusText.contains("completed", ignoreCase = true) -> MangaInfo.COMPLETED
            statusText.contains("hiatus", ignoreCase = true) -> MangaInfo.ON_HIATUS
            statusText.contains("cancelled", ignoreCase = true) -> MangaInfo.CANCELLED
            else -> MangaInfo.UNKNOWN
        }
    }
    
    /**
     * Parse date string to timestamp
     */
    private fun parseDate(dateText: String?): Long {
        if (dateText == null) return 0L
        
        // TODO: Implement proper date parsing
        // For now, return current time
        return System.currentTimeMillis()
    }
}
