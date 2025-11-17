package ireader.domain.usecases.book

import ireader.domain.models.entities.Book
import ireader.presentation.core.log.IReaderLog

/**
 * Use case for toggling book favorite status following Mihon's pattern.
 * Handles the business logic for adding/removing books from library.
 */
class ToggleFavorite(
    private val getBook: GetBook,
    private val addToLibrary: AddToLibrary,
    private val removeFromLibrary: RemoveFromLibrary,
) {
    /**
     * Toggle the favorite status of a book
     */
    suspend fun await(bookId: Long): Boolean {
        return try {
            val book = getBook.await(bookId)
            if (book == null) {
                IReaderLog.warn("Book not found for toggle favorite: $bookId", tag = "ToggleFavorite")
                return false
            }
            
            val result = if (book.favorite) {
                removeFromLibrary.await(bookId)
            } else {
                addToLibrary.await(bookId)
            }
            
            if (result) {
                val action = if (book.favorite) "removed from" else "added to"
                IReaderLog.info("Book $action library: $bookId", "ToggleFavorite")
            }
            
            result
        } catch (e: Exception) {
            IReaderLog.error("Error toggling favorite for book: $bookId", e, "ToggleFavorite")
            false
        }
    }

    /**
     * Toggle the favorite status of a book using the book entity directly
     */
    suspend fun await(book: Book): Boolean {
        return try {
            val result = if (book.favorite) {
                removeFromLibrary.await(book.id)
            } else {
                addToLibrary.await(book.id)
            }
            
            if (result) {
                val action = if (book.favorite) "removed from" else "added to"
                IReaderLog.info("Book '${book.title}' $action library", "ToggleFavorite")
            }
            
            result
        } catch (e: Exception) {
            IReaderLog.error("Error toggling favorite for book '${book.title}'", e, "ToggleFavorite")
            false
        }
    }
}