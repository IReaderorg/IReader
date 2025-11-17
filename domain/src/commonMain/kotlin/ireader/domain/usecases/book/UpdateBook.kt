package ireader.domain.usecases.book

import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.updates.BookUpdate
import ireader.presentation.core.log.IReaderLog

/**
 * Use case for updating book information following Mihon's pattern.
 * Provides both single and batch update operations with proper error handling.
 */
class UpdateBook(
    private val bookRepository: BookRepository,
) {
    /**
     * Update a single book
     */
    suspend fun await(update: BookUpdate): Boolean {
        return try {
            val result = bookRepository.update(update)
            if (result) {
                IReaderLog.debug("Successfully updated book: ${update.id}", "UpdateBook")
            } else {
                IReaderLog.warn("Failed to update book: ${update.id}", tag = "UpdateBook")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error updating book: ${update.id}", e, "UpdateBook")
            false
        }
    }

    /**
     * Update multiple books in a batch operation
     */
    suspend fun awaitAll(updates: List<BookUpdate>): Boolean {
        return try {
            val result = bookRepository.updateAll(updates)
            if (result) {
                IReaderLog.debug("Successfully updated ${updates.size} books", "UpdateBook")
            } else {
                IReaderLog.warn("Failed to update ${updates.size} books", tag = "UpdateBook")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error updating ${updates.size} books", e, "UpdateBook")
            false
        }
    }
}