package ir.kazemcodes.infinity.data.local.repository

import ir.kazemcodes.infinity.data.local.BookDao
import ir.kazemcodes.infinity.domain.model.book.BookEntity
import ir.kazemcodes.infinity.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow

class LocalRepositoryImpl(
    private val dao : BookDao
) : LocalRepository {
    override fun getBooks(): Flow<List<BookEntity>> {
        return dao.getBooks()
    }

    override suspend fun getBookById(id: Int): BookEntity? {
        return dao.getBookById(id)
    }

    override suspend fun insertBook(bookEntity: BookEntity) {
        return dao.insertBook(bookEntity)
    }

    override suspend fun deleteBook(bookEntity: BookEntity) {
        return dao.deleteBook(bookEntity = bookEntity)
    }

    override suspend fun deleteAllBook() {
        return dao.deleteAllBook()
    }


}