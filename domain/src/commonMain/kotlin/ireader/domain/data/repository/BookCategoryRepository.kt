package ireader.domain.data.repository

import ireader.domain.models.entities.BookCategory
import kotlinx.coroutines.flow.Flow

interface BookCategoryRepository {
    fun subscribeAll(): Flow<List<BookCategory>>

    suspend fun findAll(): List<BookCategory>

    suspend fun insert(category: BookCategory)
    suspend fun insertAll(categories: List<BookCategory>)
    suspend fun delete(category: List<BookCategory>)
    suspend fun delete(bookId: Long)
    suspend fun deleteAll(category: List<BookCategory>)
    
    /**
     * Atomically replace all category assignments for a book.
     * Deletes existing assignments and inserts new ones in a single transaction.
     * 
     * This is the primary method for setting book categories from:
     * - Explore screen (adding to library with categories)
     * - Book detail screen (editing categories)
     * - Bulk operations in library
     * 
     * @param bookId The book to update
     * @param categoryIds List of category IDs to assign (empty = remove all categories)
     */
    suspend fun replaceAll(bookId: Long, categoryIds: List<Long>)
}
