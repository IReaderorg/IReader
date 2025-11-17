package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.updates.BookUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated BookRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential book operations with clear, focused methods
 * that follow Mihon's proven patterns for maintainability and testability.
 */
interface BookRepository {
    
    // Single book operations
    suspend fun getBookById(id: Long): Book?
    fun getBookByIdAsFlow(id: Long): Flow<Book?>
    suspend fun getBookByUrlAndSourceId(url: String, sourceId: Long): Book?
    fun getBookByUrlAndSourceIdAsFlow(url: String, sourceId: Long): Flow<Book?>
    
    // Library operations
    suspend fun getFavorites(): List<Book>
    fun getFavoritesAsFlow(): Flow<List<Book>>
    suspend fun getLibraryBooks(): List<LibraryBook>
    fun getLibraryBooksAsFlow(): Flow<List<LibraryBook>>
    
    // Duplicate detection
    suspend fun getDuplicateLibraryBooks(id: Long, title: String): List<Book>
    
    // Category management
    suspend fun setBookCategories(bookId: Long, categoryIds: List<Long>)
    
    // Update operations following Mihon's pattern
    suspend fun update(update: BookUpdate): Boolean
    suspend fun updateAll(updates: List<BookUpdate>): Boolean
    
    // Network book insertion
    suspend fun insertNetworkBooks(books: List<Book>): List<Book>
    
    // Batch operations
    suspend fun deleteBooks(bookIds: List<Long>): Boolean
    suspend fun deleteNotInLibraryBooks(): Boolean
}