package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.BookRepository

/**
 * Use case to toggle pin status of a book
 */
class ToggleBookPinUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Toggle pin status for a book
     * @param bookId The ID of the book to toggle
     * @param isPinned Whether the book should be pinned
     * @return Result indicating success or failure
     */
    suspend fun togglePin(bookId: Long, isPinned: Boolean): Result<Unit> {
        return try {
            // Get the current max pinned order if pinning
            val pinnedOrder = if (isPinned) {
                bookRepository.getMaxPinnedOrder() + 1
            } else {
                0
            }
            
            bookRepository.updatePinStatus(bookId, isPinned, pinnedOrder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update pinned order for a book
     * @param bookId The ID of the book
     * @param pinnedOrder The new pinned order
     */
    suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int): Result<Unit> {
        return try {
            bookRepository.updatePinnedOrder(bookId, pinnedOrder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
