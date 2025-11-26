package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository

/**
 * Use case for updating book archive status
 */
class UpdateBookArchiveStatusUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Archive or unarchive a book
     */
    suspend operator fun invoke(bookId: Long, isArchived: Boolean) {
        bookRepository.updateArchiveStatus(bookId, isArchived)
    }
    
    /**
     * Archive multiple books
     */
    suspend fun archiveBooks(bookIds: List<Long>) {
        bookIds.forEach { bookId ->
            invoke(bookId, true)
        }
    }
    
    /**
     * Unarchive multiple books
     */
    suspend fun unarchiveBooks(bookIds: List<Long>) {
        bookIds.forEach { bookId ->
            invoke(bookId, false)
        }
    }
}
