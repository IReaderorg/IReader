package ireader.core.source

import androidx.annotation.Keep
import io.ktor.client.request.*
import io.ktor.http.*
import ireader.core.http.DEFAULT_USER_AGENT
import ireader.core.source.model.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.security.MessageDigest

/** Taken from https://tachiyomi.org/ **/
@Keep
abstract class ParsedHttpSource(private val dependencies: ireader.core.source.Dependencies) : HttpSource(dependencies) {

    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }

    open fun getUserAgent() =
            DEFAULT_USER_AGENT
    open fun HttpRequestBuilder.headersBuilder(
        block: HeadersBuilder.() -> Unit = {
            append(HttpHeaders.UserAgent, getUserAgent())
            append(HttpHeaders.CacheControl, "max-age=0")
        }
    ) {
        headers(block)
    }

    fun requestBuilder(
        url: String,
        block: HeadersBuilder.() -> Unit = {
            append(HttpHeaders.UserAgent, getUserAgent())
            append(HttpHeaders.CacheControl, "max-age=0")
        }
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(url)
            headers(block)
        }
    }

    protected open fun detailRequest(manga: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(manga.key)
            headersBuilder()
        }
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        return detailParse(client.get(detailRequest(manga)).asJsoup())
    }

    open fun chaptersRequest(book: MangaInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(book.key)
            headersBuilder()
        }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return getContents(chapter).map { Text(it) }
    }

    open suspend fun getContents(chapter: ChapterInfo): List<String> {
        return pageContentParse(client.get(contentRequest(chapter)).asJsoup())
    }

    open fun contentRequest(chapter: ChapterInfo): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(chapter.key)
            headersBuilder()
        }
    }

    abstract fun chapterFromElement(element: Element): ChapterInfo

    fun bookListParse(document: Document, elementSelector: String, nextPageSelector: String?, parser: (element: Element) -> MangaInfo): MangasPageInfo {
        val books = document.select(elementSelector).map { element ->
            parser(element)
        }

        val hasNextPage = nextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
    }

    abstract fun chaptersSelector(): String

    open fun chaptersParse(document: Document): List<ChapterInfo> {
        return document.select(chaptersSelector()).map { chapterFromElement(it) }
    }

    abstract fun pageContentParse(
        document: Document,
    ): List<String>

    abstract fun detailParse(document: Document): MangaInfo
}
