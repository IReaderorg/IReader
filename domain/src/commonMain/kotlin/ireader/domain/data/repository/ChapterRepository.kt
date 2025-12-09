package ireader.domain.data.repository

import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chapter data access operations.
 * 
 * Chapters represent individual sections or episodes of a book.
 * This repository provides methods for managing chapters including
 * CRUD operations, queries by book, and reactive subscriptions.
 * 
 * This interface extends both [ChapterReadRepository] and [ChapterWriteRepository]
 * for backward compatibility. New code should prefer depending on the
 * focused sub-interfaces when only read or write operations are needed.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
interface ChapterRepository : ChapterReadRepository, ChapterWriteRepository {

    /**
     * Subscribes to chapter changes by ID.
     * 
     * @param chapterId The unique identifier of the chapter
     * @return Flow emitting the chapter when it changes, or null if not found
     */
    override fun subscribeChapterById(
        chapterId: Long,
    ): Flow<Chapter?>

    /**
     * Finds a chapter by its unique identifier.
     * 
     * @param chapterId The unique identifier of the chapter
     * @return The chapter if found, null otherwise
     */
    override suspend fun findChapterById(
        chapterId: Long,
    ): Chapter?

    /**
     * Retrieves all chapters from the database.
     * 
     * @return List of all chapters
     */
    override suspend fun findAllChapters(): List<Chapter>

    /**
     * Retrieves all chapters for books in the user's library.
     * 
     * @return List of chapters from library books
     */
    override suspend fun findAllInLibraryChapter(): List<Chapter>

    /**
     * Retrieves all chapters for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return List of chapters belonging to the book
     */
    override suspend fun findChaptersByBookId(
        bookId: Long,
    ): List<Chapter>

    /**
     * Finds the last read chapter for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return The last read chapter if found, null otherwise
     */
    override suspend fun findLastReadChapter(bookId: Long): Chapter?
    
    /**
     * Subscribes to the last read chapter for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return Flow emitting the last read chapter when it changes
     */
    override suspend fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?>

    /**
     * Inserts a new chapter into the database.
     * 
     * @param chapter The chapter to insert
     * @return The ID of the inserted chapter
     */
    override suspend fun insertChapter(chapter: Chapter): Long

    /**
     * Inserts multiple chapters in a batch operation.
     * 
     * @param chapters List of chapters to insert
     * @return List of IDs for the inserted chapters
     */
    override suspend fun insertChapters(
            chapters: List<Chapter>,
    ): List<Long>

    /**
     * Deletes all chapters for a specific book.
     * 
     * @param bookId The unique identifier of the book
     */
    override suspend fun deleteChaptersByBookId(
        bookId: Long,
    )

    /**
     * Deletes multiple chapters in a batch operation.
     * 
     * @param chapters List of chapters to delete
     */
    override suspend fun deleteChapters(chapters: List<Chapter>)

    /**
     * Deletes a single chapter from the database.
     * 
     * @param chapter The chapter to delete
     */
    override suspend fun deleteChapter(
            chapter: Chapter,
    )

    /**
     * Deletes all chapters from the database.
     * WARNING: This operation cannot be undone.
     */
    override suspend fun deleteAllChapters()
    
    /**
     * Subscribes to chapter changes for a specific book.
     * 
     * @param bookId The unique identifier of the book
     * @return Flow emitting list of chapters when they change
     */
    override fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>>
    
    /**
     * Retrieves all chapters for a specific book WITH their full content.
     * This is a heavier query that includes chapter text content.
     * Use this for EPUB export or when you need the actual chapter text.
     * 
     * WARNING: This can cause OOM errors for books with many large chapters.
     * Use findChaptersByBookId() for listing chapters without content.
     * 
     * @param bookId The unique identifier of the book
     * @return List of chapters with their full content
     */
    override suspend fun findChaptersByBookIdWithContent(bookId: Long): List<Chapter>
}
