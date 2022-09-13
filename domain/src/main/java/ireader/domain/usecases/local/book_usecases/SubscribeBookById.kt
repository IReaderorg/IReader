package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import org.koin.core.annotation.Factory

/**
 * return a book from id
 */
@Factory
class SubscribeBookById(private val bookRepository: BookRepository) {
    operator fun invoke(id: Long): Flow<Book?> {
        return bookRepository.subscribeBookById(id = id)
    }
}
@Factory
class FindBookById(private val bookRepository: BookRepository) {
    suspend operator fun invoke(id: Long): Book? {
        return bookRepository.findBookById(id = id)
    }
}
