package ireader.domain.usecases.local.insert_usecases

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.withIOContext
import org.koin.core.annotation.Factory

@Factory
class InsertBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return withIOContext {
            return@withIOContext bookRepository.insertBooks(books)
        }
    }
}
@Factory
class InsertBookAndChapters(private val bookRepository: BookRepository) {
    suspend operator fun invoke(books: List<Book>, chapters: List<Chapter>) {
        return withIOContext {
            return@withIOContext bookRepository.insertBooksAndChapters(books, chapters)
        }
    }
}
