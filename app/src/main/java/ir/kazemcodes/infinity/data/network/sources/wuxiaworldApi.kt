package ir.kazemcodes.infinity.data.network.sources

import android.content.Context
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.api_feature.network.POST
import ir.kazemcodes.infinity.data.network.models.ChapterPage
import ir.kazemcodes.infinity.data.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class WuxiaWorldApi(context: Context) : ParsedHttpSource(context) {

    override val baseUrl: String = "https://wuxiaworld.site"
    override val name: String = "WuxiaWorld.site"

    override val lang: String = "en"

    override val supportsLatest: Boolean = true


    override val client: OkHttpClient = super.client

//    override suspend fun fetchLatestUpdates(page: Int): BooksPage {
//        return latestUpdatesParse(client.newCall(latestUpdatesRequest(page)).await())
//    }

    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "user-agent",
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Mobile Safari/537.36"
        )
        add("referer", baseUrl)
    }


    override val supportsMostPopular: Boolean
        get() = false
    override val supportSearch: Boolean = true

    override fun popularBookSelector(): String = "div.page-item-detail"

    override fun popularBookNextPageSelector(): String? = "div.nav-previous"

    override fun popularBookRequest(page: Int): Request =
        GET("$baseUrl/novel-list/?m_orderby=trending",headers)



    override fun popularBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.bookName = element.select("h3.h5 a").text()
        book.link = element.select("h3.h5 a").attr("href").substringAfter(baseUrl)
        book.coverLink = element.select("div.c-image-hover img.img-responsive").attr("src")
        book.source = this.name
        return book
    }

    override fun latestUpdatesSelector(): String = "div.page-item-detail"

    override fun latestUpdatesFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.bookName = element.select("h3.h5 a").text()
        book.link = element.select("h3.h5 a").attr("href").substringAfter(baseUrl)
        book.coverLink = element.select("img.img-responsive").attr("src")
        book.source = this.name
        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = "div.nav-previous"

    override fun fetchLatestUpdatesEndpoint(): String?  = "/novel-list/page/$pageFormat/?m_orderby=latest"
    override fun fetchPopularEndpoint(): String? = "/novel-list/?m_orderby=trending"

    override fun fetchSearchBookEndpoint(): String? = "/?s=$searchQueryFormat&post_type=wp-manga&op=&author=&artist=&release=&adult="

    override fun fetchChaptersEndpoint(): String? = null

    override fun fetchContentEndpoint(): String? = null

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl${fetchLatestUpdatesEndpoint()?.replace(pageFormat,page.toString())},headers")




    override fun bookDetailsParse(document: Document): Book {
        val book = Book.create()
        book.bookName = document.select("div.post-title h1").text()
        book.description =
            document.select("div.description-summary p").eachText()
        book.author = document.select("div.author-content a").text()
        book.category =
            document.select("div.genres-content a").eachText().drop(1).map { value -> value.trim() }
        book.source = name
        return book
    }

    override fun hasNextChapterSelector(): String? = null

    override fun hasNextChaptersParse(document: Document): Boolean {
        return false
    }


    override fun chapterListRequest(book: Book, page: Int): Request =
        POST(baseUrl + getUrlWithoutDomain(book.link) + "ajax/chapters/")

    override fun chapterListSelector(): String = "li.wp-manga-chapter"

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()

        chapter.title = element.select("a").text()
        chapter.link = element.select("a").attr("href")

        return chapter
    }



    override fun pageContentParse(document: Document): ChapterPage {
        val content = document.select("div.txt h3,p").eachText()

        return ChapterPage(content)
    }

    override fun searchBookSelector(): String = "div.c-tabs-item__content"

    override fun searchBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        book.link = element.select("div.c-image-hover a").attr("href").substringAfter(baseUrl)
        book.bookName = element.select("div.c-image-hover a").attr("title")
        book.coverLink = element.select("div.c-image-hover img.img-responsive").attr("src")
        return book
    }

    override fun searchBookRequest(page: Int, query: String): Request =
        GET("$baseUrl/?s=$query&post_type=wp-manga&op=&author=&artist=&release=&adult=")

    override fun searchBookNextPageSelector(): String? = null

}