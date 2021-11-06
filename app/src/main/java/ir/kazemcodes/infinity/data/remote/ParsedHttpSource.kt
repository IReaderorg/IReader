package ir.kazemcodes.infinity.data.remote

import ir.kazemcodes.infinity.data.remote.source.model.Book
import okhttp3.Headers

abstract class ParsedHttpSource {


    abstract val baseUrl : String

    abstract val name : String

    open val versionId = 1

    abstract fun fetchBook(url : String) : Book

    abstract suspend fun fetchLatestBooksFromElement(page : Int): List<Book>

    abstract  fun fetchPopularBooksFromElement(page : Int): List<Book>

    abstract  fun fetchBooksOrderByAlphabetFromElement(page : Int): List<Book>

    abstract fun searchBook()

    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }
    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
}
