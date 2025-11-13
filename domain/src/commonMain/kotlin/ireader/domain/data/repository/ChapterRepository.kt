package ireader.domain.data.repository

import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chapter data access operations.
 * 
 * Chapters represent individual sections or episodes of a book.
 * This repository provides methods for managing chapters including
 * CRUD operations, queries by book, and reactive subscriptions.
 */
interface ChapterRepository {

    /**
     * Subscribes to chapter changes by ID.
     * 
     * @param chapterId The unique identifier of the chapter
     * @return Flow emitting the chapter when it changes, or null if not found
     */
    fun subscribeChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    /**
     * Finds a chapter by its unique identifier.
     * 
     * @param chapterId The unique identifier of the chapter
     * @return The chapter if found, null otherwise
     */
    suspend fun findChapterById(
        chapterId: Long,
    ): Chapter?

    /**
     * Retrieves all chapters from the database.
     * 
     * @return List of all chapters
     */
    suspend fun findAllChapters(): List<Chapter>

    /**
     * Retrieves all chapters for books in the user's library.
     * 
     * @return List of chapters from library books
     */
    suspend fun findAllInLibraryChapter(): List<Chapter>

    /**
     * Retrieves all chapters for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return List of chapters belonging to the book
     */
    suspend fun findChaptersByBookId(
        bookId: Long,
    ): List<Chapter>

    /**
     * Finds the last read chapter for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return The last read chapter if found, null otherwise
     */
    suspend fun findLastReadChapter(bookId: Long): Chapter?
    
    /**
     * Subscribes to the last read chapter for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return Flow emitting the last read chapter when it changes
     */
    suspend fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?>

    /**
     * Inserts a new chapter into the database.
     * 
     * @param chapter The chapter to insert
     * @return The ID of the inserted chapter
     */
    suspend fun insertChapter(chapter: Chapter): Long

    /**
     * Inserts multiple chapters in a batch operation.
     * 
     * @param chapters List of chapters to insert
     * @return List of IDs for the inserted chapters
     */
    suspend fun insertChapters(
            chapters: List<Chapter>,
    ): List<Long>

    /**
     * Deletes all chapters for a specific book.
     * 
     * @param bookId The unique identifier of the book
     */
    suspend fun deleteChaptersByBookId(
        bookId: Long,
    )

    /**
     * Deletes multiple chapters in a batch operation.
     * 
     * @param chapters List of chapters to delete
     */
    suspend fun deleteChapters(chapters: List<Chapter>)

    /**
     * Deletes a single chapter from the database.
     * 
     * @param chapter The chapter to delete
     */
    suspend fun deleteChapter(
            chapter: Chapter,
    )

    /**
     * Deletes all chapters from the database.
     * WARNING: This operation cannot be undone.
     */
    suspend fun deleteAllChapters()
    
    /**
     * Subscribes to chapter changes for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return Flow emitting list of chapters when they change
     */
    fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>>
}
