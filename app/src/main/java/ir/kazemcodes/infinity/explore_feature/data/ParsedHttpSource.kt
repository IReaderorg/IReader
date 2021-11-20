package ir.kazemcodes.infinity.explore_feature.data

import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import okhttp3.Headers
import org.jsoup.select.Elements

abstract class ParsedHttpSource {


    abstract val baseUrl : String

    abstract val name : String

    open val versionId = 1

    abstract suspend fun fetchBookElements(url : String,headers : Map<String,String> = mutableMapOf(
        Pair<String, String>("Referer","https://readwebnovels.net/"),
        Pair<String, String>("User-Agent", DEFAULT_USER_AGENT),

    )) : Elements

    abstract  fun fetchBook(book: Book, elements: Elements) : Book

    abstract  fun fetchBooks(elements: Elements): List<Book>

    abstract  fun fetchChapters(book: Book, elements: Elements): List<Chapter>


    abstract  fun fetchReadingContent(elements: Elements): String



    abstract fun searchBook(query: String) : List<Book>

    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }
    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
}
