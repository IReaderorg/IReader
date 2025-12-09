package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for book data access operations.
 * 
 * This repository provides methods for managing books in the application,
 * including CRUD operations, library management, and reactive queries.
 * 
 * All suspend functions should be called from a coroutine context.
 * Flow-based functions provide reactive updates when data changes.
 * 
 * This interface extends both [BookReadRepository] and [BookWriteRepository]
 * for backward compatibility. New code should prefer depending on the
 * focused sub-interfaces when only read or write operations are needed.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
interface BookRepository : BookReadRepository, BookWriteRepository {

    /**
     * Retrieves all books from the database.
     * 
     * @return List of all books
     */
    override suspend fun findAllBooks(): List<Book>

    /**
     * Subscribes to book changes by ID.
     * 
     * @param id The unique identifier of the book
     * @return Flow emitting the book when it changes, or null if not found
     */
    override fun subscribeBookById(id: Long): Flow<Book?>

    /**
     * Finds a book by its unique identifier.
     * 
     * @param id The unique identifier of the book
     * @return The book if found, null otherwise
     */
    override suspend fun findBookById(id: Long): Book?

    /**
     * Finds a book by its key and source ID.
     * 
     * @param key The book's unique key within the source
     * @param sourceId The source identifier
     * @return The book if found, null otherwise
     */
    override suspend fun find(key: String, sourceId: Long): Book?

    /**
     * Retrieves all books in the library with sorting and filtering.
     * 
     * @param sortType The sorting method to apply
     * @param isAsc Whether to sort in ascending order
     * @param unreadFilter Whether to filter for unread books only
     * @return List of library books matching the criteria
     */
    override suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): List<Book>

    /**
     * Finds a single book by its key.
     * 
     * @param key The book's unique key
     * @return The book if found, null otherwise
     */
    override suspend fun findBookByKey(key: String): Book?

    /**
     * Finds all books matching a specific key.
     * 
     * @param key The book's key to search for
     * @return List of books with matching keys
     */
    override suspend fun findBooksByKey(key: String): List<Book>

    /**
     * Subscribes to books matching a key and title.
     * 
     * @param key The book's key
     * @param title The book's title
     * @return Flow emitting list of matching books when they change
     */
    override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>

    /**
     * Deletes multiple books from the database.
     * 
     * @param book List of books to delete
     */
    override suspend fun deleteBooks(book: List<Book>)

    /**
     * Inserts books and their chapters in a single transaction.
     * 
     * @param books List of books to insert
     * @param chapters List of chapters to insert
     */
    override suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)

    /**
     * Deletes a book by its unique identifier.
     * 
     * @param id The unique identifier of the book to delete
     */
    override suspend fun deleteBookById(id: Long)
    
    /**
     * Finds a duplicate book by title and source ID.
     * 
     * @param title The book's title
     * @param sourceId The source identifier
     * @return The duplicate book if found, null otherwise
     */
    override suspend fun findDuplicateBook(title: String, sourceId: Long): Book?

    /**
     * Deletes all books from the database.
     * WARNING: This operation cannot be undone.
     */
    override suspend fun deleteAllBooks()
    
    /**
     * Deletes all books that are not in the user's library.
     */
    override suspend fun deleteNotInLibraryBooks()



    /**
     * Updates an existing book in the database.
     * 
     * @param book The book with updated information
     */
    override suspend fun updateBook(book: Book)
    
    /**
     * Updates a library book's favorite status.
     * 
     * @param book The library book to update
     * @param favorite Whether the book is marked as favorite
     */
    override suspend fun updateBook(book: LibraryBook, favorite: Boolean)
    
    /**
     * Updates multiple books in a batch operation.
     * 
     * @param book List of books to update
     */
    override suspend fun updateBook(book: List<Book>)
    
    /**
     * Inserts a new book or updates if it already exists.
     * 
     * @param book The book to upsert
     * @return The ID of the inserted or updated book
     */
    override suspend fun upsert(book: Book): Long
    
    /**
     * Updates only the changed fields of a book.
     * 
     * @param book The book with partial updates
     * @return The ID of the updated book
     */
    override suspend fun updatePartial(book: Book): Long
    
    /**
     * Inserts multiple books in a batch operation.
     * 
     * @param book List of books to insert
     * @return List of IDs for the inserted books
     */
    override suspend fun insertBooks(book: List<Book>): List<Long>
    
    /**
     * Deletes a book by its key.
     * 
     * @param key The book's unique key
     */
    override suspend fun delete(key: String)

    /**
     * Retrieves source IDs for all books marked as favorites.
     * 
     * @return List of source IDs that have favorite books
     */
    override suspend fun findFavoriteSourceIds(): List<Long>
    
    /**
     * Ensures all books have the default category assigned
     */
    override suspend fun repairCategoryAssignments()
    
    /**
     * Update pin status for a book
     */
    override suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int)
    
    /**
     * Update pinned order for a book
     */
    override suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int)
    
    /**
     * Get the maximum pinned order value
     */
    override suspend fun getMaxPinnedOrder(): Int
    
    /**
     * Update archive status for a book
     */
    override suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean)
}
