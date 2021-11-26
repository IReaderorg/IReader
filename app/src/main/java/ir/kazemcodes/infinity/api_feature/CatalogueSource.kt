package ir.kazemcodes.infinity.api_feature

import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import org.jsoup.select.Elements

interface CatalogueSource {


    val supportsLatest: Boolean

    fun fetchBook(book: Book, elements: Elements) : Book

    fun fetchBooks(elements: Elements): List<Book>

    fun fetchChapters(book: Book, elements: Elements): List<Chapter>

    fun fetchReadingContent(elements: Elements): String

    fun searchBook(query: String) : List<Book>
}