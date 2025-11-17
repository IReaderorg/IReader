package ireader.domain.usecases.book

import ireader.domain.data.repository.consolidated.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.presentation.core.log.IReaderLog
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving chapter information following Mihon's pattern.
 * Provides both suspend and Flow-based access to chapter data.
 */
class GetChapters(
    private val chapterRepository: ChapterRepository,
) {
    /**
     * Get chapters by book ID as a suspend function
     */
    suspend fun await(bookId: Long): List<Chapter> {
        return try {
            chapterRepository.getChaptersByBookId(bookId)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get chapters for book: $bookId", e, "GetChapters")
            emptyList()
        }
    }

    /**
     * Subscribe to chapter changes by book ID as a Flow
     */
    fun subscribe(bookId: Long): Flow<List<Chapter>> {
        return chapterRepository.getChaptersByBookIdAsFlow(bookId)
    }

    /**
     * Get a single chapter by ID
     */
    suspend fun awaitChapter(id: Long): Chapter? {
        return try {
            chapterRepository.getChapterById(id)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get chapter by id: $id", e, "GetChapters")
            null
        }
    }

    /**
     * Subscribe to a single chapter by ID
     */
    fun subscribeChapter(id: Long): Flow<Chapter?> {
        return chapterRepository.getChapterByIdAsFlow(id)
    }

    /**
     * Get the last read chapter for a book
     */
    suspend fun awaitLastRead(bookId: Long): Chapter? {
        return try {
            chapterRepository.getLastReadChapter(bookId)
        } catch (e: Exception) {
            IReaderLog.error("Failed to get last read chapter for book: $bookId", e, "GetChapters")
            null
        }
    }

    /**
     * Subscribe to the last read chapter for a book
     */
    fun subscribeLastRead(bookId: Long): Flow<Chapter?> {
        return chapterRepository.getLastReadChapterAsFlow(bookId)
    }
}