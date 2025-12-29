package ireader.domain.usecases.book

import ireader.core.log.IReaderLog
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.updates.BookUpdate
import ireader.domain.usecases.category.AutoCategorizeBookUseCase
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for adding books to the library following Mihon's pattern.
 * Handles the business logic for marking books as favorites.
 */
class AddToLibrary(
    private val updateBook: UpdateBook,
    private val bookRepository: BookRepository,
    private val autoCategorizeBook: AutoCategorizeBookUseCase,
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
                
                // Auto-categorize the book based on rules
                try {
                    val book = bookRepository.findBookById(bookId)
                    if (book != null) {
                        val assignedCategories = autoCategorizeBook(book)
                        if (assignedCategories.isNotEmpty()) {
                            IReaderLog.info("Auto-categorized book $bookId to ${assignedCategories.size} categories", "AddToLibrary")
                        }
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize book: $bookId - ${e.message}", tag = "AddToLibrary")
                }
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
                
                // Auto-categorize all added books
                try {
                    val books = bookIds.mapNotNull { bookRepository.findBookById(it) }
                    if (books.isNotEmpty()) {
                        val categorizedBooks = autoCategorizeBook.categorizeMultiple(books)
                        if (categorizedBooks.isNotEmpty()) {
                            IReaderLog.info("Auto-categorized ${categorizedBooks.size} books", "AddToLibrary")
                        }
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize books - ${e.message}", tag = "AddToLibrary")
                }
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