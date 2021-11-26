package ir.kazemcodes.infinity.library_feature.data.repository

import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalBookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalBookRepositoryImpl @Inject constructor(
    private val dao : BookDao
) : LocalBookRepository {
    override fun getBooks(): Flow<List<BookEntity>> {
        return dao.getInitializedBooks()
    }
    override suspend fun getBookById(bookId: Int): BookEntity {
        return dao.getBookById(bookId)
    }

    override suspend fun getBookByName(bookName: String): BookEntity {
        return dao.getBookByName(bookName)
    }

    override suspend fun insertBook(bookEntity: BookEntity) {
        return dao.insertBook(bookEntity)
    }

    override suspend fun insertBooks(bookEntities: List<BookEntity>) {
        return dao.insertBooks(bookEntities)
    }
    override suspend fun deleteBook(bookId: Int) {
        return dao.deleteBook(bookId = bookId)
    }
    override suspend fun deleteAllBook() {
        return dao.deleteAllBook()
    }
}