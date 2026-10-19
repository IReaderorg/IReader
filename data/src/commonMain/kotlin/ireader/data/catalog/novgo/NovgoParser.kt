package ireader.data.catalog.novgo

import com.fleeksoft.ksoup.Ksoup
import io.ktor.http.encodeURLParameter
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.MangaInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HTML/JSON parser for AllNovelFull, which now lives at novgo.net.
 * The published sources-bundle.js does not include this site's selectors.
 */
object NovgoParser {
    const val BASE = "https://novgo.net"

    private val json = Json { ignoreUnknownKeys = true }

    fun listingUrl(query: String, page: Int): String {
        val safePage = if (page < 1) 1 else page
        return if (query.isBlank()) {
            "$BASE/most-popular?page=$safePage"
        } else {
            "$BASE/search?keyword=${query.encodeURLParameter()}&page=$safePage"
        }
    }

    fun parseListing(html: String): List<MangaInfo> {
        return Ksoup.parse(html).select(".li-row").mapNotNull { row ->
            val link = row.selectFirst("h3.tit a") ?: return@mapNotNull null
            val title = link.text().trim()
            val href = link.attr("href")
            if (title.isEmpty() || href.isEmpty()) return@mapNotNull null
            val cover = row.selectFirst(".pic img")?.attr("src").orEmpty()
            MangaInfo(
                key = absolute(href),
                title = title,
                cover = absolute(cover),
            )
        }
    }

    fun parseBook(html: String, key: String): MangaInfo {
        val document = Ksoup.parse(html)
        val raw = document.select("script[type=application/ld+json]")
            .map { it.data() }
            .firstOrNull { it.contains("Book") }
            ?: return MangaInfo(key = key, title = "")
        val book = runCatching { json.decodeFromString<LdBook>(raw) }.getOrNull()
            ?: return MangaInfo(key = key, title = "")
        return MangaInfo(
            key = key,
            title = book.name,
            description = book.description,
            author = book.author.firstOrNull()?.name.orEmpty(),
            genres = book.genre,
            cover = absolute(book.image),
        )
    }

    fun chapterListInfo(html: String): ChapterListInfo? {
        val element = Ksoup.parse(html).selectFirst("#list-chapter") ?: return null
        val novelId = element.attr("data-novel-id")
        if (novelId.isBlank()) return null
        val pages = element.attr("data-total-page").toIntOrNull()?.coerceAtLeast(1) ?: 1
        return ChapterListInfo(novelId, pages)
    }

    fun chapterListUrl(novelId: String, page: Int): String {
        return "$BASE/ajax-chapter-list?novelId=$novelId&page=$page"
    }

    fun parseChapterPage(jsonText: String): List<ChapterInfo> {
        val page = json.decodeFromString<ChapterPage>(jsonText)
        return parseChapterLinks(page.html)
    }

    fun parseChapterLinks(html: String): List<ChapterInfo> {
        return Ksoup.parse(html).select("a.con").map { link ->
            val name = link.selectFirst(".chapter-text")?.text()?.trim().orEmpty()
                .ifBlank { link.attr("title").trim() }
            ChapterInfo(
                key = absolute(link.attr("href")),
                name = name,
                number = ChapterInfo.extractChapterNumber(name),
            )
        }
    }

    fun parseContent(html: String): List<String> {
        val document = Ksoup.parse(html)
        val paragraphs = document.select("#chapter-content p")
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }
        if (paragraphs.isNotEmpty()) return paragraphs
        val text = document.selectFirst("#chapter-content")?.text()?.trim().orEmpty()
        return if (text.isEmpty()) emptyList() else listOf(text)
    }

    fun absolute(path: String): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return BASE + if (path.startsWith("/")) path else "/$path"
    }

    data class ChapterListInfo(val novelId: String, val pages: Int)

    @Serializable
    private data class ChapterPage(val html: String = "")

    @Serializable
    private data class LdBook(
        val name: String = "",
        val description: String = "",
        val image: String = "",
        val genre: List<String> = emptyList(),
        val author: List<LdAuthor> = emptyList(),
    )

    @Serializable
    private data class LdAuthor(val name: String = "")
}
