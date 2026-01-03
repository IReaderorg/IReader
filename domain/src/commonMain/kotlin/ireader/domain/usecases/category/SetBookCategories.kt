package ireader.domain.usecases.category

import ireader.core.log.Log
import ireader.domain.data.repository.BookCategoryRepository

/**
 * Use case for setting book categories atomically.
 * Replaces all existing category assignments with the new set.
 * 
 * This follows Mihon's SetMangaCategories pattern and is the primary use case 
 * for category assignment from:
 * - Explore screen (adding to library with categories)
 * - Book detail screen (editing categories)
 * - Bulk operations in library
 * 
 * @property bookCategoryRepository Repository for book-category relationships
 */
class SetBookCategories(
    private val bookCategoryRepository: BookCategoryRepository,
) {
    /**
     * Set categories for a book, replacing any existing assignments.
     * 
     * @param bookId The book to update
     * @param categoryIds List of category IDs to assign (empty = remove all categories)
     */
    suspend fun await(bookId: Long, categoryIds: List<Long>) {
        try {
            bookCategoryRepository.replaceAll(bookId, categoryIds)
            Log.info { "SetBookCategories: Set ${categoryIds.size} categories for book $bookId" }
        } catch (e: Exception) {
            Log.error { "SetBookCategories: Failed to set categories for book $bookId: ${e.message}" }
        }
    }
    
    /**
     * Set categories for multiple books at once.
     * Each book will have the same set of categories assigned.
     * 
     * @param bookIds List of book IDs to update
     * @param categoryIds List of category IDs to assign to all books
     */
    suspend fun awaitAll(bookIds: List<Long>, categoryIds: List<Long>) {
        try {
            bookIds.forEach { bookId ->
                bookCategoryRepository.replaceAll(bookId, categoryIds)
            }
            Log.info { "SetBookCategories: Set ${categoryIds.size} categories for ${bookIds.size} books" }
        } catch (e: Exception) {
            Log.error { "SetBookCategories: Failed to set categories for ${bookIds.size} books: ${e.message}" }
        }
    }
}
