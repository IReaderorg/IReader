package ireader.domain.usecases.book

import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.entities.Book
import ireader.presentation.core.log.IReaderLog
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving book information following Mihon's pattern.
 * Provides both suspend and Flow-based access to book data.
 */
class GetBook(
    private val bookRepository: BookRepository,
) {
    /**
     * Get a book by ID as a suspend function
     */
    suspend fun await(id: Long): Book? {
        return try {
            bookRepository.getBookById(id)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get book by id: $id", e, "GetBook")
            null
        }
    }

    /**
     * Subscribe to book changes by ID as a Flow
     */
    fun subscribe(id: Long): Flow<Book?> {
        return bookRepository.getBookByIdAsFlow(id)
    }

    /**
     * Get a book by URL and source ID
     */
    suspend fun awaitByUrlAndSource(url: String, sourceId: Long): Book? {
        return try {
            bookRepository.getBookByUrlAndSourceId(url, sourceId)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get book by url and source: $url, $sourceId", e, "GetBook")
            null
        }
    }

    /**
     * Subscribe to book changes by URL and source ID
     */
    fun subscribeByUrlAndSource(url: String, sourceId: Long): Flow<Book?> {
        return bookRepository.getBookByUrlAndSourceIdAsFlow(url, sourceId)
    }
}