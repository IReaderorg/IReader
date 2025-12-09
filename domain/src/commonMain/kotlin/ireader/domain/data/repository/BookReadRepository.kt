package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

/**
 * Read-only repository interface for book data access operations.
 * 
 * This interface provides methods for querying books without modification.
 * Use this interface when a component only needs to read book data.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
interface BookReadRepository {

    /**
     * Retrieves all books from the database.
     * 
     * @return List of all books
     */
    suspend fun findAllBooks(): List<Book>

    /**
     * Subscribes to book changes by ID.
     * 
     * @param id The unique identifier of the book
     * @return Flow emitting the book when it changes, or null if not found
     */
    fun subscribeBookById(id: Long): Flow<Book?>

    /**
     * Finds a book by its unique identifier.
     * 
     * @param id The unique identifier of the book
     * @return The book if found, null otherwise
     */
    suspend fun findBookById(id: Long): Book?

    /**
     * Finds a book by its key and source ID.
     * 
     * @param key The book's unique key within the source
     * @param sourceId The source identifier
     * @return The book if found, null otherwise
     */
    suspend fun find(key: String, sourceId: Long): Book?

    /**
     * Retrieves all books in the library with sorting and filtering.
     * 
     * @param sortType The sorting method to apply
     * @param isAsc Whether to sort in ascending order
     * @param unreadFilter Whether to filter for unread books only
     * @return List of library books matching the criteria
     */
    suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean = false,
        unreadFilter: Boolean = false,
    ): List<Book>

    /**
     * Finds a single book by its key.
     * 
     * @param key The book's unique key
     * @return The book if found, null otherwise
     */
    suspend fun findBookByKey(key: String): Book?

    /**
     * Finds all books matching a specific key.
     * 
     * @param key The book's key to search for
     * @return List of books with matching keys
     */
    suspend fun findBooksByKey(key: String): List<Book>

    /**
     * Subscribes to books matching a key and title.
     * 
     * @param key The book's key
     * @param title The book's title
     * @return Flow emitting list of matching books when they change
     */
    suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>

    /**
     * Finds a duplicate book by title and source ID.
     * 
     * @param title The book's title
     * @param sourceId The source identifier
     * @return The duplicate book if found, null otherwise
     */
    suspend fun findDuplicateBook(title: String, sourceId: Long): Book?

    /**
     * Retrieves source IDs for all books marked as favorites.
     * 
     * @return List of source IDs that have favorite books
     */
    suspend fun findFavoriteSourceIds(): List<Long>

    /**
     * Get the maximum pinned order value
     */
    suspend fun getMaxPinnedOrder(): Int
}
