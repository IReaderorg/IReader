package ireader.domain.usecases.chapter.controller

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Use case interface for retrieving chapters.
 * Provides reactive subscriptions and one-shot queries for chapter data.
 */
interface GetChaptersUseCase {
    /**
     * Subscribe to chapter changes for a specific book.
     * Emits a new list whenever chapters are added, removed, or modified.
     *
     * @param bookId The unique identifier of the book
     * @return Flow emitting list of chapters when they change
     */
    fun subscribeByBookId(bookId: Long): Flow<List<Chapter>>

    /**
     * Find all chapters for a specific book (one-shot query).
     *
     * @param bookId The unique identifier of the book
     * @return List of chapters belonging to the book
     */
    suspend fun findByBookId(bookId: Long): List<Chapter>

    /**
     * Find a specific chapter by its ID.
     *
     * @param chapterId The unique identifier of the chapter
     * @return The chapter if found, null otherwise
     */
    suspend fun findById(chapterId: Long): Chapter?
}

/**
 * Default implementation of [GetChaptersUseCase].
 * Delegates to [ChapterRepository] for data access.
 */
class GetChaptersUseCaseImpl(
    private val chapterRepository: ChapterRepository
) : GetChaptersUseCase {

    override fun subscribeByBookId(bookId: Long): Flow<List<Chapter>> {
        return chapterRepository.subscribeChaptersByBookId(bookId)
    }

    override suspend fun findByBookId(bookId: Long): List<Chapter> {
        return chapterRepository.findChaptersByBookId(bookId)
    }

    override suspend fun findById(chapterId: Long): Chapter? {
        return chapterRepository.findChapterById(chapterId)
    }
}
