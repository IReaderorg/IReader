package ireader.domain.usecases.local.insert_usecases

import ireader.domain.data.repository.BookRepository
import ireader.common.models.entities.Book
import ireader.common.models.entities.LibraryBook
import org.koin.core.annotation.Factory

@Factory
class InsertBook(private val bookRepository: BookRepository) {
    suspend operator fun invoke(book: Book?): Long {
        if (book == null) return -1
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.upsert(book) ?: -1
        }
    }
}
@Factory
class UpdateBook(private val bookRepository: BookRepository) {
    suspend fun update(book: LibraryBook, favorite: Boolean) {
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.updateBook(book, favorite = favorite)
        }
    }
    suspend fun update(book: Book?) {
        if (book == null) return
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.updateBook(book)
        }
    }
    suspend fun update(book: List<Book>) {
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.updateBook(book)
        }
    }
}
