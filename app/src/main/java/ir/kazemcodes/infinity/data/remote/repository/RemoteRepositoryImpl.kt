package ir.kazemcodes.infinity.data.remote.repository

import ir.kazemcodes.infinity.data.remote.ParsedHttpSource
import ir.kazemcodes.infinity.data.remote.source.model.Book
import ir.kazemcodes.infinity.domain.repository.RemoteRepository
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val api : ParsedHttpSource
) : RemoteRepository {
    override suspend fun getBookDetail(page: Int): Book {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestBooks(page: Int): List<Book> {
        return api.fetchLatestBooksFromElement(page = page)
    }

    override suspend fun getPopularBooks(page: Int): List<Book> {
        TODO("Not yet implemented")
    }

}