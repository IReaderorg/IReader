package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a book by its ID
 */
class GetBookByIdUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Get book by ID as a one-time operation
     */
    suspend operator fun invoke(bookId: Long): Book? {
        return bookRepository.findBookById(bookId)
    }
    
    /**
     * Subscribe to book changes by ID
     */
    fun subscribe(bookId: Long): Flow<Book?> {
        return bookRepository.subscribeBookById(bookId)
    }
}
