package ireader.domain.data.repository

import ireader.domain.models.entities.ExploreBook

/**
 * Repository interface for explore book data access operations.
 * 
 * This repository manages temporary storage of browsed books in the explore screen.
 * It maintains a fixed size limit and automatically cleans up old entries.
 * Books are promoted to the main book table when favorited or viewed in detail.
 */
interface ExploreBookRepository {
    
    companion object {
        /**
         * Maximum number of explore books to keep in the database.
         * Older books are automatically deleted when this limit is exceeded.
         */
        const val MAX_EXPLORE_BOOKS = 500
    }
    
    /**
     * Insert or update an explore book.
     * If the book already exists (same URL and source), it will be updated.
     * 
     * @param book The explore book to upsert
     * @return The ID of the inserted/updated book
     */
    suspend fun upsert(book: ExploreBook): Long
    
    /**
     * Insert multiple explore books with automatic cleanup.
     * If the total count exceeds MAX_EXPLORE_BOOKS, oldest books are deleted.
     * 
     * @param books List of explore books to insert
     */
    suspend fun upsertAll(books: List<ExploreBook>)
    
    /**
     * Find an explore book by URL and source ID.
     * 
     * @param url The book's URL
     * @param sourceId The source ID
     * @return The explore book if found, null otherwise
     */
    suspend fun findByUrlAndSource(url: String, sourceId: Long): ExploreBook?
    
    /**
     * Find all explore books for a specific source.
     * 
     * @param sourceId The source ID
     * @return List of explore books for the source
     */
    suspend fun findBySource(sourceId: Long): List<ExploreBook>
    
    /**
     * Count total explore books in the database.
     * 
     * @return The total count
     */
    suspend fun countAll(): Long
    
    /**
     * Count explore books for a specific source.
     * 
     * @param sourceId The source ID
     * @return The count for the source
     */
    suspend fun countBySource(sourceId: Long): Long
    
    /**
     * Delete an explore book by URL and source.
     * Called when a book is promoted to the main book table.
     * 
     * @param url The book's URL
     * @param sourceId The source ID
     */
    suspend fun deleteByUrlAndSource(url: String, sourceId: Long)
    
    /**
     * Delete all explore books for a source.
     * 
     * @param sourceId The source ID
     */
    suspend fun deleteBySource(sourceId: Long)
    
    /**
     * Delete all explore books.
     */
    suspend fun deleteAll()
    
    /**
     * Cleanup old explore books to maintain the size limit.
     * Deletes the oldest books if count exceeds MAX_EXPLORE_BOOKS.
     */
    suspend fun cleanup()
    
    /**
     * Find an explore book by ID.
     * 
     * @param id The book ID
     * @return The explore book if found, null otherwise
     */
    suspend fun findById(id: Long): ExploreBook?
}
