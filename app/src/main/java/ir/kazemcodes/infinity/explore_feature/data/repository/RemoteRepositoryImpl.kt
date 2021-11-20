package ir.kazemcodes.infinity.explore_feature.data.repository

import ir.kazemcodes.infinity.explore_feature.data.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.domain.repository.RemoteRepository
import org.jsoup.select.Elements
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val api : ParsedHttpSource
) : RemoteRepository {
    override suspend fun getElements(url: String, headers: Map<String, String>): Elements {
        return api.fetchBookElements(url = url, headers = headers)
    }


    override suspend fun getBooks(elements: Elements): List<Book> {
        return api.fetchBooks(elements = elements)
    }

    override suspend fun getBookDetail(book: Book, elements: Elements): Book {
        return api.fetchBook(book , elements)
    }

    override suspend fun getChapters(book: Book, elements: Elements): List<Chapter> {
        return api.fetchChapters(book,elements)
    }

    override suspend fun getReadingContent(elements: Elements): String {
        return api.fetchReadingContent(elements = elements)
    }

}