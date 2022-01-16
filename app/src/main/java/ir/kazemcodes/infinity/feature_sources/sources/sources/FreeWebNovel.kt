//package ir.kazemcodes.infinity.Sources.sources
//
//import android.content.Context
//import ir.kazemcodes.infinity.api_feature.network.GET
//import ir.kazemcodes.infinity.core.data.network.models.BookPage
//import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
//import ir.kazemcodes.infinity.core.data.network.models.ParsedHttpSource
//import ir.kazemcodes.infinity.core.domain.models.Book
//import ir.kazemcodes.infinity.core.domain.models.Chapter
//import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
//import okhttp3.Headers
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//
//
//class FreeWebNovel(context: Context) : ParsedHttpSource(context) {
//
//    override val baseUrl: String = "https://freewebnovel.com"
//    override val name: String = "FreeWebNovel"
//
//    override val lang: String = "en"
//
//    override val supportsLatest: Boolean = true
//
//    override val client: OkHttpClient = super.client
//
//    override val supportSearch: Boolean = true
//
//    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
//        add(
//            "User-Agent",
//            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 "
//        )
//        add("Referer", baseUrl)
//    }
//
//    override val supportsMostPopular: Boolean
//        get() = true
//    override fun popularBookSelector(): String = "div.ul-list1"
//
//    override fun popularBookNextPageSelector(): String? = "div.ul-list1"
//
//    override fun popularBookRequest(page: Int): Request = GET("$baseUrl/most-popular-novel/")
//    override fun popularBookFromElement(element: Element): Book {
//        val book: Book = Book.create()
//        book.link = element.attr("href").substringAfter(baseUrl)
//        book.bookName = element.attr("title")
//        book.coverLink = element.select("img").attr("src")
//        return book
//    }
//
//    override fun latestUpdatesSelector(): String = "div.ul-list1 div.li"
//
//    override fun latestUpdatesFromElement(element: Element): Book {
//        val book: Book = Book.create()
//        book.link = element.select("div.txt a").attr("href").substringAfter(baseUrl)
//        book.bookName = element.select("div.txt a").attr("title")
//        book.coverLink = element.select("div.pic img").attr("src")
//        return book
//    }
//
//    override fun latestUpdatesNextPageSelector(): String? = "div.ul-list1"
//
//    override fun fetchLatestUpdatesEndpoint(): String? = "/latest-release-novel/$pageFormat/"
//    override fun fetchPopularEndpoint(): String? = "/most-popular-novel/"
//
//    override fun fetchSearchBookEndpoint(): String? = "/search?searchkey=$searchQueryFormat"
//
//    override fun fetchChaptersEndpoint(): String? = null
//
//    override fun fetchContentEndpoint(): String? = null
//
//    override fun latestUpdatesRequest(page: Int): Request =
//        GET("$baseUrl${fetchLatestUpdatesEndpoint()?.replace(pageFormat,page.toString())}")
//
//
//    override fun bookDetailsParse(document: Document): BookPage {
//        val isCloudflareEnable = document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
//        val book = Book.create()
//        book.bookName = document.select("div.m-desc h1.tit").text()
//        book.description = document.select("div.inner").eachText()
//        book.author = document.select("div.right a.a1").attr("title")
//        book.category = document.select("div.item div.right a.a1").eachText().drop(1)
//            .map { value -> value.trim() }
//        book.source = name
//        return BookPage(book = book, isCloudflareEnabled = isCloudflareEnable)
//    }
//
//        override fun hasNextChapterSelector() = "div.page a:nth-child(4)"
//
//    override fun hasNextChaptersParse(document: Document): Boolean {
//
//        val res = document.select(hasNextChapterSelector()).text()
//
//        return res.contains("Next")
//    }
//
//
//    override fun chapterListRequest(book: Book, page: Int): Request =
//        GET(baseUrl + book.link.replace(".html", "/$page.html"))
//
//    override fun chapterListSelector(): String = "div.m-newest2 ul.ul-list5 li"
//
//    override fun chapterFromElement(element: Element): Chapter {
//        val chapter = Chapter.create()
//        chapter.title = element.select("a").attr("title")
//        chapter.link = element.select("a").attr("href").substringAfter(baseUrl)
//
//        return chapter
//    }
//
////    override fun chapterListNextPageSelector(): String? = "div.page a:nth-child(4)"
//
//
//    override fun pageContentParse(document: Document): ChapterPage {
//        val content = document.select("div.txt h4,p").eachText()
//
//        return ChapterPage(content)
//    }
//
//
//
//    override fun searchBookSelector(): String = "div.ul-list1 div.li"
//
//    override fun searchBookFromElement(element: Element): Book {
//        val book: Book = Book.create()
//        book.link = element.select("div.txt a").attr("href").substringAfter(baseUrl)
//        book.bookName = element.select("div.txt a").attr("title")
//        book.coverLink = element.select("div.pic img").attr("src")
//        return book
//    }
//
//    override fun searchBookRequest(page: Int, query: String): Request =
//        GET("https://freewebnovel.com/search?searchkey=$query")
//
//    override fun searchBookNextPageSelector(): String? = null
//
//
//}