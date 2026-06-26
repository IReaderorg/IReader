package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import tachiyomi.core.common.util.lang.awaitSingle
import uy.kohesive.injekt.injectLazy
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest

/**
 * Minimal HttpSource abstract class shim for tsundoku extension compatibility.
 *
 * This provides the base class that tsundoku extensions extend.
 * Uses Injekt shim to get NetworkHelper for HTTP requests.
 */
abstract class HttpSource : CatalogueSource {

    /**
     * Network service — obtained from Injekt shim.
     */
    protected val network: NetworkHelper by injectLazy()

    /**
     * Base url of the website without the trailing slash.
     */
    abstract val baseUrl: String

    /**
     * Version id used to generate the source id.
     */
    open val versionId = 1

    /**
     * Default OkHttp client for HTTP requests.
     */
    open val client: OkHttpClient
        get() = network.client

    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        generateSourceId(key)
    }

    override val lang: String get() = ""

    val headers: Headers
        get() = headersBuilder().build()

    open fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", DEFAULT_USER_AGENT)

    override suspend fun getPopularManga(page: Int): MangasPage {
        return fetchPopularManga(page).awaitSingle()
    }

    override suspend fun getLatestUpdates(page: Int): MangasPage {
        return fetchLatestUpdates(page).awaitSingle()
    }

    override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage {
        return fetchSearchManga(page, query, filters).awaitSingle()
    }

    override suspend fun getMangaDetails(manga: SManga): SManga {
        return fetchMangaDetails(manga).awaitSingle()
    }

    override suspend fun getChapterList(manga: SManga): List<SChapter> {
        return fetchChapterList(manga).awaitSingle()
    }

    override suspend fun getPageList(chapter: SChapter): List<Page> {
        return fetchPageList(chapter).awaitSingle()
    }

    // Default fetch implementations — extensions override popularMangaRequest/Parse etc.

    open fun fetchPopularManga(page: Int): Observable<MangasPage> {
        return client.newCall(popularMangaRequest(page)).asObservableSuccess()
            .map { popularMangaParse(it) }
    }

    open fun fetchLatestUpdates(page: Int): Observable<MangasPage> {
        return client.newCall(latestUpdatesRequest(page)).asObservableSuccess()
            .map { latestUpdatesParse(it) }
    }

    open fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return client.newCall(searchMangaRequest(page, query, filters)).asObservableSuccess()
            .map { searchMangaParse(it) }
    }

    open fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return client.newCall(mangaDetailsRequest(manga)).asObservableSuccess()
            .map { mangaDetailsParse(it, manga) }
    }

    open fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return client.newCall(chapterListRequest(manga)).asObservableSuccess()
            .map { chapterListParse(it) }
    }

    open fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        return client.newCall(pageListRequest(chapter)).asObservableSuccess()
            .map { pageListParse(it) }
    }

    // Protected request/parse methods that extensions override

    protected open fun popularMangaRequest(page: Int): Request = throw UnsupportedOperationException()
    protected open fun popularMangaParse(response: Response): MangasPage = throw UnsupportedOperationException()
    protected open fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException()
    protected open fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException()
    protected open fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw UnsupportedOperationException()
    protected open fun searchMangaParse(response: Response): MangasPage = throw UnsupportedOperationException()
    protected open fun mangaDetailsRequest(manga: SManga): Request = throw UnsupportedOperationException()
    protected open fun mangaDetailsParse(response: Response, manga: SManga): SManga = throw UnsupportedOperationException()
    protected open fun chapterListRequest(manga: SManga): Request = throw UnsupportedOperationException()
    protected open fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException()
    protected open fun pageListRequest(chapter: SChapter): Request = throw UnsupportedOperationException()
    protected open fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException()

    // Utility methods

    protected fun GET(url: String, headers: Headers = this.headers): Request = GET(url, headers)

    protected fun sanitizeUri(uri: URI): URI {
        return try {
            URI(uri.scheme, uri.userInfo, uri.host, uri.port, uri.path, uri.query, uri.fragment)
        } catch (e: URISyntaxException) {
            uri
        }
    }

    override fun toString(): String = "$name (${lang.uppercase()})"

    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        fun generateSourceId(key: String): Long {
            val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
            return (bytes[0].toLong() and 0xFFL) or
                ((bytes[1].toLong() and 0xFFL) shl 8) or
                ((bytes[2].toLong() and 0xFFL) shl 16) or
                ((bytes[3].toLong() and 0xFFL) shl 24) or
                ((bytes[4].toLong() and 0xFFL) shl 32) or
                ((bytes[5].toLong() and 0xFFL) shl 40) or
                ((bytes[6].toLong() and 0xFFL) shl 48) or
                ((bytes[7].toLong() and 0x7FL) shl 56)
        }
    }
}
