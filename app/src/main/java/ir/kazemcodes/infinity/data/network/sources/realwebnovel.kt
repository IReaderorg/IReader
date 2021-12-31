package ir.kazemcodes.infinity.data.network.sources

import android.content.Context
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.data.network.models.ChapterPage
import ir.kazemcodes.infinity.data.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class RealWebNovelApi(context: Context ) : ParsedHttpSource(context) {

    override val baseUrl: String = "https://readwebnovels.net"
    override val name: String = "RealWebNovels.net"

    override val lang: String = "en"

    override val supportsLatest: Boolean = true

    override val client: OkHttpClient = super.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 "
        )
        add("Referer", baseUrl)
    }

    override val supportsMostPopular: Boolean
        get() = true

    override fun popularBookSelector(): String = "div.page-item-detail"

    override fun popularBookNextPageSelector(): String? = "div.nav-previous"

    override fun popularBookRequest(page: Int): Request =
        GET("$baseUrl/manga-2/page/$page/?m_orderby=trending")

    override fun popularBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.link = element.select("a").attr("href").substringAfter(baseUrl)
        book.bookName = element.select("a").attr("title")
        book.coverLink = element.select("img").attr("src")
        book.source = this.name
        return book
    }

    override fun latestUpdatesSelector(): String = "div.page-item-detail"

    override fun latestUpdatesFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.bookName = element.select("a").attr("title")
        book.link = element.select("a").attr("href").substringAfter(baseUrl)
        book.coverLink = element.select("img").attr("src")
        book.source = this.name
        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = "div.nav-previous"

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/manga-2/page/$page/?m_orderby=latest")


    override fun bookDetailsParse(document: Document): Book {
        val book = Book.create()
        book.bookName = document.select("div.post-title h1").text()
        book.description = document.select("div.summary__content").eachText().joinToString("\n\n\n")
        book.author = document.select("div.author-content a").text()
        book.category =
            document.select("div.genres-content a").eachText().drop(1).map { value -> value.trim() }
                .joinToString(" - ")
        book.source = name
        return book
    }

    override fun hasNextChapterSelector(): String? = null

    override fun hasNextChaptersParse(document: Document): Boolean {
        return false
    }


    override fun chapterListRequest(book: Book, page: Int): Request = GET(baseUrl + book.link)

    override fun chapterListSelector(): String = "li.wp-manga-chapter"

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()
        chapter.title = element.select("a").text()
        chapter.link = element.select("a").attr("href")

        return chapter
    }

    override fun chapterListNextPageSelector(): String? = null


    override fun pageContentParse(document: Document): ChapterPage {
        val content = document.select("div.reading-content h4,p").eachText().joinToString("\n\n\n")

        return ChapterPage(content)
    }

    override fun searchBookSelector(): String = "div.c-tabs-item__content"

    override fun searchBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.link = element.select("div.tab-thumb a").attr("href").substringAfter(baseUrl)
        book.bookName = element.select("h4").text()
        book.coverLink = element.select("div.tab-thumb a img").attr("src")
        return book
    }

    override fun searchBookRequest(page: Int, query: String): Request =
        GET("$baseUrl/?s=$query&post_type=wp-manga")

    override fun searchBookNextPageSelector(): String? = null
}


