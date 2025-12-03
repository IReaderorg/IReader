package ireader.domain.usecases.book

import ireader.core.log.IReaderLog
import ireader.domain.models.updates.BookUpdate
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for adding books to the library following Mihon's pattern.
 * Handles the business logic for marking books as favorites.
 */
class AddToLibrary(
    private val updateBook: UpdateBook,
) {
    /**
     * Add a book to the library by marking it as favorite
     */
    suspend fun await(bookId: Long): Boolean {
        return try {
            val update = BookUpdate(
                id = bookId,
                favorite = true,
                dateAdded = currentTimeToLong(),
            )
            
            val result = updateBook.await(update)
            if (result) {
                IReaderLog.info("Added book to library: $bookId", "AddToLibrary")
            } else {
                IReaderLog.warn("Failed to add book to library: $bookId", tag = "AddToLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error adding book to library: $bookId", e, "AddToLibrary")
            false
        }
    }

    /**
     * Add multiple books to the library
     */
    suspend fun awaitAll(bookIds: List<Long>): Boolean {
        return try {
            val currentTime = currentTimeToLong()
            val updates = bookIds.map { bookId ->
                BookUpdate(
                    id = bookId,
                    favorite = true,
                    dateAdded = currentTime,
                )
            }
            
            val result = updateBook.awaitAll(updates)
            if (result) {
                IReaderLog.info("Added ${bookIds.size} books to library", "AddToLibrary")
            } else {
                IReaderLog.warn("Failed to add ${bookIds.size} books to library", tag = "AddToLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error adding ${bookIds.size} books to library", e, "AddToLibrary")
            false
        }
    }
}