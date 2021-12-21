package ir.kazemcodes.infinity.domain.network.models

import ir.kazemcodes.infinity.api_feature.data.BooksPage
import ir.kazemcodes.infinity.api_feature.data.ChapterPage
import ir.kazemcodes.infinity.api_feature.data.ChaptersPage
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.api_feature.network.InfinityInstance
import ir.kazemcodes.infinity.api_feature.network.NetworkHelper
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.gildor.coroutines.okhttp.await
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest

/**
 * A simple implementation for sources from a website.
 */
abstract class HttpSource : Source {


    protected val network: NetworkHelper = InfinityInstance.networkHelper

    /**
     * Base url of the website without the trailing slash, like: http://mysite.com
     */
    abstract override val baseUrl : String

    /**
     * Default network client for doing requests.
     */
    open val client: OkHttpClient
        get() = network.client


    /**
     * Headers used for requests.
     */
    val headers: Headers by lazy { headersBuilder().build() }

    /**
     * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
     * of the MD5 of the string: sourcename/language/versionId
     * Note the generated id sets the sign bit to 0.
     */
    override val id by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }.reduce(Long::or) and Long.MAX_VALUE
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


    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    abstract fun popularMangaSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract fun popularBookNextPageSelector(): String?

    /**
     * Returns the request for the popular books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun popularBookRequest(page: Int): Request


    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    abstract fun popularBookParse(response: Response): BooksPage

    /**
     * Returns an observable containing a page with a list of latest Book updates.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchLatestUpdates(page: Int): BooksPage {
        val response =  client.newCall(latestUpdatesRequest(page))
            .await()
        return  latestUpdatesParse(response)
    }
    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun latestUpdatesRequest(page: Int): Request

    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    abstract fun latestUpdatesParse(response: Response): BooksPage


    /**
     * Returns the request for the details of a Book. Override only if it's needed to change the
     * url, send different headers or request method like POST.
     *
     * @param Book the Book to be updated.
     */
    open fun bookDetailsRequest(Book: Book): Request {
        return GET(baseUrl + Book.link, headers)
    }

    /**
     * Parses the response from the site and returns the details of a book.
     *
     * @param response the response from the site.
     */
    abstract fun bookDetailsParse(response: Response): Book

    /**
     * Returns the request for updating the chapter list. Override only if it's needed to override
     * the url, send different headers or request method like POST.
     *
     * @param Book the Book to look for chapters.
     */
    open fun chapterListRequest(Book: Book): Request {
        return GET(baseUrl + Book.link, headers)
    }

    abstract fun chapterListRequest(Book: Book, page: Int): Request


    abstract fun hasNextChapterSelector() : String

    abstract fun hasNextChaptersParse(document: Document) : Boolean


    /**
     * Parses the response from the site and returns a list of chapters.
     *
     * @param response the response from the site.
     */
    abstract fun chapterListParse(response: Response): ChaptersPage

    /**
     * Returns the request for getting the page list. Override only if it's needed to override the
     * url, send different headers or request method like POST.
     *
     * @param chapter the chapter whose page list has to be fetched.
     */
    open fun pageContentRequest(chapter: Chapter): Request {
        return GET(baseUrl + chapter.link, headers)
    }

    /**
     * Parses the response from the site and returns a list of pages.
     *
     * @param response the response from the site.
     */
    abstract fun pageContentParse(response: Response): ChapterPage



    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    abstract fun searchMangaSelector(): String

    /**
     * Returns a manga from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [searchMangaSelector].
     */
    abstract fun searchMangaFromElement(element: Element): Book

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract fun searchMangaNextPageSelector(): String?

    /**
     * Returns the url of the given string without the scheme and domain.
     *
     * @param orig the full url.
     */
    private fun getUrlWithoutDomain(orig: String): String {
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
     * fields, like the title or the chapter number. Do not change anything to [Book].
     *
     * @param chapter the chapter to be added.
     * @param Book the Book of the chapter.
     */
    open fun prepareNewChapter(chapter: Chapter, Book: Book) {
    }

    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }
    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
}
