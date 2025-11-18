package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated LibraryRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential library management operations with proper
 * sorting, filtering, and organization capabilities.
 */
interface LibraryRepository {
    
    // Library retrieval with sorting and filtering
    suspend fun getLibraryBooks(
        sort: LibrarySort = LibrarySort.default,
        includeArchived: Boolean = false
    ): List<LibraryBook>
    
    fun getLibraryBooksAsFlow(
        sort: LibrarySort = LibrarySort.default,
        includeArchived: Boolean = false
    ): Flow<List<LibraryBook>>
    
    // Category-based library operations
    suspend fun getLibraryBooksByCategory(categoryId: Long): List<LibraryBook>
    fun getLibraryBooksByCategoryAsFlow(categoryId: Long): Flow<List<LibraryBook>>
    
    // Library statistics
    suspend fun getLibraryCount(): Int
    suspend fun getUnreadCount(): Int
    suspend fun getReadingCount(): Int
    
    // Library management
    suspend fun addToLibrary(bookId: Long): Boolean
    suspend fun removeFromLibrary(bookId: Long): Boolean
    suspend fun toggleFavorite(bookId: Long): Boolean
    
    // Bulk operations
    suspend fun addBooksToLibrary(bookIds: List<Long>): Boolean
    suspend fun removeBooksFromLibrary(bookIds: List<Long>): Boolean
    
    // Archive operations
    suspend fun archiveBook(bookId: Long): Boolean
    suspend fun unarchiveBook(bookId: Long): Boolean
    
    // Pin operations
    suspend fun pinBook(bookId: Long, order: Int): Boolean
    suspend fun unpinBook(bookId: Long): Boolean
}