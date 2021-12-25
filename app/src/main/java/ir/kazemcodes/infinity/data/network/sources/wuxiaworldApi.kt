package ir.kazemcodes.infinity.data.network.sources

import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.api_feature.network.POST
import ir.kazemcodes.infinity.data.network.models.BooksPage
import ir.kazemcodes.infinity.data.network.models.ChapterPage
import ir.kazemcodes.infinity.data.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.gildor.coroutines.okhttp.await

class WuxiaWorldApi : ParsedHttpSource() {

    override val baseUrl: String = "https://wuxiaworld.site"
    override val name: String = "WuxiaWorld.site"

    override val lang: String = "en"

    override val supportsLatest: Boolean = true

    override suspend fun fetchPopular(page: Int): BooksPage {
        val req = popularBookRequest(page)
        val res = client.newCall(req).await()
        return popularBookParse(res)
    }

    override suspend fun fetchSearchBook(page: Int, query: String): BooksPage {
        val req = searchBookRequest(page, query = query)
        val res = client.newCall(req).await()
        return searchBookParse(res)
    }


    override suspend fun fetchContent(chapter: Chapter): ChapterPage {
        val req = pageContentRequest(chapter)
        val res = client.newCall(req).await()

        return pageContentParse(res)
    }

    override suspend fun fetchBook(book: Book): Book {
        val req = bookDetailsRequest(book)
        val res = client.newCall(req).await()
        return bookDetailsParse(res)
    }


    override suspend fun fetchChapters(book: Book): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        var page: Int = 1
        val req = chapterListRequest(book, page)
        val res = client.newCall(req).await()
        val content = chapterListParse(res)
        chapters.addAll(content.chapters)
        page += 1
        return chapters
    }

    override suspend fun fetchLatestUpdates(page: Int): BooksPage {
        val req = latestUpdatesRequest(page)
        val res = client.newCall(req).await()
        return latestUpdatesParse(res)
    }

    override val client: OkHttpClient = super.client

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 "
        )
        add("Referer", baseUrl)
    }

    override fun popularBookSelector(): String = "div.page-item-detail"

    override fun popularBookNextPageSelector(): String? = "div.nav-previous"

    override fun popularBookRequest(page: Int): Request =
        GET("$baseUrl/novel-list/?m_orderby=trending")

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
        book.link = element.select("a").attr("href").substringAfter(baseUrl)
        book.bookName = element.select("a").attr("title")
        book.coverLink = element.select("img").attr("src")
        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = "div.nav-previous"

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/novel-list/page/$page/?m_orderby=latest")


    override fun bookDetailsParse(document: Document): Book {
        val book = Book.create()
        book.bookName = document.select("div.post-title h1").text()
        book.description =
            document.select("div.description-summary p").eachText().joinToString("\n\n\n")
        book.author = document.select("div.author-content a").text()
        book.category =
            document.select("div.genres-content a").eachText().drop(1).map { value -> value.trim() }
                .joinToString(" ,")
        book.source = name
        return book
    }

    override fun hasNextChapterSelector(): String? = null

    override fun hasNextChaptersParse(document: Document): Boolean {
        return false
    }


    override fun chapterListRequest(book: Book, page: Int): Request =
        POST(baseUrl + "${book.link}/ajax/chapters/")

    override fun chapterListSelector(): String = "li.wp-manga-chapter"

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()
        chapter.title = element.select("a").text()
        chapter.link = element.select("a").attr("href")

        return chapter
    }

    override fun chapterListNextPageSelector(): String? = null


    override fun pageContentParse(document: Document): ChapterPage {
        val content = document.select("div.txt h3,p").eachText().joinToString("\n\n\n")

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
        GET("$baseUrl/?s=$query&post_type=wp-manga&op=&author=&artist=&release=&adult=")

    override fun searchBookNextPageSelector(): String? = null
}