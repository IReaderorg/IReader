package ireader.domain.usecases.book

import ireader.domain.data.repository.BookRepository

/**
 * Use case for updating book pin status
 */
class UpdateBookPinStatusUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Pin or unpin a book
     */
    suspend operator fun invoke(bookId: Long, isPinned: Boolean) {
        val pinnedOrder = if (isPinned) {
            // Get next available pinned order
            bookRepository.getMaxPinnedOrder() + 1
        } else {
            0
        }
        
        bookRepository.updatePinStatus(bookId, isPinned, pinnedOrder)
    }
    
    /**
     * Update pinned order for a book
     */
    suspend fun updateOrder(bookId: Long, pinnedOrder: Int) {
        bookRepository.updatePinnedOrder(bookId, pinnedOrder)
    }
}
