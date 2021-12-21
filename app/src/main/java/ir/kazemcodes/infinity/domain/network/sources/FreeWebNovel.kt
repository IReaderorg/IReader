package ir.kazemcodes.infinity.domain.network.sources

import ir.kazemcodes.infinity.api_feature.data.BooksPage
import ir.kazemcodes.infinity.api_feature.data.ChapterPage
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber

class FreeWebNovel : ParsedHttpSource() {

    override val baseUrl: String = "https://freewebnovel.com"
    override val name: String = "FreeWebNovel"

    override val lang: String = "en"

    override val supportsLatest: Boolean = true
    override suspend fun fetchPopular(page: Int): BooksPage {
        TODO("Not yet implemented")
    }

    override suspend fun fetchSearchBook(page: Int, query: String): BooksPage {
        TODO("Not yet implemented")
    }

    override suspend fun fetchContent(chapter: Chapter): ChapterPage {
        val req = pageContentRequest(chapter)
        val res = client.newCall(req).await()
        val content = pageContentParse(res)

        return content
    }

    override suspend fun fetchBook(book: Book): Book {
        val req = bookDetailsRequest(book)
        val res = client.newCall(req).await()

        return bookDetailsParse(res)
    }

    override suspend fun fetchChapters(book: Book): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        var hasNext = true
        var page : Int = 1
        while (hasNext){
            val req = chapterListRequest(book,page)
            val res = client.newCall(req).await()
            val content = chapterListParse(res)
            chapters.addAll(content.chapters)
            hasNext = content.hasNextPage
            page += 1
            Timber.d("Timber: GetRemoteChaptersUseCase + ${chapters.size} Chapters Added , current page: $page" )
        }
        Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully with $page tries")
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

    override fun popularMangaSelector(): String = "div.ul-list1"

    override fun popularBookNextPageSelector(): String? = "div.ul-list1"

    override fun popularBookRequest(page: Int): Request = GET("$baseUrl/most-popular-novel/")
    override fun popularBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.link = element.attr("href").substringAfter(baseUrl)
        book.bookName = element.attr("title")
        book.coverLink = element.select("img").attr("src")
        return book
    }

    override fun latestUpdatesSelector(): String = "div.ul-list1 div.li"

    override fun latestUpdatesFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.link = element.select("div.txt a").attr("href").substringAfter(baseUrl)
        book.bookName = element.select("div.txt a").attr("title")
        book.coverLink = element.select("div.pic img").attr("src")
        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = "div.ul-list1"

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/latest-release-novel/$page/")


    override fun bookDetailsParse(document: Document): Book {
        val book = Book.create()
        book.bookName = document.select("div.m-desc h1.tit").text()
        book.description = document.select("div.inner").eachText().joinToString("\n\n\n")
        book.author = document.select("div.right a.a1").attr("title")
        book.category = document.select("div.item div.right a.a1").eachText().joinToString(" ,")
        book.source = name
        return book
    }

    override fun hasNextChapterSelector() = "div.page a:nth-child(4)"

    override fun hasNextChaptersParse(document: Document): Boolean {

        val res = document.select(hasNextChapterSelector()).text()

        return res.contains("Next")
    }

    /** **/
    override fun chapterListRequest(book: Book, page: Int): Request =
        GET(baseUrl + book.link.replace(".html", "/$page.html"))

    override fun chapterListSelector(): String = "div.m-newest2 ul.ul-list5 li"

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()
        chapter.title = element.select("a").attr("title")
        chapter.link = element.select("a").attr("href").substringAfter(baseUrl)

        return chapter
    }

    override fun chapterListNextPageSelector(): String? = "div.page a:nth-child(4)"


    override fun pageContentParse(document: Document): ChapterPage {
        val content = document.select("div.txt h4,p").eachText().joinToString("\n\n\n")

        return ChapterPage(content)
    }


}