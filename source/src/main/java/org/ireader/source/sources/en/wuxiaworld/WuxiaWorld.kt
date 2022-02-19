package org.ireader.source.sources.en.wuxiaworld

import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.ireader.source.core.Dependencies
import org.ireader.source.core.ParsedHttpSource
import org.ireader.source.models.BookInfo
import org.ireader.source.models.BooksPage
import org.ireader.source.models.ChapterInfo
import org.ireader.source.models.FilterList
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

class WuxiaWorld(deps: Dependencies) : ParsedHttpSource(deps) {

    override val name = "WuxiaWorld.site"
    override val creator: String = "@Kazem"
    override val iconUrl: String =
        "https://wuxiaworld.site/wp-content/uploads/2019/02/WuxiaWorld-e1567126455773.png"

    override val baseUrl = "https://wuxiaworld.site"

    override val lang = "en"

    override val supportsLatest = true
    override val supportsMostPopular: Boolean = true
    override val supportSearch: Boolean = true
    override fun getFilterList(): FilterList {
        return FilterList()
    }


    override fun fetchLatestEndpoint(page: Int): String? =
        "/novel-list/page/$page/"

    override fun fetchPopularEndpoint(page: Int): String? =
        "/novel-list/page/$page/?m_orderby=views"

    override fun fetchSearchEndpoint(page: Int, query: String): String? =
        "/?s=$query&post_type=wp-manga&op=&author=&artist=&release=&adult="


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

    override fun popularSelector() = "div.page-item-detail"

    override fun popularFromElement(element: Element): BookInfo {
        val title = element.select("h3.h5 a").text()
        val url = element.select("h3.h5 a").attr("href")
        val thumbnailUrl = element.select("img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun popularNextPageSelector() = "div.nav-previous>a"


    // latest

    override fun latestRequest(page: Int): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(baseUrl + fetchLatestEndpoint(page)!!)
            headers { headers }
        }
    }

    override fun latestSelector(): String = "div.page-item-detail"


    override fun latestFromElement(element: Element): BookInfo {
        val title = element.select("h3.h5 a").text()
        val url = element.select("h3.h5 a").attr("href")
        val thumbnailUrl = element.select("img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun latestNextPageSelector() = popularNextPageSelector()

    override fun searchSelector() = "div.c-tabs-item__content"

    override fun searchFromElement(element: Element): BookInfo {
        val title = element.select("div.post-title h3.h4 a").text()
        val url = element.select("div.post-title h3.h4 a").attr("href")
        val thumbnailUrl = element.select("img").attr("src")
        return BookInfo(link = url, title = title, cover = thumbnailUrl)
    }

    override fun searchNextPageSelector(): String? = null


    // manga details
    override fun detailParse(document: Document): BookInfo {
        val title = document.select("div.post-title>h1").text()
        val cover = document.select("div.summary_image a img").attr("src")
        val link = baseUrl + document.select("div.cur div.wp a:nth-child(5)").attr("href")
        val authorBookSelector = document.select("div.author-content>a").attr("title")
        val description =
            document.select("div.description-summary div.summary__content p").eachText()
                .joinToString("\n\n")
        val category = document.select("div.genres-content a").eachText()
        val rating = document.select("div.post-rating span.score").text()
        val status = document.select("div.post-status div.summary-content").text()


        return BookInfo(
            title = title,
            cover = cover,
            description = description,
            author = authorBookSelector,
            genres = category,
            link = link,
            rating = paresRating(rating),
            status = parseStatus(status)
        )
    }

    private fun parseStatus(string: String): Int {
        return when {
            "OnGoing" in string -> BookInfo.ONGOING
            "Completed" in string -> BookInfo.COMPLETED
            else -> BookInfo.UNKNOWN
        }
    }

    private fun paresRating(string: String): Int {
        return when {
            "1" in string -> 1
            "2" in string -> 2
            "3" in string -> 3
            "4" in string -> 4
            "5" in string -> 5
            else -> {
                0
            }
        }
    }

    // chapters
    override fun chaptersRequest(book: BookInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(book.link)
            headers { headers }
        }
    }

    override fun chaptersSelector() = "li.wp-manga-chapter"

    override fun chapterFromElement(element: Element): ChapterInfo {
        val link = baseUrl + element.select("a").attr("href").substringAfter(baseUrl)
        val name = element.select("a").text()
        val dateUploaded = element.select("i").text()

        return ChapterInfo(name = name, key = link, dateUpload = parseChapterDate(dateUploaded))
    }

    fun parseChapterDate(date: String): Long {
        return if (date.contains("ago")) {
            val value = date.split(' ')[0].toInt()
            when {
                "min" in date -> Calendar.getInstance().apply {
                    add(Calendar.MINUTE, value * -1)
                }.timeInMillis
                "hour" in date -> Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, value * -1)
                }.timeInMillis
                "day" in date -> Calendar.getInstance().apply {
                    add(Calendar.DATE, value * -1)
                }.timeInMillis
                "week" in date -> Calendar.getInstance().apply {
                    add(Calendar.DATE, value * 7 * -1)
                }.timeInMillis
                "month" in date -> Calendar.getInstance().apply {
                    add(Calendar.MONTH, value * -1)
                }.timeInMillis
                "year" in date -> Calendar.getInstance().apply {
                    add(Calendar.YEAR, value * -1)
                }.timeInMillis
                else -> {
                    0L
                }
            }
        } else {
            try {
                dateFormat.parse(date)?.time ?: 0
            } catch (_: Exception) {
                0L
            }
        }
    }

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("MMM dd,yyyy", Locale.US)
    override suspend fun getChapters(book: BookInfo): List<ChapterInfo> {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                var chapters =
                    chaptersParse(client.post<Document>(requestBuilder(book.link + "ajax/chapters/")))
                if (chapters.isEmpty()) {
                    chapters = chaptersParse(client.post<Document>(requestBuilder(book.link)))
                }
                return@withContext chapters.reversed()
            }
        }.getOrThrow()
    }


    override fun pageContentParse(document: Document): List<String> {
        return document.select("div.read-container h3,p").eachText()
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

    override suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage {
        return searchParse(client.get<Document>(searchRequest(page, query, filters)))
    }


}