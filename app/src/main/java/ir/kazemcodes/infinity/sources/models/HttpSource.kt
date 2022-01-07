package ir.kazemcodes.infinity.data.network.models

import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.data.network.utils.NetworkHelper
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.util.applyPageFormat
import ir.kazemcodes.infinity.util.applySearchFormat
import ir.kazemcodes.infinity.util.asJsoup
import okhttp3.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.kodein.di.*
import org.kodein.di.android.closestDI
import ru.gildor.coroutines.okhttp.await
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

/**
 * A simple implementation for sources from a website.
 */
abstract class HttpSource(context: Context) : Source, DIAware {


    final override val di: DI by closestDI(context)

    protected val network: NetworkHelper by di.instance<NetworkHelper>()


    /**
     * Base url of the website without the trailing slash, like: http://mysite.com
     */
    abstract override val baseUrl: String

    /**
     * Default network client for doing requests.
     */
    open val client: OkHttpClient
        get() = network.client

    val webView = WebView(context)


    /**
     * Headers used for requests.
     */
    val headers: Headers by lazy { headersBuilder().build() }

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
     * Version id used to generate the source id. If the site completely changes and urls are
     * incompatible, you may increase this value and it'll be considered as a new source.
     */
    open val versionId = 1

    /**
     * Visible name of the source.
     */
    override fun toString() = "$name (${lang.uppercase()})"

    val pageFormat: String = "{page}"
    val searchQueryFormat: String = "{query}"
    /****************************************************************************************************/

    /**
     *return the end point for the fetch latest books updates feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchLatestEndpoint: String?

    /**
     *return the end point for the  fetch Popular books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchPopularEndpoint: String?

    /**
     *return the end point for the fetch Search feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     * note: use "{query}" in the endpoint instead of query
     */
    abstract val fetchSearchEndpoint: String?

    /**
     *return the end point for the fetch Chapters books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchChaptersEndpoint: String?

    /**
     *return the end point for the fetch Content  books feature,
     * if there is not endpoint just return null
     */
    abstract val fetchContentEndpoint: String?

    /****************************************************************************************************/
    /**
     * Returns a page with a list of book. Normally it's not needed to
     * override this method.
     * @param page the page number to retrieve.
     */
    override suspend fun fetchPopular(page: Int): BooksPage {
        var books = popularParse(client.newCall(popularRequest(page)).await(), page = page)
        if (books.isCloudflareEnabled) {
            books =
                popularParse(document = network.getHtmlFromWebView(baseUrl + fetchPopularEndpoint?.applyPageFormat(page)), page = page)
        }
        return books
    }
    /**
     * Returns a page with a list of latest Book updates.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchLatest(page: Int): BooksPage {
        var books =
            latestParse(client.newCall(latestRequest(page)).await(), page = page)
        if (books.isCloudflareEnabled) {
            books =
                latestParse(network.getHtmlFromWebView(baseUrl + fetchLatestEndpoint?.applyPageFormat(page)), page = page)
        }

        return books
    }
    /**
     * Returns a book. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchBook(book: Book): BookPage {

        var Completebook = detailParse(client.newCall(detailsRequest(book)).await())
        if (Completebook.isCloudflareEnabled) {
            Completebook = detailParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(book.link)))
        }
        return Completebook
    }

    /**
     * Returns a list of chapter. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param book the chapters to retrieve.
     */
    override suspend fun fetchChapters(book: Book, page: Int): ChaptersPage {
        var chapters = chapterListParse(client.newCall(chaptersRequest(book, page)).await())
        if (chapters.isCloudflareEnabled) {
            chapters = chaptersParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(book.link)))
        }

        return chapters
    }

    /**
     * Returns a ChapterPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchContent(chapter: Chapter): ChapterPage {
        var content = pageContentParse(client.newCall(contentRequest(chapter)).await())

        if (content.isCloudflareEnabled) {
            content = contentParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(chapter.link)))
        }
        return content
    }

    /**
     * Returns a BooksPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param query the search query to retrieve.
     */
    override suspend fun fetchSearch(page: Int, query: String): BooksPage {
        var books = searchBookParse(client.newCall(searchRequest(page, query)).await(), page)
        if (books.isCloudflareEnabled) {
            books =
                searchParse(network.getHtmlFromWebView(baseUrl + fetchSearchEndpoint?.applySearchFormat(query,page)), page = page)
        }
        return books
    }
    /****************************************************************************************************/

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each Book.
     */
    abstract val  popularSelector: String?

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each Book.
     */
    abstract val searchSelector: String?

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each Book.
     */
    abstract val latestSelector: String?

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each chapter.
     */
    abstract val chaptersSelector: String?

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract val latestUpdatesNextPageSelector: String?

    /****************************************************************************************************/

    abstract fun hasNextChapterSelector(): String?


    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract fun popularBookNextPageSelector(): String?

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract fun searchBookNextPageSelector(): String?

    open fun searchBookNextValuePageSelector(): String? = null

    /****************************************************************************************************/

    /**
     * Returns the request for the popular books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun popularRequest(page: Int): Request

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun latestRequest(page: Int): Request


    /**
     * Returns the request for the details of a Book. Override only if it's needed to change the
     * url, send different headers or request method like POST.
     *
     * @param book the Book to be updated.
     */
    open fun detailsRequest(book: Book): Request {
        return GET(baseUrl + getUrlWithoutDomain(book.link), headers)
    }

    /**
     * Returns the request for updating the chapter list. Override only if it's needed to override
     * the url, send different headers or request method like POST.
     *
     * @param book the Book to look for chapters.
     */
    open fun chaptersRequest(book: Book): Request {
        return GET(baseUrl + getUrlWithoutDomain(book.link), headers)
    }

    abstract fun chaptersRequest(book: Book, page: Int): Request


    /**
     * Returns the request for getting the page list. Override only if it's needed to override the
     * url, send different headers or request method like POST.
     *
     * @param chapter the chapter whose page list has to be fetched.
     */
    open fun contentRequest(chapter: Chapter): Request {
        return GET(baseUrl + getUrlWithoutDomain(chapter.link), headers)
    }

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun searchRequest(page: Int, query: String): Request

    /****************************************************************************************************/


    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    private fun popularParse(response: Response, page: Int): BooksPage {
        return popularParse(response.asJsoup(), page)
    }

    abstract fun popularParse(document: Document, page: Int): BooksPage


    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    fun latestParse(response: Response, page: Int): BooksPage {
        return latestParse(response.asJsoup(), page = page)
    }

    /**
     * Parses the document from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    abstract fun latestParse(document: Document, page: Int): BooksPage


    /**
     * Parses the response from the site and returns the details of a book.
     *
     * @param response the response from the site.
     */
    fun detailParse(response: Response): BookPage {
        return detailParse(response.asJsoup())
    }

    /**
     * Returns the details of the Book from the given [document].
     *
     * @param document the parsed document.
     */
    abstract fun detailParse(document: Document): BookPage


    /**
     * Parses the response from the site and returns a list of pages.
     *
     * @param response the response from the site.
     */
    fun pageContentParse(response: Response): ChapterPage {
        return contentParse(response.asJsoup())
    }

    abstract fun contentParse(document: Document): ChapterPage


    /**
     * Parses the response from the site and returns a list of chapters.
     *
     * @param response the response from the site.
     */
    abstract fun chaptersParse(document: Document): ChaptersPage

    fun chapterListParse(response: Response): ChaptersPage {
        return chaptersParse(response.asJsoup())
    }

    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    fun searchBookParse(response: Response, page: Int): BooksPage {
        return searchParse(response.asJsoup(), page = page)
    }

    abstract fun searchParse(document: Document, page: Int): BooksPage

    abstract fun hasNextChaptersParse(document: Document): Boolean





    /****************************************************************************************************/


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

    /**
     * Called before inserting a new chapter into database. Use it if you need to override chapter
     * fields, like the title or the chapter number. Do not change anything to [book].
     *
     * @param chapter the chapter to be added.
     * @param book the Book of the chapter.
     */
    open fun prepareNewChapter(chapter: Chapter, book: Book) {
    }


    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
}
