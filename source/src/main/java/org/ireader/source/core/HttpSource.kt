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

/**
 * A simple implementation for sources from a website.
 */
abstract class HttpSource(private val dependencies: Dependencies) : CatalogSource {

    abstract override val baseUrl: String

    open val versionId = 1

    open val client: HttpClient
        get() = dependencies.httpClients.default

    override val headers: Headers by lazy { headersBuilder().build() }

    abstract override val iconUrl: String

    override val id by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }

    override fun toString() = "$name (${lang.uppercase()})"

    override suspend fun getBookList(sort: Listing?, page: Int): BookPageInfo {
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

    override suspend fun getBookList(filters: FilterList, page: Int): BookPageInfo {
        throw Exception("not implemented")
    }


    open suspend fun getPopular(page: Int): BookPageInfo {
        val request = client.get<Document>(popularRequest(page))
        return popularParse(request)
    }


    open suspend fun getLatest(page: Int): BookPageInfo {
        val request = client.get<Document>(latestRequest(page))
        return latestParse(request)
    }


    override suspend fun getBookDetails(book: BookInfo): BookInfo {
        val request = client.get<Document>(detailsRequest(book))
        return detailParse(request)
    }


    override suspend fun getChapterList(book: BookInfo): List<ChapterInfo> {
        val request = client.get<Document>(chaptersRequest(book))
        return chaptersParse(request)
    }

    override suspend fun getContents(chapter: ChapterInfo): List<String> {
        val request = client.get<Document>(contentRequest(chapter))
        return pageContentParse(request)
    }

    override suspend fun getSearch(query: String, filters: FilterList, page: Int): BookPageInfo {
        val request = client.get<Document>(searchRequest(page, query, filters))
        return searchParse(request)
    }


    protected abstract fun popularRequest(page: Int): HttpRequestBuilder

    protected abstract fun latestRequest(page: Int): HttpRequestBuilder

    protected open fun detailsRequest(book: BookInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(book.key)
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

    protected open fun chaptersRequest(book: BookInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(book.key)
            headers { headers }
        }
    }


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


    protected abstract fun searchRequest(
        page: Int,
        query: String,
        filters: List<Filter<*>>,
    ): HttpRequestBuilder

    /****************************************************************************************************/

    abstract fun popularParse(
        document: Document,
    ): BookPageInfo


    abstract fun latestParse(
        document: Document,
    ): BookPageInfo


    abstract override fun detailParse(document: Document): BookInfo


    abstract override fun pageContentParse(
        document: Document,
    ): List<String>


    abstract override fun chaptersParse(document: Document): List<ChapterInfo>


    abstract fun searchParse(
        document: Document,
    ): BookPageInfo


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
