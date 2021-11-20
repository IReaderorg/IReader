package ir.kazemcodes.infinity.explore_feature.domain.repository

import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import org.jsoup.select.Elements

interface RemoteRepository {


    suspend fun getElements(url : String , headers : Map<String,String>) : Elements

    suspend fun getBooks(elements: Elements) : List<Book>

    suspend fun getBookDetail(book: Book, elements: Elements) : Book
    suspend fun getChapters(book: Book, elements: Elements) : List<Chapter>

    suspend fun getReadingContent(elements: Elements) : String

}