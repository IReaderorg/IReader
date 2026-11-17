package ireader.data.catalog.freewebnovel

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HTML parser for FreeWebNovel. Listing paths moved under /sort/.
 * The old latest-release-novels URLs now return 404.
 */
object FreeWebNovelParser {
    const val BASE = "https://freewebnovel.com"
    const val SEARCH = "$BASE/search"

    private val json = Json { ignoreUnknownKeys = true }

    fun latestUrl(page: Int): String = sortUrl("latest-release", page)

    fun popularUrl(page: Int): String = sortUrl("most-popular", page)

    fun newNovelsUrl(page: Int): String = sortUrl("latest-novel", page)

    fun chapterAjaxUrl(novelUrl: String, page: Int): String {
        val base = novelUrl.substringBefore('?').trimEnd('/')
        return "$base?ajax=chapters&page=${page.coerceAtLeast(1)}&pageSize=40"
    }

    fun parseLatest(html: String, page: Int = 1): MangasPageInfo = parseListing(html, page)

    fun parsePopular(html: String, page: Int = 1): MangasPageInfo = parseListing(html, page)

    fun parseNewNovels(html: String, page: Int = 1): MangasPageInfo = parseListing(html, page)

    fun parseSearch(html: String, page: Int = 1): MangasPageInfo = parseListing(html, page)

    fun parseBook(html: String, key: String): MangaInfo {
        val document = Ksoup.parse(html)
        val genres = document.select("[title=Genre]").firstOrNull()
            ?.nextElementSibling()
            ?.text()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        val status = document.select("[title=Status]").firstOrNull()
            ?.nextElementSibling()
            ?.text()
            ?.let(::statusOf)
            ?: MangaInfo.ONGOING
        val author = document.select("a[href*=/author/]").firstOrNull()
        return MangaInfo(
            key = key,
            title = document.select("div.m-desc h1.tit").text(),
            cover = absolute(document.select("div.m-book1 div.pic img, .m-info .pic img").attr("src")),
            description = document.select("div.inner p").joinToString("\n") { it.text() },
            author = author?.attr("title").orEmpty().ifBlank { author?.text().orEmpty() },
            genres = genres,
            status = status,
        )
    }

    fun chapterPageCount(html: String): Int {
        return Ksoup.parse(html).selectFirst("#indexListPage")
            ?.attr("data-total-page")
            ?.toIntOrNull()
            ?.coerceAtLeast(1)
            ?: 1
    }

    fun parseChapters(html: String): List<ChapterInfo> {
        val listed = Ksoup.parse(html).select("div.m-newest2 ul.ul-list5 li a")
        if (listed.isNotEmpty()) return listed.map(::chapterFromLink)
        return parseChapterLinks(html)
    }

    fun parseChapterAjax(jsonText: String): List<ChapterInfo> {
        val page = runCatching { json.decodeFromString<ChapterAjax>(jsonText) }.getOrNull() ?: return emptyList()
        if (page.html.isBlank()) return emptyList()
        return parseChapterLinks(page.html)
    }

    fun parseContent(html: String): List<String> {
        val document = Ksoup.parse(html)
        val article = document.selectFirst("#article")
        val nodes = if (article != null) article.select("h4, p") else document.select("div.txt h4, div.txt p")
        return nodes.map { it.text().trim() }.filter { it.isNotEmpty() }
    }

    private fun parseListing(html: String, page: Int): MangasPageInfo {
        val document = Ksoup.parse(html)
        val books = document.select("div.ul-list1 div.li-row, div.ul-list1 div.li").mapNotNull { row ->
            if (row.selectFirst("div.li-row, div.li") != null && row.hasClass("ul-list1")) return@mapNotNull null
            val link = row.selectFirst("h3.tit a")
                ?: row.selectFirst("div.txt a[title]")
                ?: row.selectFirst("a[title]")
                ?: return@mapNotNull null
            val title = link.attr("title").ifBlank { link.text() }.trim()
            val href = link.attr("href")
            if (title.isEmpty() || href.isEmpty() || !href.contains("/novel/")) return@mapNotNull null
            MangaInfo(
                key = absolute(href),
                title = title,
                cover = absolute(row.selectFirst("div.pic img, img")?.attr("src").orEmpty()),
            )
        }.distinctBy { it.key }
        return MangasPageInfo(books, hasNextPage(document, page) && books.isNotEmpty())
    }

    private fun hasNextPage(document: Document, page: Int): Boolean {
        val next = page.coerceAtLeast(1) + 1
        return document.select(".pages a").any { link ->
            link.text().trim() == next.toString()
        }
    }

    private fun parseChapterLinks(html: String): List<ChapterInfo> {
        return Ksoup.parse(html).select("a.con, li a[href*=/chapter-]").map(::chapterFromLink)
            .filter { it.key.isNotBlank() && it.name.isNotBlank() }
            .distinctBy { it.key }
    }

    private fun chapterFromLink(link: Element): ChapterInfo {
        val name = link.attr("title").trim().ifBlank { link.text().trim() }
        return ChapterInfo(
            key = absolute(link.attr("href")),
            name = name,
            number = ChapterInfo.extractChapterNumber(name),
        )
    }

    private fun statusOf(text: String): Long = when (text.trim()) {
        "OnGoing" -> MangaInfo.ONGOING
        "Complete" -> MangaInfo.COMPLETED
        else -> MangaInfo.ONGOING
    }

    private fun sortUrl(kind: String, page: Int): String {
        val safePage = page.coerceAtLeast(1)
        return if (safePage == 1) "$BASE/sort/$kind" else "$BASE/sort/$kind/$safePage"
    }

    private fun absolute(path: String): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return BASE + if (path.startsWith("/")) path else "/$path"
    }

    @Serializable
    private data class ChapterAjax(val html: String = "")
}
