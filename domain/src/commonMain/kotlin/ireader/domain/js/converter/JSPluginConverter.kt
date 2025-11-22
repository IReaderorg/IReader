package ireader.domain.js.converter

import ireader.core.source.Dependencies
import ireader.core.source.HttpSource
import ireader.core.source.model.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Converts analyzed JS plugins into native Kotlin HttpSource implementations
 */
class JSPluginConverter {
    
    // Support both old regex-based and new AST-based analyzers
    fun convert(analyzed: JSPluginAnalyzer.AnalyzedPlugin, dependencies: Dependencies): HttpSource {
        return GeneratedHttpSource(
            name = analyzed.metadata.name,
            lang = analyzed.metadata.lang,
            baseUrl = analyzed.metadata.baseUrl,
            searchPattern = analyzed.searchPattern?.let { 
                SearchPatternWrapper(it.urlTemplate, it.selector, it.titleSelector, it.linkSelector, it.coverSelector)
            },
            detailsPattern = analyzed.detailsPattern?.let {
                DetailsPatternWrapper(it.authorSelector, it.descriptionSelector, it.genresSelector, 
                    it.statusSelector, it.coverSelector, it.chaptersSelector, it.chapterNameSelector, it.chapterUrlSelector)
            },
            contentPattern = analyzed.contentPattern?.let {
                ContentPatternWrapper(it.selector, it.textJoin)
            },
            dependencies = dependencies
        )
    }
    
    fun convert(analyzed: JSPluginAnalyzerAST.AnalyzedPlugin, dependencies: Dependencies): HttpSource {
        return GeneratedHttpSource(
            name = analyzed.metadata.name,
            lang = analyzed.metadata.lang,
            baseUrl = analyzed.metadata.baseUrl,
            searchPattern = analyzed.searchPattern?.let { 
                SearchPatternWrapper(it.urlTemplate, it.selector, it.titleSelector, it.linkSelector, it.coverSelector)
            },
            detailsPattern = analyzed.detailsPattern?.let {
                DetailsPatternWrapper(it.authorSelector, it.descriptionSelector, it.genresSelector, 
                    it.statusSelector, it.coverSelector, it.chaptersSelector, it.chapterNameSelector, it.chapterUrlSelector)
            },
            contentPattern = analyzed.contentPattern?.let {
                ContentPatternWrapper(it.selector, it.textJoin)
            },
            dependencies = dependencies
        )
    }
    
    // Wrapper classes to unify both analyzer outputs
    private data class SearchPatternWrapper(
        val urlTemplate: String,
        val selector: String,
        val titleSelector: String,
        val linkSelector: String,
        val coverSelector: String?
    )
    
    private data class DetailsPatternWrapper(
        val authorSelector: String?,
        val descriptionSelector: String?,
        val genresSelector: String?,
        val statusSelector: String?,
        val coverSelector: String?,
        val chaptersSelector: String?,
        val chapterNameSelector: String?,
        val chapterUrlSelector: String?
    )
    
    private data class ContentPatternWrapper(
        val selector: String,
        val textJoin: String
    )
    
    private class GeneratedHttpSource(
        override val name: String,
        override val lang: String,
        override val baseUrl: String,
        private val searchPattern: SearchPatternWrapper?,
        private val detailsPattern: DetailsPatternWrapper?,
        private val contentPattern: ContentPatternWrapper?,
        dependencies: Dependencies
    ) : HttpSource(dependencies) {
        
        override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
            return getMangaList(emptyList(), page)
        }
        
        override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
            val pattern = searchPattern ?: run {
                println("JSPluginConverter: No search pattern available")
                println("  This plugin uses complex JavaScript that can't be converted")
                println("  Consider using native sources or opening in WebView")
                return MangasPageInfo(emptyList(), false)
            }
            
            return try {
                // Build URL from template
                val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
                val url = buildSearchUrl(pattern.urlTemplate, query, page)
                
                println("JSPluginConverter: Fetching search URL: $url")
                println("JSPluginConverter: Query: '$query', Page: $page")
                
                val html = client.get(url).bodyAsText()
                val doc = Jsoup.parse(html, baseUrl)
                
                // Try multiple selectors if the main one fails
                val selectors = pattern.selector.split(",").map { it.trim() }
                var elements = org.jsoup.select.Elements()
                
                for (selector in selectors) {
                    elements = doc.select(selector)
                    if (elements.isNotEmpty()) {
                        println("JSPluginConverter: Found ${elements.size} results with selector: $selector")
                        break
                    }
                }
                
                if (elements.isEmpty()) {
                    println("JSPluginConverter: No results found with any selector")
                    return MangasPageInfo(emptyList(), false)
                }
                
                val mangas = elements.mapNotNull { element ->
                    parseMangaFromSearch(element, pattern)
                }
                
                println("JSPluginConverter: Parsed ${mangas.size} manga from ${elements.size} elements")
                
                MangasPageInfo(mangas, mangas.isNotEmpty())
            } catch (e: Exception) {
                println("JSPluginConverter: Error in getMangaList: ${e.message}")
                e.printStackTrace()
                MangasPageInfo(emptyList(), false)
            }
        }
        
        private fun buildSearchUrl(template: String, query: String, page: Int): String {
            return template
                .replace("\${this.site}", baseUrl)
                .replace("\${this.baseUrl}", baseUrl)
                .replace("\${query}", query)
                .replace("\${page}", page.toString())
                .replace("\$query", query)
                .replace("\$page", page.toString())
        }
        
        private fun parseMangaFromSearch(
            element: Element,
            pattern: SearchPatternWrapper
        ): MangaInfo? {
            return try {
                // Try to find link - try multiple selectors
                val linkSelectors = pattern.linkSelector.split(",").map { it.trim() }
                var link: String? = null
                
                for (selector in linkSelectors) {
                    link = element.selectFirst(selector)?.attr("abs:href")
                    if (!link.isNullOrBlank()) break
                }
                
                // If no link found with selector, try href on element itself
                if (link.isNullOrBlank()) {
                    link = element.attr("abs:href")
                }
                
                if (link.isNullOrBlank()) {
                    println("JSPluginConverter: No link found in element")
                    return null
                }
                
                // Try to find title - try multiple selectors
                val titleSelectors = pattern.titleSelector.split(",").map { it.trim() }
                var title: String? = null
                
                for (selector in titleSelectors) {
                    title = element.selectFirst(selector)?.text()
                    if (!title.isNullOrBlank()) break
                }
                
                // If no title found, try element text itself
                if (title.isNullOrBlank()) {
                    title = element.text()
                }
                
                if (title.isNullOrBlank()) {
                    println("JSPluginConverter: No title found in element")
                    return null
                }
                
                // Try to find cover
                var cover = ""
                if (pattern.coverSelector != null) {
                    val coverSelectors = pattern.coverSelector.split(",").map { it.trim() }
                    for (selector in coverSelectors) {
                        val img = element.selectFirst(selector)
                        cover = img?.attr("abs:src") ?: img?.attr("abs:data-src") ?: ""
                        if (cover.isNotBlank()) break
                    }
                }
                
                // If no cover found with selector, try any img tag
                if (cover.isBlank()) {
                    val img = element.selectFirst("img")
                    cover = img?.attr("abs:src") ?: img?.attr("abs:data-src") ?: ""
                }
                
                MangaInfo(
                    key = link,
                    title = title.trim(),
                    cover = cover
                )
            } catch (e: Exception) {
                println("JSPluginConverter: Error parsing manga from element: ${e.message}")
                null
            }
        }
        
        override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
            val pattern = detailsPattern ?: return manga
            
            return try {
                println("JSPluginConverter: Fetching details from: ${manga.key}")
                
                val html = client.get(manga.key).bodyAsText()
                val doc = Jsoup.parse(html, baseUrl)
                
                // Extract author with fallback selectors
                val author = pattern.authorSelector?.let { selectors ->
                    selectors.split(",").map { it.trim() }.firstNotNullOfOrNull { selector ->
                        doc.selectFirst(selector)?.text()?.takeIf { it.isNotBlank() }
                    }
                } ?: manga.author
                
                // Extract description with fallback selectors
                val description = pattern.descriptionSelector?.let { selectors ->
                    selectors.split(",").map { it.trim() }.firstNotNullOfOrNull { selector ->
                        doc.selectFirst(selector)?.text()?.takeIf { it.isNotBlank() }
                    }
                } ?: manga.description
                
                // Extract genres with fallback selectors
                val genres = pattern.genresSelector?.let { selectors ->
                    selectors.split(",").map { it.trim() }.firstNotNullOfOrNull { selector ->
                        val elements = doc.select(selector)
                        if (elements.isNotEmpty()) {
                            elements.map { it.text().trim() }.filter { it.isNotBlank() }
                        } else null
                    }
                } ?: manga.genres
                
                // Extract status with fallback selectors
                val status = pattern.statusSelector?.let { selectors ->
                    selectors.split(",").map { it.trim() }.firstNotNullOfOrNull { selector ->
                        doc.selectFirst(selector)?.text()?.let { parseStatus(it) }
                    }
                } ?: manga.status
                
                // Extract cover with fallback selectors
                val cover = pattern.coverSelector?.let { selectors ->
                    selectors.split(",").map { it.trim() }.firstNotNullOfOrNull { selector ->
                        val img = doc.selectFirst(selector)
                        (img?.attr("abs:src") ?: img?.attr("abs:data-src"))?.takeIf { it.isNotBlank() }
                    }
                } ?: manga.cover
                
                println("JSPluginConverter: Extracted details - author: $author, genres: ${genres.size}, status: $status")
                
                manga.copy(
                    author = author,
                    description = description,
                    genres = genres,
                    status = status,
                    cover = cover
                )
            } catch (e: Exception) {
                println("JSPluginConverter: Error in getMangaDetails: ${e.message}")
                e.printStackTrace()
                manga
            }
        }
        
        override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
            val pattern = detailsPattern ?: return emptyList()
            val chaptersSelector = pattern.chaptersSelector ?: return emptyList()
            
            return try {
                println("JSPluginConverter: Fetching chapters from: ${manga.key}")
                
                val html = client.get(manga.key).bodyAsText()
                val doc = Jsoup.parse(html, baseUrl)
                
                // Try multiple selectors
                val selectors = chaptersSelector.split(",").map { it.trim() }
                var elements = org.jsoup.select.Elements()
                
                for (selector in selectors) {
                    elements = doc.select(selector)
                    if (elements.isNotEmpty()) {
                        println("JSPluginConverter: Found ${elements.size} chapters with selector: $selector")
                        break
                    }
                }
                
                if (elements.isEmpty()) {
                    println("JSPluginConverter: No chapters found")
                    return emptyList()
                }
                
                val chapters = elements.mapIndexedNotNull { index, element ->
                    try {
                        // Try to find chapter URL
                        val chapterUrl = element.selectFirst("a")?.attr("abs:href") 
                            ?: element.attr("abs:href")
                            ?: return@mapIndexedNotNull null
                        
                        // Try to find chapter name
                        val chapterName = element.selectFirst(pattern.chapterNameSelector ?: "a")?.text()
                            ?: element.selectFirst("a")?.text()
                            ?: element.text()
                            ?: "Chapter ${index + 1}"
                        
                        ChapterInfo(
                            key = chapterUrl,
                            name = chapterName.trim(),
                            number = (index + 1).toFloat()
                        )
                    } catch (e: Exception) {
                        println("JSPluginConverter: Error parsing chapter at index $index: ${e.message}")
                        null
                    }
                }
                
                println("JSPluginConverter: Parsed ${chapters.size} chapters")
                chapters
            } catch (e: Exception) {
                println("JSPluginConverter: Error in getChapterList: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
        
        override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
            val pattern = contentPattern ?: return emptyList()
            
            return try {
                println("JSPluginConverter: Fetching content from: ${chapter.key}")
                
                val html = client.get(chapter.key).bodyAsText()
                val doc = Jsoup.parse(html, baseUrl)
                
                // Try multiple selectors
                val selectors = pattern.selector.split(",").map { it.trim() }
                var elements = org.jsoup.select.Elements()
                
                for (selector in selectors) {
                    elements = doc.select(selector)
                    if (elements.isNotEmpty()) {
                        println("JSPluginConverter: Found ${elements.size} content elements with selector: $selector")
                        break
                    }
                }
                
                if (elements.isEmpty()) {
                    println("JSPluginConverter: No content found, trying body text")
                    // Fallback to body text
                    val bodyText = doc.body()?.text() ?: ""
                    return if (bodyText.isNotBlank()) {
                        listOf(Text(bodyText))
                    } else {
                        emptyList()
                    }
                }
                
                // Join content with specified separator
                val content = elements.joinToString(pattern.textJoin) { element ->
                    // Get text and clean it up
                    element.text().trim()
                }.trim()
                
                if (content.isBlank()) {
                    println("JSPluginConverter: Content is blank after parsing")
                    return emptyList()
                }
                
                println("JSPluginConverter: Extracted ${content.length} characters of content")
                
                listOf(Text(content))
            } catch (e: Exception) {
                println("JSPluginConverter: Error in getPageList: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
        
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
        
        override fun getFilters(): FilterList {
            return listOf(Filter.Title())
        }
    }
}
