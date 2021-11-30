package ir.kazemcodes.infinity.explore_feature

import ir.kazemcodes.infinity.api_feature.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import okhttp3.Headers
import org.jsoup.select.Elements

class WuxiaorldApi : ParsedHttpSource() {
    override val baseUrl: String
        get() = "https://wuxiaworld.site/"
    override val name: String
        get() = "Wuxiaorld Api"

    override fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0 ")
        add("Referer", baseUrl)
    }

    override suspend fun fetchBookElements(url: String, headers: Map<String, String>): Elements {
        TODO("Not yet implemented")
    }

    override fun fetchBook(book: Book, elements: Elements): Book {
        TODO("Not yet implemented")
    }

    override fun fetchBooks(elements: Elements): List<Book> {
        TODO("Not yet implemented")
    }

    override fun fetchChapters(book: Book, elements: Elements): List<Chapter> {
        TODO("Not yet implemented")
    }

    override fun fetchReadingContent(elements: Elements): String {
        TODO("Not yet implemented")
    }

    override fun searchBook(query: String): List<Book> {
        TODO("Not yet implemented")
    }

    override val supportsLatest: Boolean
        get() = TODO("Not yet implemented")

}