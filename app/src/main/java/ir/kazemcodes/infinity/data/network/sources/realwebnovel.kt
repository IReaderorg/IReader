package ir.kazemcodes.infinity.data.network.sources

import ir.kazemcodes.infinity.api_feature.network.GET
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


class RealWebNovelApi: ParsedHttpSource() {

    override val baseUrl: String = "https://readwebnovels.net"
    override val name: String = "RealWebNovels.net"

    override val lang: String = "en"

    override val supportsLatest: Boolean = true

    override suspend fun fetchPopular(page: Int): BooksPage {
        val req = popularBookRequest(page)
        val res = client.newCall(req).await()
        return popularBookParse(res)
    }

    override suspend fun fetchSearchBook(page: Int, query: String): BooksPage {
        val req = searchBookRequest(page,query = query)
        val res = client.newCall(req).await()
        return searchBookParse(res)
    }


    override suspend fun fetchContent(chapter: Chapter): ChapterPage {
        val req = pageContentRequest(chapter)
        val res = client.newCall(req).await()
        return pageContentParse(res)
    }

    override fun pageContentRequest(chapter: Chapter): Request  = GET(chapter.link,headers = headers)

    override suspend fun fetchBook(book: Book): Book {
        val req = bookDetailsRequest(book)
        val res = client.newCall(req).await()
        return bookDetailsParse(res)
    }


    override suspend fun fetchChapters(book: Book): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val req = chapterListRequest(book,1)
        val res = client.newCall(req).await()
        val content = chapterListParse(res)
        chapters.addAll(content.chapters)
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

    override fun popularBookRequest(page: Int): Request = GET("$baseUrl/manga-2/page/$page/?m_orderby=trending")
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
        book.category = document.select("div.genres-content a").eachText().drop(1).map {value-> value.trim()  }.joinToString(" ,")
        book.source = name
        return book
    }

    override fun hasNextChapterSelector(): String? = null

    override fun hasNextChaptersParse(document: Document): Boolean {
        return  false
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

    override fun searchBookRequest(page: Int,query: String): Request = GET("$baseUrl/?s=$query&post_type=wp-manga")

    override fun searchBookNextPageSelector(): String? = null
}


//
//class RealWebnovel : HttpSource() {
//    override val baseUrl: String
//        get() = "https://readwebnovels.net/"
//    override val name: String
//        get() = "RealWebNovel.net"
//    override val nextPageLinkFormat: String
//        get() = "https://readwebnovels.net/page/{page}/"
//
//
//    override suspend fun fetchElements(url: String, headers: Map<String, String> ): Elements {
//        return kotlin.runCatching {
//            val request = GET(url)
//            val client = OkHttpClient()
//            val ok = client.newCall(request).await()
//            // Log.d("TAG", "onCreate: " + ok.body?.string())
//
//            return@runCatching Jsoup.parse(ok.body?.string()?:"").allElements
//
//        }.getOrThrow()
//
//    }
//
//    override fun fetchBook(book: Book, elements: Elements): Book {
//        val eThumbnail =
//            elements.select("div.tab-summary > div.summary_image > a > img").eachAttr("src")
//
//
//        val eAuthor = elements.select("div.author-content a").eachText()
//        val eGenre = elements.select("iv.summary-content > div.genres-content").eachText()
//        val eDescription =
//            elements.select("div.description-summary div.summary__content.show-more").eachText().joinToString("\n\n")
//
//        book.apply {
//            coverLink = eThumbnail[0]
//            author = eAuthor[0]
//            description = eDescription
//        }
//        return book
//    }
//
//
//    override fun fetchBooks(elements: Elements): List<Book> {
//        val books = mutableListOf<Book>()
//        val eTitle =
//            elements.select("div.item-summary > div.post-title.font-title > h3 > a").eachText()
//        val eImage = elements.select("div.site-content a img").eachAttr("src")
//        val eLink =
//            elements.select("div > div.item-summary > div.post-title.font-title > h3 > a")
//                .eachAttr("href")
//        for (i in 0..11) {
//            books.add(Book.create().apply {
//                bookName = eTitle[i].toString()
//                coverLink = eImage[i]
//                link = eLink[i]
//            })
//        }
//        return books
//    }
//
//    override suspend fun fetchChapters(book: Book, elements: Elements): List<Chapter> {
//        val chapters = mutableListOf<Chapter>()
//        val eChaptersTitle = elements.select("li.wp-Book-chapter     a").eachText()
//        val eChaptersUrl = elements.select("li.wp-Book-chapter     a").eachAttr("href")
//
//
//        eChaptersTitle.forEachIndexed { index, element ->
//            chapters.add(Chapter(
//                bookName = book.bookName,
//                title = element,
//                link = eChaptersUrl[index],
//                index = index,
//            ))
//        }
//        return chapters
//    }
//
//    override fun fetchReadingContent(elements: Elements): String {
//        return elements.select("div.entry-content > div > div > div > div.text-left > p").eachText().joinToString("\n\n\n")
//    }
//
//    override fun searchBook(query: String): List<Book> {
//        TODO("Not yet implemented")
//    }
//
//    override val id: Long
//        get() = 2
//    override val lang: String
//        get() = "en"
//
//    override val supportsLatest: Boolean
//        get() = true
//}





//    override  fun fetchBook(book: Book): Book {
//
//            val elements =
//                Jsoup.connect(book.url).header("Referer", "https://readwebnovels.net/").get()
//            Log.d(TAG, "fetchLatestBooksFromElement: $elements")
//
//
//    }


