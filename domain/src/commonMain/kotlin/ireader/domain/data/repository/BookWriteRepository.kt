package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook

/**
 * Write-only repository interface for book data modification operations.
 * 
 * This interface provides methods for creating, updating, and deleting books.
 * Use this interface when a component needs to modify book data.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
interface BookWriteRepository {

    /**
     * Deletes multiple books from the database.
     * 
     * @param book List of books to delete
     */
    suspend fun deleteBooks(book: List<Book>)

    /**
     * Inserts books and their chapters in a single transaction.
     * 
     * @param books List of books to insert
     * @param chapters List of chapters to insert
     */
    suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)

    /**
     * Deletes a book by its unique identifier.
     * 
     * @param id The unique identifier of the book to delete
     */
    suspend fun deleteBookById(id: Long)

    /**
     * Deletes all books from the database.
     * WARNING: This operation cannot be undone.
     */
    suspend fun deleteAllBooks()
    
    /**
     * Deletes all books that are not in the user's library.
     */
    suspend fun deleteNotInLibraryBooks()

    /**
     * Updates an existing book in the database.
     * 
     * @param book The book with updated information
     */
    suspend fun updateBook(book: Book)
    
    /**
     * Updates a library book's favorite status.
     * 
     * @param book The library book to update
     * @param favorite Whether the book is marked as favorite
     */
    suspend fun updateBook(book: LibraryBook, favorite: Boolean)
    
    /**
     * Updates multiple books in a batch operation.
     * 
     * @param book List of books to update
     */
    suspend fun updateBook(book: List<Book>)
    
    /**
     * Inserts a new book or updates if it already exists.
     * 
     * @param book The book to upsert
     * @return The ID of the inserted or updated book
     */
    suspend fun upsert(book: Book): Long
    
    /**
     * Updates only the changed fields of a book.
     * 
     * @param book The book with partial updates
     * @return The ID of the updated book
     */
    suspend fun updatePartial(book: Book): Long
    
    /**
     * Inserts multiple books in a batch operation.
     * 
     * @param book List of books to insert
     * @return List of IDs for the inserted books
     */
    suspend fun insertBooks(book: List<Book>): List<Long>
    
    /**
     * Deletes a book by its key.
     * 
     * @param key The book's unique key
     */
    suspend fun delete(key: String)

    /**
     * Ensures all books have the default category assigned
     */
    suspend fun repairCategoryAssignments()
    
    /**
     * Update pin status for a book
     */
    suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int)
    
    /**
     * Update pinned order for a book
     */
    suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int)
    
    /**
     * Update archive status for a book
     */
    suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean)
}
