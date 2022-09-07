package ireader.domain.use_cases.local.insert_usecases

import ireader.common.data.repository.BookRepository
import ireader.common.models.entities.Book
import ireader.common.models.entities.Chapter
import org.koin.core.annotation.Factory

@Factory
class InsertBooks(private val bookRepository: BookRepository) {
    suspend operator fun invoke(books: List<Book>): List<Long> {
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.insertBooks(books)
        }
    }
}
@Factory
class InsertBookAndChapters(private val bookRepository: BookRepository) {
    suspend operator fun invoke(books: List<Book>, chapters: List<Chapter>) {
        return ireader.common.extensions.withIOContext {
            return@withIOContext bookRepository.insertBooksAndChapters(books, chapters)
        }
    }
}
