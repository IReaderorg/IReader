package ireader.data.catalog.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
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
import ireader.data.catalog.novgo.NovgoParser

/**
 * AllNovelFull on iOS. The downloaded JS bundle does not contain the site's
 * selectors, and the site now redirects to novgo.net.
 */
class NovgoCatalogSource(
    private val httpClients: HttpClients,
    override val id: Long,
) : CatalogSource {

    private val client: HttpClient
        get() = httpClients.default

    override val name: String = "AllNovelFull"
    override val lang: String = "en"

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return loadListing("", page)
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value.orEmpty()
        return loadListing(query, page)
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        val html = fetch(manga.key)
        val parsed = NovgoParser.parseBook(html, manga.key)
        return if (parsed.title.isBlank()) manga else parsed
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        val html = fetch(manga.key)
        val info = NovgoParser.chapterListInfo(html) ?: return emptyList()
        val chapters = mutableListOf<ChapterInfo>()
        for (page in 1..info.pages) {
            val body = fetch(NovgoParser.chapterListUrl(info.novelId, page))
            chapters += NovgoParser.parseChapterPage(body)
        }
        return chapters
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        val html = fetch(chapter.key)
        return NovgoParser.parseContent(html).map { Text(it) }
    }

    override fun getListings(): List<Listing> = listOf(object : Listing("Popular") {})

    override fun getFilters(): FilterList = listOf(Filter.Title())

    override fun getCommands(): CommandList = emptyList()

    private suspend fun loadListing(query: String, page: Int): MangasPageInfo {
        val books = NovgoParser.parseListing(fetch(NovgoParser.listingUrl(query, page)))
        return MangasPageInfo(books, books.isNotEmpty())
    }

    private suspend fun fetch(url: String): String {
        return client.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.Accept, "text/html,application/json")
        }.bodyAsText()
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile"
    }
}

internal fun novgoSourceOrNull(pkgName: String, sourceId: Long, httpClients: HttpClients): CatalogSource? {
    val isAllNovel = pkgName == "ireader.allnovelfull.en" || pkgName.contains("allnovelfull")
    if (!isAllNovel) return null
    return NovgoCatalogSource(httpClients, sourceId)
}
