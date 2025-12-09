package ireader.domain.data.repository

import ireader.domain.models.entities.Chapter

/**
 * Write-only repository interface for chapter data modification operations.
 * 
 * This interface provides methods for creating, updating, and deleting chapters.
 * Use this interface when a component needs to modify chapter data.
 * 
 * Requirements: 9.1, 9.2, 9.3
 */
interface ChapterWriteRepository {

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
}
