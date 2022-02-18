package org.ireader.domain.source.en.freewebnovel

import io.ktor.client.request.*
import kotlinx.coroutines.*
import okhttp3.Headers
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.FilterList
import org.ireader.domain.models.source.BooksPage
import org.ireader.domain.source.Dependencies
import org.ireader.domain.source.ParsedHttpSource
import org.ireader.source.models.BookInfo
import org.ireader.source.models.ChapterInfo
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class FreeWbNovel(deps: Dependencies) : ParsedHttpSource(deps) {

    override val name = "FreeWebNovel.com"
    override val creator: String = "@Kazem"
    override val iconUrl: String = "https://freewebnovel.com/static/freewebnovel/images/logo.png"

    override val baseUrl = "https://freewebnovel.com"

    override val lang = "en"

    override val supportsLatest = true
    override val supportsMostPopular: Boolean = true
    override val supportSearch: Boolean = true
    override fun getFilterList(): FilterList {
        return FilterList()
    }


    override fun fetchLatestEndpoint(page: Int): String? =
        "/latest-release-novel/$page/"

    override fun fetchPopularEndpoint(page: Int): String? =
        "/most-popular-novel/"

    override fun fetchSearchEndpoint(page: Int, query: String): String? =
        "/search/?searchkey=$query"


    override fun headersBuilder() = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        )
        add("Referer", baseUrl)
        add("cache-control", "max-age=0")
    }

    override val headers: Headers = headersBuilder().build()


    // popular
    override fun popularRequest(page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + fetchPopularEndpoint(page = page))
        }
    }

    override fun popularSelector() = "div.ul-list1 div.li-row"

    override fun popularFromElement(element: Element): BookInfo {
        val url = element.select("a").attr("href")
        val title = element.select("a").attr("title")
        val thumbnailUrl = element.select("img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun popularNextPageSelector() = null


    // latest

    override fun latestRequest(page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + fetchLatestEndpoint(page)!!)
            headers { headers }
        }
    }

    override fun latestSelector(): String = "div.ul-list1 div.li"


    override fun latestFromElement(element: Element): BookInfo {
        val title = element.select("div.txt a").attr("title")
        val url = element.select("div.txt a").attr("href").substringAfter(baseUrl)
        val thumbnailUrl = element.select("div.pic img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun latestNextPageSelector() = "div.ul-list1"

    override fun searchSelector() = "div.ul-list1 div.li-row"

    override fun searchFromElement(element: Element): BookInfo {
        val title = element.select("div.txt a").attr("title")
        val url = element.select("div.txt a").attr("href")
        val thumbnailUrl = element.select("div.pic img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun searchNextPageSelector(): String? = null


    // manga details
    override fun detailParse(document: Document): BookInfo {
        val title = document.select("div.m-desc h1.tit").text()
        val link = baseUrl + document.select("div.cur div.wp a:nth-child(5)").attr("href")
        val authorBookSelector = document.select("div.right a.a1").attr("title")
        val description = document.select("div.inner p").eachText().joinToString("\n")
        val category = document.select("div.item div.right a.a1").eachText()

        return BookInfo(
            title = title,
            description = description,
            author = authorBookSelector,
            genres = category,
            link = link
        )
    }

    // chapters
    override fun chaptersRequest(book: Book): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + book.link)
            headers { headers }
        }
    }

    override fun chaptersSelector() = "div.m-newest2 ul.ul-list5 li"

    override fun chapterFromElement(element: Element): ChapterInfo {
        val link = baseUrl + element.select("a").attr("href").substringAfter(baseUrl)
        val name = element.select("a").attr("title")

        return ChapterInfo(name = name, key = link)
    }

    fun uniqueChaptersRequest(book: Book, page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + book.link.replace("/${page - 1}.html", "").replace(".html", "")
                .plus("/$page.html"))
            headers { headers }
        }
    }

    override suspend fun getChapters(book: Book): List<ChapterInfo> {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                val page = client.get<Document>(chaptersRequest(book = book))
                val maxPage = parseMaxPage(book)
                val list = mutableListOf<Deferred<List<ChapterInfo>>>()
                for (i in 1..maxPage) {
                    val pChapters = async {
                        chaptersParse(client.get<Document>(uniqueChaptersRequest(book = book,
                            page = i)))
                    }
                    list.addAll(listOf(pChapters))
                }
                //  val request = client.get<Document>(chaptersRequest(book = book))

                return@withContext list.awaitAll().flatten()
            }
        }.getOrThrow()
    }

    suspend fun parseMaxPage(book: Book): Int {
        val page = client.get<Document>(chaptersRequest(book = book))
        val maxPage = page.select("#indexselect option").eachText().size
        return maxPage
    }


    override fun pageContentParse(document: Document): List<String> {
        return document.select("div.txt h4,p").eachText()
    }

    override suspend fun getContents(chapter: Chapter): List<String> {
        return pageContentParse(client.get<Document>(contentRequest(chapter)))
    }


    override fun contentRequest(chapter: Chapter): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(chapter.link)
            headers { headers }
        }
    }

    override fun searchRequest(page: Int, query: String, filters: FilterList): HttpRequestBuilder {
        return requestBuilder(baseUrl + fetchSearchEndpoint(page = page, query = query))
    }

    override suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage {
        return searchParse(client.get<Document>(searchRequest(page, query, filters)))
    }


}