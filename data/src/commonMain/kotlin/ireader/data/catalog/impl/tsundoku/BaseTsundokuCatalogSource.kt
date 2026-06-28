package ireader.data.catalog.impl.tsundoku

import com.fleeksoft.ksoup.Ksoup
import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.CommandList
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Page
import ireader.core.source.model.Text

/**
 * Abstract base for [TsundokuCatalogSource] that holds all platform-agnostic logic.
 *
 * Platform-specific subclasses (Android, Desktop) provide the eu.kanade-dependent
 * model conversions and network calls, which cannot live in commonMain because
 * eu.kanade.tachiyomi types exist only in platform-specific source sets.
 */
abstract class BaseTsundokuCatalogSource : CatalogSource {

    /** Whether the underlying source supports fetching latest updates. */
    protected abstract val supportsLatest: Boolean

    // ── Trivial overrides (no eu.kanade dependency) ─────────────────

    override fun getFilters(): List<Filter<*>> = emptyList()
    override fun getCommands(): CommandList = emptyList()
    override suspend fun getChapterPageCount(manga: MangaInfo): Int = 1
    override fun supportsPaginatedChapters(): Boolean = false

    override fun getListings(): List<Listing> {
        val listings = mutableListOf<Listing>(PopularListing())
        if (supportsLatest) listings.add(LatestListing())
        return listings
    }

    override fun toString(): String = "TsundokuSource($name, $lang, id=$id)"

    // ── Novel content parsing ──────────────────────────────────────

    /**
     * Parse HTML content from a novel source into multiple readable [Text] pages.
     * Splits by paragraph tags, headings, and line breaks so the reader
     * can display content in digestible chunks instead of one giant wall of text.
     */
    protected fun parseNovelContent(html: String): List<Page> {
        val doc = Ksoup.parse(html)
        val contentElement = doc.selectFirst(
            ".chapter-content, .entry-content, .content, article, .text, #content, .chapter_body, .reading-content"
        ) ?: doc.body()
        ?: return listOf(Text(html))

        val paragraphs = mutableListOf<String>()

        // Extract text from content, preserving paragraph structure
        for (element in contentElement.children()) {
            val tag = element.tagName().lowercase()
            when (tag) {
                "p", "div", "section" -> {
                    val text = element.text().trim()
                    if (text.isNotBlank()) paragraphs.add(text)
                }
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val text = element.text().trim()
                    if (text.isNotBlank()) paragraphs.add(text)
                }
                "br" -> {
                    // skip bare <br> tags
                }
                "img" -> {
                    // Image in novel content — skip or handle as needed
                    val src = element.attr("src")
                    if (src.isNotBlank()) {
                        // future: could create ImageUrl pages here
                    }
                }
                else -> {
                    // For any other block elements, try to get their text
                    val text = element.text().trim()
                    if (text.isNotBlank()) paragraphs.add(text)
                }
            }
        }

        // If no child elements parsed, fall back to full text extraction
        if (paragraphs.isEmpty()) {
            val fullText = contentElement.text().trim()
            if (fullText.isNotBlank()) {
                val lines = fullText.split(Regex("\n{2,}|<br\\s*/?>|</?p>"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                return lines.map { Text(it) }
            }
            return listOf(Text(html))
        }

        Log.info { "Tsundoku[$name]: Parsed ${paragraphs.size} paragraphs from HTML" }
        return paragraphs.map { Text(it) }
    }

    // ── Shared listing types ───────────────────────────────────────

    class PopularListing : Listing("Popular")
    class LatestListing : Listing("Latest")
}
