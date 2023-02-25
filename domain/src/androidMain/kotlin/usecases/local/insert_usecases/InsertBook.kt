package ireader.domain.usecases.local.insert_usecases

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.utils.extensions.withIOContext



class InsertBook(private val bookRepository: BookRepository) {
    suspend operator fun invoke(book: Book?): Long {
        if (book == null) return -1
        return withIOContext {
            return@withIOContext bookRepository.upsert(book) ?: -1
        }
    }
}

class UpdateBook(private val bookRepository: BookRepository) {
    suspend fun update(book: LibraryBook, favorite: Boolean) {
        return withIOContext {
            return@withIOContext bookRepository.updateBook(book, favorite = favorite)
        }
    }
    suspend fun update(book: Book?) {
        if (book == null) return
        return withIOContext {
            return@withIOContext bookRepository.updateBook(book)
        }
    }
    suspend fun update(book: List<Book>) {
        return withIOContext {
            return@withIOContext bookRepository.updateBook(book)
        }
    }
}
