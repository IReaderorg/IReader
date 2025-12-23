package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    suspend fun findAll(sort: LibrarySort, includeArchived: Boolean = false): List<LibraryBook>
    
    /**
     * Get paginated library books from database.
     * This is more efficient for large libraries as it only loads the requested page.
     * 
     * @param sort The sort order
     * @param limit Maximum number of books to return
     * @param offset Number of books to skip
     * @param includeArchived Whether to include archived books
     * @return List of library books for the requested page
     */
    suspend fun findAllPaginated(
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): List<LibraryBook>
    
    /**
     * Get the total count of library books.
     * Used for pagination to know total pages.
     */
    suspend fun getLibraryCount(includeArchived: Boolean = false): Int
    
    /**
     * Get paginated library books for a specific category from database.
     * Uses the existing getLibraryByCategoryPaginated SQL query.
     */
    suspend fun findByCategoryPaginated(
        categoryId: Long,
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): List<LibraryBook>
    
    /**
     * Get the total count of library books in a specific category.
     */
    suspend fun getLibraryCountByCategory(categoryId: Long, includeArchived: Boolean = false): Int
    
    /**
     * Get paginated uncategorized library books from database.
     */
    suspend fun findUncategorizedPaginated(
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): List<LibraryBook>
    
    /**
     * Get the total count of uncategorized library books.
     */
    suspend fun getUncategorizedCount(includeArchived: Boolean = false): Int
    fun subscribe(sort: LibrarySort, includeArchived: Boolean = false): Flow<List<LibraryBook>>
    
    /**
     * Fast subscribe that doesn't load chapter counts initially.
     * Use this for low-end devices to improve initial load time.
     * Chapter counts will be 0 and should be loaded lazily if needed.
     */
    fun subscribeFast(sort: LibrarySort, includeArchived: Boolean = false): Flow<List<LibraryBook>>
    
    /**
     * Ultra-fast direct query (no flow) for initial load.
     * Returns immediately with cached data, then emits fresh data.
     * Best for low-end devices where flow subscription is slow.
     */
    suspend fun findAllFast(sort: LibrarySort, includeArchived: Boolean = false): List<LibraryBook>

    suspend fun findDownloadedBooks(): List<Book>

    suspend fun findFavorites(): List<Book>
    
    /**
     * Search library books by title with pagination.
     * 
     * @param query The search query (matches title)
     * @param sort The sort order
     * @param limit Maximum number of books to return
     * @param offset Number of books to skip
     * @param includeArchived Whether to include archived books
     * @return List of matching library books
     */
    suspend fun searchPaginated(
        query: String,
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): List<LibraryBook>
    
    /**
     * Get the total count of library books matching a search query.
     */
    suspend fun getSearchCount(query: String, includeArchived: Boolean = false): Int
}
