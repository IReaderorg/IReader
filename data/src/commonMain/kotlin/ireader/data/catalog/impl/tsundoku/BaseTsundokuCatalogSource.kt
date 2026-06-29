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
     *
     * Strategy: inject unique break markers into the DOM at block boundaries,
     * then let Ksoup's recursive [org.jsoup.nodes.Element.text] do all the heavy
     * lifting of whitespace normalization and nested-text extraction. Finally split
     * on the markers to recover clean, standalone paragraphs.
     *
     * This works regardless of HTML nesting depth because [org.jsoup.nodes.Element.text]
     * walks the entire subtree — no `children()`-only iteration like the old approach.
     */
    protected fun parseNovelContent(html: String): List<Page> {
        val doc = Ksoup.parse(html)

        // 1. Strip noise elements that never contain chapter text
        doc.select("script, style, noscript, nav, footer, header, aside, form, iframe, pre, code, .comments, #comments").remove()

        // 2. Find the main content container — try common selectors, fall back to body.
        //    Even body is fine now because step 1 already removed the junk.
        val contentElement = doc.selectFirst(
            ".chapter-content, .entry-content, .content, article, .text, #content, .chapter_body, .reading-content"
        ) ?: doc.body()
        ?: return listOf(Text(html))

        // 3. Inject a unique textual break marker at every block boundary.
        //    Ksoup's .text() collapses newlines to spaces but does NOT touch our marker.
        //    Single pass: br only gets .append(), block elements get both .prepend() and .append().
        val marker = "§¶§"
        val allBlocks = contentElement.select("p, div, section, article, li, h1, h2, h3, h4, h5, h6")
        allBlocks.prepend(" $marker ")
        allBlocks.append(" $marker ")
        contentElement.select("br").let { brs ->
            // brs already covered by the block selector above if br was in it, but it's not.
            // Only append — br is an empty void element.
            brs.append(" $marker ")
        }

        // 4. Extract recursively-normalised text (handles arbitrarily deep nesting)
        val normalized = contentElement.text()

        // 5. Split on the marker and clean each paragraph
        val paragraphs = normalized.split(marker)
            .map { it.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotBlank() }

        if (paragraphs.isEmpty()) {
            return listOf(Text(html))
        }

        Log.info { "Tsundoku[$name]: Parsed ${paragraphs.size} paragraphs from HTML" }
        return paragraphs.map { Text(it) }
    }

    // ── Shared listing types ───────────────────────────────────────

    class PopularListing : Listing("Popular")
    class LatestListing : Listing("Latest")
}
