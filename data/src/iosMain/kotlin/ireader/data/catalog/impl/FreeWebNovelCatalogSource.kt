package ireader.data.catalog.impl

import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import ireader.core.http.HttpClients
import ireader.core.source.CatalogSource
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.Filter
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.data.catalog.freewebnovel.FreeWebNovelParser

/**
 * FreeWebNovel on iOS. Kazem asked to use this source for testing while the
 * JS bundle is rebuilt.
 */
class FreeWebNovelCatalogSource(
    private val httpClients: HttpClients,
    override val id: Long,
) : CatalogSource {

    override val name: String = "FreeWebNovel"
    override val lang: String = "en"

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return FreeWebNovelParser.parseLatest(fetch(FreeWebNovelParser.latestUrl(page)), page)
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value.orEmpty()
        if (query.isNotBlank()) {
            return FreeWebNovelParser.parseSearch(search(query), page)
        }
        val sortIndex = filters.filterIsInstance<Filter.Sort>().firstOrNull()?.value?.index
        return when (sortIndex) {
            1 -> FreeWebNovelParser.parsePopular(fetch(FreeWebNovelParser.popularUrl(page)), page)
            2 -> FreeWebNovelParser.parseNewNovels(fetch(FreeWebNovelParser.newNovelsUrl(page)), page)
            else -> FreeWebNovelParser.parseLatest(fetch(FreeWebNovelParser.latestUrl(page)), page)
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        val parsed = FreeWebNovelParser.parseBook(fetch(manga.key), manga.key)
        return if (parsed.title.isBlank()) manga else parsed
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        val html = fetch(manga.key)
        val pages = FreeWebNovelParser.chapterPageCount(html)
        if (pages <= 1) return FreeWebNovelParser.parseChapters(html)
        val chapters = mutableListOf<ChapterInfo>()
        for (page in 1..pages) {
            val parsed = FreeWebNovelParser.parseChapterAjax(
                fetch(FreeWebNovelParser.chapterAjaxUrl(manga.key, page))
            )
            if (parsed.isEmpty()) break
            chapters += parsed
        }
        return chapters.ifEmpty { FreeWebNovelParser.parseChapters(html) }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return FreeWebNovelParser.parseContent(fetch(chapter.key)).map { Text(it) }
    }

    override fun getListings(): List<Listing> = listOf(object : Listing("Latest") {})

    override fun getFilters(): FilterList = listOf(
        Filter.Title(),
        Filter.Sort("Sort By:", arrayOf("Latest", "Popular", "New Novels")),
    )

    override fun getCommands(): CommandList = emptyList()

    private suspend fun fetch(url: String): String {
        return httpClients.default.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.CacheControl, "max-age=0")
            header(HttpHeaders.Referrer, FreeWebNovelParser.BASE)
        }.bodyAsText()
    }

    private suspend fun search(query: String): String {
        return httpClients.default.submitForm(
            url = FreeWebNovelParser.SEARCH,
            formParameters = parameters { append("keyword", query) },
        ) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.Referrer, FreeWebNovelParser.BASE)
        }.bodyAsText()
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
    }
}

internal fun freeWebNovelSourceOrNull(pkgName: String, sourceName: String, sourceId: Long, httpClients: HttpClients): CatalogSource? {
    val match = pkgName.contains("freewebnovel") || sourceName.equals("FreeWebNovel", ignoreCase = true)
    if (!match) return null
    return FreeWebNovelCatalogSource(httpClients, sourceId)
}
