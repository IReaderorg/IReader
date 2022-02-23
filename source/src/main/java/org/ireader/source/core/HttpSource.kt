package org.ireader.source.core

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import okhttp3.Headers
import org.ireader.source.models.*
import org.jsoup.nodes.Document
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

/**
 * A simple implementation for sources from a website.
 */
abstract class HttpSource(private val dependencies: Dependencies) : Source {


    /**
     * Base url of the website without the trailing slash, like: http://mysite.com
     */
    abstract override val baseUrl: String


    /**
     * Version id used to generate the source id. If the site completely changes and urls are
     * incompatible, you may increase this value and it'll be considered as a new source.
     */
    open val versionId = 1


    /**
     * Default network client for doing requests.
     */
    open val client: HttpClient
        get() = dependencies.httpClients.default


    /**
     * Headers used for requests.
     */
    override val headers: Headers by lazy { headersBuilder().build() }

    /**
     * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
     * of the MD5 of the string: source name/language/versionId
     * Note the generated id sets the sign bit to 0.
     */
    override val id by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }


    /**
     * Visible name of the source.
     */
    override fun toString() = "$name (${lang.uppercase()})"

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
    /**
     * Returns a page with a list of book. Normally it's not needed to
     * override this method.
     * @param page the page number to retrieve.
     */
    override suspend fun getPopular(page: Int): MangasPageInfo {
        val request = client.get<Document>(popularRequest(page))
        return popularParse(request)
    }

    /**
     * Returns a page with a list of latest Book updates.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun getLatest(page: Int): MangasPageInfo {
        val request = client.get<Document>(latestRequest(page))
        return latestParse(request)
    }

    /**
     * Returns a book. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun getMangaDetails(manga: MangaInfo): MangaInfo {
        val request = client.get<Document>(detailsRequest(manga))
        return detailParse(request)
    }

    /**
     * Returns a list of chapter. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param manga the chapters to retrieve.
     */
    override suspend fun getChapters(manga: MangaInfo): List<ChapterInfo> {
        val request = client.get<Document>(chaptersRequest(manga))
        return chaptersParse(request)
    }

    /**
     * Returns a ChapterPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun getContents(chapter: ChapterInfo): List<String> {
        val request = client.get<Document>(contentRequest(chapter))
        return pageContentParse(request)
    }

    /**
     * Returns a BooksPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param query the search query to retrieve.
     */
    override suspend fun getSearch(page: Int, query: String, filters: FilterList): MangasPageInfo {
        val request = client.get<Document>(searchRequest(page, query, filters))
        return searchParse(request)
    }


    /**
     * Returns the request for the popular books given the page.
     *
     * @param page the page number to retrieve.
     */
    protected abstract fun popularRequest(page: Int): HttpRequestBuilder

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    protected abstract fun latestRequest(page: Int): HttpRequestBuilder


    /**
     * Returns the request for the details of a Book. Override only if it's needed to change the
     * url, send different headers or request method like POST.
     *
     * @param manga the Book to be updated.
     */
    protected open fun detailsRequest(manga: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(manga.key)
            headers { headers }
        }
    }

    protected open fun requestBuilder(
        url: String,
        mHeaders: Headers = headers,
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(url)
            headers { headers }
        }
    }

    /**
     * Returns the request for updating the chapter list. Override only if it's needed to override
     * the url, send different headers or request method like POST.
     *
     * @param manga the Book to look for chapters.
     */
    protected open fun chaptersRequest(manga: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(manga.key)
            headers { headers }
        }
    }


    /**
     * Returns the request for getting the page list. Override only if it's needed to override the
     * url, send different headers or request method like POST.
     *
     * @param chapter the chapter whose page list has to be fetched.
     */
    @OptIn(InternalAPI::class)
    protected open fun contentRequest(chapter: ChapterInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(chapter.key)
            headers {
                append(HttpHeaders.Accept, "text/html")
                append(HttpHeaders.Authorization, "token")
                append(HttpHeaders.UserAgent, "ktor client")
                headers
            }.build()
        }
    }

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    protected abstract fun searchRequest(
        page: Int,
        query: String,
        filters: FilterList,
    ): HttpRequestBuilder

    /****************************************************************************************************/


    /**
     * Parses the response from the site and returns a [MangasPageInfo] object.
     *
     * @param response the response from the site.
     */
    abstract override fun popularParse(
        document: Document,
    ): MangasPageInfo


    /**
     * Parses the document from the site and returns a [MangasPageInfo] object.
     *
     * @param response the response from the site.
     */
    abstract override fun latestParse(
        document: Document,
    ): MangasPageInfo


    /**
     * Returns the details of the Book from the given [document].
     *
     * @param document the parsed document.
     */
    abstract override fun detailParse(document: Document): MangaInfo


    abstract override fun pageContentParse(
        document: Document,
    ): List<String>


    /**
     * Parses the response from the site and returns a list of chapters.
     *
     * @param response the response from the site.
     */
    abstract override fun chaptersParse(document: Document): List<ChapterInfo>


    abstract override fun searchParse(
        document: Document,
    ): MangasPageInfo


    /**
     * Returns the url of the given string without the scheme and domain.
     *
     * @param orig the full url.
     */
    fun getUrlWithoutDomain(orig: String): String {
        return try {
            val uri = URI(orig.replace(" ", "%20"))
            var out = uri.path
            if (uri.query != null) {
                out += "?" + uri.query
            }
            if (uri.fragment != null) {
                out += "#" + uri.fragment
            }
            out
        } catch (e: URISyntaxException) {
            orig
        }
    }

    open fun getImageRequest(page: ImageUrl): Pair<HttpClient, HttpRequestBuilder> {
        return client to HttpRequestBuilder().apply {
            url(page.url)
        }
    }

    open fun getCoverRequest(url: String): Pair<HttpClient, HttpRequestBuilder> {
        return client to HttpRequestBuilder().apply {
            url(url)
        }
    }

    override fun getListings(): List<Listing> {
        return emptyList()
    }

    protected open fun headersBuilder() = okhttp3.Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0"
    }
}
