package org.ireader.extensions.sources.en.mtlnovel

import io.ktor.client.request.*
import io.ktor.http.*
import okhttp3.Headers
import org.ireader.source.core.*
import org.ireader.source.models.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class MtlNovel(deps: Dependencies) : ParsedHttpSource(deps) {

    override val name = "MtlNovel"
    override val creator: String = "@Kazem"
    override val iconUrl: String = "https://www.mtlnovel.net/themes/mtlnovel/images/mtlnovel-32.png"
    override val baseUrl = "https://www.mtlnovel.com"
    override val lang = "en"

    override fun getFilterList(): FilterList {
        return FilterList()
    }

    override fun getListings(): List<Listing> {
        return listOf(PopularListing(), LatestListing(), SearchListing())
    }

    override fun fetchLatestEndpoint(page: Int): String? =
        "/novel-list/?orderby=date&order=desc&status=all&pg=$page"

    override fun fetchPopularEndpoint(page: Int): String? =
        "/monthly-rank/page/$page/"

    override fun fetchSearchEndpoint(page: Int, query: String): String? =
        "/wp-admin/admin-ajax.php?action=autosuggest&q=$query&__amp_source_origin=https%3A%2F%2Fwww.mtlnovel.com"


    override fun headersBuilder() = Headers.Builder().apply {
        add(
            HttpHeaders.UserAgent,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4793.0 Safari/537.36"
        )
        add(HttpHeaders.Referrer, baseUrl)
        add(HttpHeaders.CacheControl, "max-age=0")
    }

    override val headers: Headers = headersBuilder().build()


    // popular
    override fun popularRequest(page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + fetchPopularEndpoint(page = page))
        }
    }

    override fun popularSelector() = "div.box"

    override fun popularFromElement(element: Element): MangaInfo {
        val title = element.select("a.list-title").attr("aria-label")
        val url = element.select("a.list-title").attr("href")
        val thumbnailUrl = element.select("amp-img.list-img").attr("src")
        return MangaInfo(key = url, title = title, cover = thumbnailUrl)
    }

    override fun popularNextPageSelector() = "#pagination > a:nth-child(13)"


    // latest

    override fun latestRequest(page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + fetchLatestEndpoint(page)!!)
            headers { headers }
        }
    }

    override fun latestSelector(): String = "div.box"


    override fun latestFromElement(element: Element): MangaInfo = popularFromElement(element)
    override fun latestNextPageSelector() = popularNextPageSelector()

    override fun searchSelector() = "div.ul-list1 div.li-row"

    override fun searchFromElement(element: Element): MangaInfo {
        val title = element.select("div.txt a").attr("title")
        val url = element.select("div.txt a").attr("href")
        val thumbnailUrl = element.select("div.pic img").attr("src")
        return MangaInfo(key = url, title = title, cover = thumbnailUrl)
    }

    override fun searchNextPageSelector(): String? = null


    // manga details
    override fun detailParse(document: Document): MangaInfo {
        val title = document.select("h1.entry-title").text()
        val cover = document.select("div.nov-head img").attr("src")
        val authorBookSelector = document.select("#author a").text()
        val description = document.select("div.desc p").eachText().joinToString("\n")
        val category = document.select("#currentgen a").eachText()

        return MangaInfo(
            title = title,
            cover = cover,
            description = description,
            author = authorBookSelector,
            genres = category,
            key = "",
        )
    }

    // chapters
    override fun chaptersRequest(manga: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(manga.key, "chapter-list/")
            headers { headers }
        }
    }

    override fun chaptersSelector() = "div.ch-list p a"

    override fun chapterFromElement(element: Element): ChapterInfo {
        val link = element.select("a").attr("href")
        val name = element.select("a").text()

        return ChapterInfo(name = name, key = link)
    }

    override suspend fun getChapters(manga: MangaInfo): List<ChapterInfo> {
        return super.getChapters(manga).reversed()
    }


    override fun pageContentParse(document: Document): List<String> {
        val title = document.select("h1.main-title").text()
        val content = document.select("div.par p").eachText()
        content.add(0, title)
        return content
    }

    override suspend fun getContents(chapter: ChapterInfo): List<String> {
        return pageContentParse(client.get<Document>(contentRequest(chapter)))
    }


    override fun contentRequest(chapter: ChapterInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(chapter.key)
            headers { headers }
        }
    }

    override fun searchRequest(page: Int, query: String, filters: FilterList): HttpRequestBuilder {
        return requestBuilder(baseUrl + fetchSearchEndpoint(page = page, query = query))
    }

    override suspend fun getSearch(page: Int, query: String, filters: FilterList): MangasPageInfo {
        return customJsonSearchParse(client.get<mtlSearchItem>(searchRequest(page, query, filters)))
    }

    private fun customJsonSearchParse(mtlSearchItem: mtlSearchItem): MangasPageInfo {
        val books = mutableListOf<MangaInfo>()
        mtlSearchItem.items.first { item ->
            item.results.forEach { res ->
                books.add(MangaInfo(title = Jsoup.parse(res.title).text(),
                    key = res.permalink,
                    author = res.cn,
                    cover = res.thumbnail))
            }
            return@first true
        }

        return MangasPageInfo(books, false)
    }


}