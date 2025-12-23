package ireader.domain.js.bridge

import ireader.core.log.Log
import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.SourceHelpers
import ireader.core.source.model.*
import ireader.domain.js.models.PluginMetadata
import ireader.domain.js.util.JSFilterConverter
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

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
    
    // Use metadata.site if valid, otherwise will be auto-detected from first URL
    private var _baseUrl: String = metadata.site.takeIf { 
        it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://"))
    } ?: ""
    
    override val baseUrl: String
        get() = _baseUrl
    
    class LatestListing() : Listing(name = "Latest")
    class PopularListing() : Listing(name = "Popular")
    
    private val filterConverter = JSFilterConverter()
    private var cachedFilters: FilterList? = null
    
    /**
     * Extract base URL from an absolute URL.
     * E.g., "https://example.com/novel/123" -> "https://example.com"
     */
    private fun extractBaseUrl(url: String): String? {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return null
        }
        
        val regex = Regex("^(https?://[^/]+)")
        val extracted = regex.find(url)?.groupValues?.get(1)
        
        // Validate that the extracted baseUrl has a proper domain (contains a dot)
        // This prevents invalid URLs like "https://libraryofheader" from being used
        if (extracted != null && !extracted.contains(".") && !extracted.contains("localhost")) {
            Log.warn("JSPluginSource: [$name] Rejected invalid baseUrl: $extracted (no TLD)")
            return null
        }
        
        return extracted
    }
    
    /**
     * Auto-detect and cache base URL from the first absolute URL we encounter.
     */
    private fun autoDetectBaseUrl(url: String) {
        if (_baseUrl.isBlank() && url.startsWith("http")) {
            extractBaseUrl(url)?.let { detected ->
                _baseUrl = detected
                Log.info("JSPluginSource: [$name] Auto-detected baseUrl: $_baseUrl")
            }
        }
    }
    
    /**
     * Convert an absolute URL back to the format the plugin expects.
     * If the URL is absolute and starts with our baseUrl, return just the path.
     * Otherwise, return the URL as-is (it might already be in the right format).
     * 
     * E.g., "https://example.com/novel/123" -> "/novel/123" (if baseUrl is "https://example.com")
     * E.g., "/novel/123" -> "/novel/123" (already relative)
     */
    private fun toPluginUrl(url: String): String {
        // If URL is not absolute, return as-is
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url
        }
        
        // If we have a baseUrl and the URL starts with it, extract the path
        if (_baseUrl.isNotBlank() && url.startsWith(_baseUrl)) {
            return url.substring(_baseUrl.length)
        }
        
        // Otherwise, try to extract path from any absolute URL
        val regex = Regex("^https?://[^/]+(/.*)$")
        return regex.find(url)?.groupValues?.get(1) ?: url
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        Log.info("JSPluginSource: [$name] getMangaList(sort) called - sort=$sort, page=$page")
        
        return try {
            // Check for cancellation before starting
            currentCoroutineContext().ensureActive()
            
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
            
            // Check for cancellation after fetch
            currentCoroutineContext().ensureActive()
            
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
        } catch (e: CancellationException) {
            // Re-throw cancellation exceptions - don't treat them as errors
            throw e
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getMangaList(sort): ${e.message}", e)
            e.printStackTrace()
            MangasPageInfo(emptyList(), false)
        }
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        return try {
            // Check for cancellation before starting
            currentCoroutineContext().ensureActive()
            
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
            
            // Check for cancellation after fetch
            currentCoroutineContext().ensureActive()
            
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
        } catch (e: CancellationException) {
            // Re-throw cancellation exceptions - don't treat them as errors
            throw e
        } catch (e: Exception) {
            Log.error("JSPluginSource: Error in getMangaList(filters): ${e.message}", e)
            e.printStackTrace()
            MangasPageInfo(emptyList(), false)
        }
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        Log.info("JSPluginSource: [$name] getMangaDetails START for ${manga.key}")
        return try {
            // Convert URL to plugin format (relative path if needed)
            val pluginUrl = toPluginUrl(manga.key)
            Log.info("JSPluginSource: [$name] getMangaDetails calling plugin.getNovelDetails with $pluginUrl")
            
            val details = plugin.getNovelDetails(pluginUrl)
            
            Log.info("JSPluginSource: [$name] getMangaDetails got response: name=${details.name}, cover=${details.cover}")
            
            // Only convert cover to absolute if we have a valid baseUrl
            val coverUrl = if (details.cover.isNotBlank()) {
                if (details.cover.startsWith("http://") || details.cover.startsWith("https://")) {
                    details.cover  // Already absolute
                } else if (baseUrl.isNotBlank() && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
                    SourceHelpers.buildAbsoluteUrl(baseUrl, details.cover)  // Convert relative to absolute
                } else {
                    details.cover  // Keep as-is if no valid baseUrl
                }
            } else {
                ""
            }
            
            val result = manga.copy(
                title = details.name,
                cover = coverUrl,
                author = details.author ?: "",
                description = details.description ?: "",
                genres = details.genres,
                status = parseStatus(details.status)
            )
            Log.info("JSPluginSource: [$name] getMangaDetails SUCCESS: title=${result.title}")
            result
        } catch (e: Exception) {
            Log.error("JSPluginSource: [$name] getMangaDetails ERROR: ${e.message}", e)
            manga
        }
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        return try {
            // Convert URL to plugin format (relative path)
            val pluginUrl = toPluginUrl(manga.key)
            Log.info("JSPluginSource: [$name] getChapterList called for ${manga.key} -> $pluginUrl")
            
            val chapters = plugin.getChapters(pluginUrl)
            
            Log.info("JSPluginSource: [$name] Got ${chapters.size} chapters from plugin")
            
            // Store chapter URLs as ABSOLUTE URLs
            chapters.mapIndexed { index, chapter ->
                // Convert chapter URL to absolute for storage
                val absoluteChapterUrl = if (chapter.url.startsWith("http://") || chapter.url.startsWith("https://")) {
                    chapter.url  // Already absolute
                } else if (_baseUrl.isNotBlank()) {
                    // Append relative path to baseUrl, handling trailing/leading slashes
                    val base = _baseUrl.trimEnd('/')
                    val path = if (chapter.url.startsWith("/")) chapter.url else "/${chapter.url}"
                    base + path
                } else {
                    chapter.url  // Keep as-is if no baseUrl available
                }
                
                ChapterInfo(
                    key = absoluteChapterUrl,  // Store ABSOLUTE URL
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
            // Convert URL to plugin format (relative path if needed)
            val pluginUrl = toPluginUrl(chapter.key)
            Log.info("JSPluginSource: [$name] getPageList called for ${chapter.key} -> $pluginUrl")
            
            val content = plugin.getChapterContent(pluginUrl)
            
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
            val doc = com.fleeksoft.ksoup.Ksoup.parse(html)
            
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
     * IMPORTANT: Store ABSOLUTE URLs in the app (for display, sharing, etc.)
     * The toPluginUrl() function will convert back to relative when calling plugin methods
     */
    private fun PluginNovel.toMangaInfo(): MangaInfo {
        // Auto-detect baseUrl from the first URL we see (for cover images)
        autoDetectBaseUrl(this.url)
        if (this.cover.isNotBlank()) {
            autoDetectBaseUrl(this.cover)
        }
        
        // Convert URL to absolute for storage
        val absoluteUrl = if (this.url.startsWith("http://") || this.url.startsWith("https://")) {
            this.url  // Already absolute
        } else if (_baseUrl.isNotBlank()) {
            // Append relative path to baseUrl, handling trailing/leading slashes
            val base = _baseUrl.trimEnd('/')
            val path = if (this.url.startsWith("/")) this.url else "/${this.url}"
            base + path
        } else {
            this.url  // Keep as-is if no baseUrl available
        }
        
        Log.debug("JSPluginSource: [$name] toMangaInfo: ${this.url} -> $absoluteUrl (baseUrl=$_baseUrl)")
        
        return MangaInfo(
            key = absoluteUrl,  // Store ABSOLUTE URL
            title = this.name,
            cover = if (this.cover.isNotBlank()) {
                // Only convert cover to absolute if we have a valid baseUrl
                if (this.cover.startsWith("http://") || this.cover.startsWith("https://")) {
                    this.cover  // Already absolute
                } else if (baseUrl.isNotBlank() && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
                    SourceHelpers.buildAbsoluteUrl(baseUrl, this.cover)  // Convert relative to absolute
                } else {
                    this.cover  // Keep as-is if no valid baseUrl
                }
            } else ""
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
     * Parse date string to timestamp.
     * Supports common date formats used by novel sources.
     */
    private fun parseDate(dateText: String?): Long {
        if (dateText.isNullOrBlank()) return 0L
        
        val text = dateText.trim().lowercase()
        
        // Handle relative dates like "2 hours ago", "3 days ago"
        val relativeMatch = """(\d+)\s*(second|minute|hour|day|week|month|year)s?\s*ago""".toRegex().find(text)
        if (relativeMatch != null) {
            val amount = relativeMatch.groupValues[1].toLongOrNull() ?: return 0L
            val unit = relativeMatch.groupValues[2]
            val now = currentTimeToLong()
            val msPerUnit = when (unit) {
                "second" -> 1000L
                "minute" -> 60 * 1000L
                "hour" -> 60 * 60 * 1000L
                "day" -> 24 * 60 * 60 * 1000L
                "week" -> 7 * 24 * 60 * 60 * 1000L
                "month" -> 30 * 24 * 60 * 60 * 1000L
                "year" -> 365 * 24 * 60 * 60 * 1000L
                else -> return 0L
            }
            return now - (amount * msPerUnit)
        }
        
        // Handle "just now", "today", "yesterday"
        when {
            text.contains("just now") || text.contains("now") -> return currentTimeToLong()
            text.contains("today") -> return currentTimeToLong()
            text.contains("yesterday") -> return currentTimeToLong() - (24 * 60 * 60 * 1000L)
        }
        
        // Try parsing common date formats
        // Format: YYYY-MM-DD or YYYY/MM/DD
        val isoMatch = """(\d{4})[-/](\d{1,2})[-/](\d{1,2})""".toRegex().find(text)
        if (isoMatch != null) {
            return try {
                val year = isoMatch.groupValues[1].toInt()
                val month = isoMatch.groupValues[2].toInt()
                val day = isoMatch.groupValues[3].toInt()
                dateToTimestamp(year, month, day)
            } catch (e: Exception) { 0L }
        }
        
        // Format: DD-MM-YYYY or DD/MM/YYYY or DD.MM.YYYY
        val dmyMatch = """(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})""".toRegex().find(text)
        if (dmyMatch != null) {
            return try {
                val day = dmyMatch.groupValues[1].toInt()
                val month = dmyMatch.groupValues[2].toInt()
                val year = dmyMatch.groupValues[3].toInt()
                dateToTimestamp(year, month, day)
            } catch (e: Exception) { 0L }
        }
        
        // Format: Month DD, YYYY (e.g., "January 15, 2024")
        val monthNames = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
            "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )
        val monthMatch = """([a-z]+)\s+(\d{1,2}),?\s*(\d{4})""".toRegex().find(text)
        if (monthMatch != null) {
            val monthStr = monthMatch.groupValues[1].take(3)
            val month = monthNames[monthStr]
            if (month != null) {
                return try {
                    val day = monthMatch.groupValues[2].toInt()
                    val year = monthMatch.groupValues[3].toInt()
                    dateToTimestamp(year, month, day)
                } catch (e: Exception) { 0L }
            }
        }
        
        // Format: DD Month YYYY (e.g., "15 January 2024")
        val dayMonthMatch = """(\d{1,2})\s+([a-z]+)\s+(\d{4})""".toRegex().find(text)
        if (dayMonthMatch != null) {
            val monthStr = dayMonthMatch.groupValues[2].take(3)
            val month = monthNames[monthStr]
            if (month != null) {
                return try {
                    val day = dayMonthMatch.groupValues[1].toInt()
                    val year = dayMonthMatch.groupValues[3].toInt()
                    dateToTimestamp(year, month, day)
                } catch (e: Exception) { 0L }
            }
        }
        
        return 0L
    }
    
    /**
     * Convert year, month, day to Unix timestamp (milliseconds).
     * Uses a simple calculation without external date libraries.
     */
    private fun dateToTimestamp(year: Int, month: Int, day: Int): Long {
        // Days in each month (non-leap year)
        val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        
        // Calculate days since Unix epoch (1970-01-01)
        var days = 0L
        
        // Add days for complete years
        for (y in 1970 until year) {
            days += if (isLeapYear(y)) 366 else 365
        }
        
        // Add days for complete months in current year
        for (m in 1 until month) {
            days += daysInMonth[m]
            if (m == 2 && isLeapYear(year)) days += 1
        }
        
        // Add remaining days
        days += day - 1
        
        // Convert to milliseconds
        return days * 24 * 60 * 60 * 1000L
    }
    
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
