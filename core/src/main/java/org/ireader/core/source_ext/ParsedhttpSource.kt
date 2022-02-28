package org.ireader.core.source_ext

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import okhttp3.Headers
import okhttp3.OkHttpClient
import org.ireader.core.LatestListing
import org.ireader.core.PopularListing
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tachiyomi.source.Dependencies
import tachiyomi.source.HttpSource
import tachiyomi.source.model.*
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/** Taken from https://tachiyomi.org/ **/
abstract class ParsedHttpSource(private val dependencies: Dependencies) : HttpSource(dependencies) {


    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }


    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        if (sort == null) {
            throw Exception("sort can not be empty.")
        }
        return when (sort) {
            is LatestListing -> getLatest(page)
            is PopularListing -> getPopular(page)
            else -> {
                throw Exception("no sort was found")
            }
        }
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filter { it.name == "query" }.first().value
        if (query != null && query is String) {
            client.get<String>(searchRequest(page = page, query, filters))
            val request = client.get<String>(searchRequest(page = page, query, filters)).parseHtml()
            return searchParse(request)
        } else {
            throw Exception("Query must not be empty")
        }

    }

    override val client: HttpClient
        get() = HttpClient(OkHttp) {
            BrowserUserAgent()
            engine {
                preconfigured = clientBuilder()
            }
        }

    fun clientBuilder(): OkHttpClient = OkHttpClient()
        .newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun headersBuilder() = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        )
        add("cache-control", "max-age=0")
    }

    open val headers: Headers = headersBuilder().build()


    protected open fun requestBuilder(
        url: String,
        mHeaders: Headers = headers,
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(url)
            headers { headers }
        }
    }


    protected abstract fun popularRequest(page: Int): HttpRequestBuilder

    protected abstract fun latestRequest(page: Int): HttpRequestBuilder

    protected open fun detailRequest(manga: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(manga.key)
            headers { headers }
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo): MangaInfo {
        return detailParse(client.get<String>(detailRequest(manga)).parseHtml())
    }

    open fun chaptersRequest(book: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(book.key)
            headers { headers }
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo): List<Page> {
        return getContents(chapter).map { Text(it) }
    }

    open suspend fun getContents(chapter: ChapterInfo): List<String> {
        return pageContentParse(client.get<String>(contentRequest(chapter)).parseHtml())
    }

    protected abstract fun searchRequest(
        page: Int,
        query: String,
        filters: List<Filter<*>>,
    ): HttpRequestBuilder

    open fun contentRequest(chapter: ChapterInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(chapter.key)
            headers { headers }
        }
    }

    fun String.parseHtml(): Document {
        return Jsoup.parse(this)
    }

    open suspend fun getLatest(page: Int): MangasPageInfo {
        val request = client.get<String>(latestRequest(page)).parseHtml()
        return latestParse(request)
    }

    open suspend fun getPopular(page: Int): MangasPageInfo {
        val request = client.get<String>(popularRequest(page)).parseHtml()
        return popularParse(request)
    }

    open suspend fun getSearch(query: String, filters: FilterList, page: Int): MangasPageInfo {
        val request = client.get<String>(searchRequest(page, query, filters)).parseHtml()
        return searchParse(request)
    }

    /****************************************************************************************************/
    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularSelector].
     */
    abstract fun popularFromElement(element: Element): MangaInfo

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestSelector].
     */
    abstract fun latestFromElement(element: Element): MangaInfo

    /**
     * Returns a chapter from the given element.
     *
     * @param element an element obtained from [chaptersSelector].
     */
    abstract fun chapterFromElement(element: Element): ChapterInfo

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [searchSelector].
     */
    abstract fun searchFromElement(element: Element): MangaInfo

    /****************************************************************************************************/
    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun popularSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract fun popularNextPageSelector(): String?

    open fun popularParse(document: Document): MangasPageInfo {
        val books = document.select(popularSelector()).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun latestSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract fun latestNextPageSelector(): String?
    open fun latestParse(document: Document): MangasPageInfo {

        val books = document.select(latestSelector()).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun chaptersSelector(): String


    open fun chaptersParse(document: Document): List<ChapterInfo> {
        return document.select(chaptersSelector()).map { chapterFromElement(it) }
    }


    abstract fun pageContentParse(
        document: Document,
    ): List<String>

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun searchSelector(): String

    protected abstract fun searchNextPageSelector(): String?


    open fun searchParse(document: Document): MangasPageInfo {
        /**
         * I Add Filter Because sometimes this value contains null values
         * so the null book shows in search screen
         */
        val books = document.select(searchSelector()).map { element ->
            searchFromElement(element)
        }.filter {
            it.title.isNotBlank()
        }
        val hasNextPage = searchNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books,
            hasNextPage)
    }

    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     *return the end point for the fetch latest books updates feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract fun fetchLatestEndpoint(page: Int): String?

    /**
     *return the end point for the  fetch Popular books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract fun fetchPopularEndpoint(page: Int): String?

    /**
     *return the end point for the fetch Search feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     * note: use "{query}" in the endpoint instead of query
     */
    abstract fun fetchSearchEndpoint(page: Int, query: String): String?


    /****************************************************************************************************/


    abstract fun detailParse(document: Document): MangaInfo


}