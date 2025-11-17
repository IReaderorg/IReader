package ireader.domain.usecases.book

import ireader.domain.models.updates.BookUpdate
import ireader.presentation.core.log.IReaderLog

/**
 * Use case for removing books from the library following Mihon's pattern.
 * Handles the business logic for unmarking books as favorites.
 */
class RemoveFromLibrary(
    private val updateBook: UpdateBook,
) {
    /**
     * Remove a book from the library by unmarking it as favorite
     */
    suspend fun await(bookId: Long): Boolean {
        return try {
            val update = BookUpdate(
                id = bookId,
                favorite = false,
            )
            
            val result = updateBook.await(update)
            if (result) {
                IReaderLog.info("Removed book from library: $bookId", "RemoveFromLibrary")
            } else {
                IReaderLog.warn("Failed to remove book from library: $bookId", tag = "RemoveFromLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error removing book from library: $bookId", e, "RemoveFromLibrary")
            false
        }
    }

    /**
     * Remove multiple books from the library
     */
    suspend fun awaitAll(bookIds: List<Long>): Boolean {
        return try {
            val updates = bookIds.map { bookId ->
                BookUpdate(
                    id = bookId,
                    favorite = false,
                )
            }
            
            val result = updateBook.awaitAll(updates)
            if (result) {
                IReaderLog.info("Removed ${bookIds.size} books from library", "RemoveFromLibrary")
            } else {
                IReaderLog.warn("Failed to remove ${bookIds.size} books from library", tag = "RemoveFromLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error removing ${bookIds.size} books from library", e, "RemoveFromLibrary")
            false
        }
    }
}