package ireader.domain.usecases.book

import ireader.core.log.IReaderLog
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.models.updates.BookUpdate

/**
 * Use case for removing books from the library following Mihon's pattern.
 * Handles the business logic for unmarking books as favorites.
 */
class RemoveFromLibrary(
    private val updateBook: UpdateBook,
    private val bookCategoryRepository: BookCategoryRepository? = null,
) {
    /**
     * Remove a book from the library by unmarking it as favorite.
     * Also removes all category associations for the book.
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
                
                // Remove all category associations for this book
                try {
                    bookCategoryRepository?.delete(bookId)
                    IReaderLog.info("Removed all category associations for book: $bookId", "RemoveFromLibrary")
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to remove category associations for book: $bookId - ${e.message}", tag = "RemoveFromLibrary")
                }
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
     * Remove multiple books from the library.
     * Also removes all category associations for the books.
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
                
                // Remove all category associations for these books
                try {
                    bookIds.forEach { bookId ->
                        bookCategoryRepository?.delete(bookId)
                    }
                    IReaderLog.info("Removed all category associations for ${bookIds.size} books", "RemoveFromLibrary")
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to remove category associations for books - ${e.message}", tag = "RemoveFromLibrary")
                }
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