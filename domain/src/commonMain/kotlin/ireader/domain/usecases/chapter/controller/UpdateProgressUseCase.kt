package ireader.domain.usecases.chapter.controller

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case interface for updating reading progress.
 * Handles last read chapter tracking and paragraph position persistence.
 */
interface UpdateProgressUseCase {
    /**
     * Update the last read chapter for a book.
     * Records the chapter as the most recently read and marks it as read.
     *
     * @param bookId The unique identifier of the book
     * @param chapterId The unique identifier of the chapter
     */
    suspend fun updateLastRead(bookId: Long, chapterId: Long)

    /**
     * Update the paragraph index (reading position) for a chapter.
     *
     * @param chapterId The unique identifier of the chapter
     * @param paragraphIndex The current paragraph index
     */
    suspend fun updateParagraphIndex(chapterId: Long, paragraphIndex: Int)

    /**
     * Subscribe to the last read chapter ID for a book.
     *
     * @param bookId The unique identifier of the book
     * @return Flow emitting the last read chapter ID when it changes
     */
    fun subscribeLastRead(bookId: Long): Flow<Long?>
}

/**
 * Default implementation of [UpdateProgressUseCase].
 * Delegates to [HistoryRepository] and [ChapterRepository] for persistence.
 */
class UpdateProgressUseCaseImpl(
    private val historyRepository: HistoryRepository,
    private val chapterRepository: ChapterRepository,
    private val uiPreferences: UiPreferences
) : UpdateProgressUseCase {

    override suspend fun updateLastRead(bookId: Long, chapterId: Long) {
        // Respect incognito mode
        if (uiPreferences.incognitoMode().get()) {
            return
        }

        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        val existingHistory = historyRepository.findHistoryByChapterId(chapterId)

        // Mark chapter as read
        chapterRepository.insertChapter(
            chapter.copy(read = true)
        )

        // Update history
        historyRepository.insertHistory(
            History(
                id = existingHistory?.id ?: 0,
                chapterId = chapterId,
                readAt = currentTimeToLong(),
                readDuration = existingHistory?.readDuration ?: 0
            )
        )
    }

    override suspend fun updateParagraphIndex(chapterId: Long, paragraphIndex: Int) {
        // Respect incognito mode
        if (uiPreferences.incognitoMode().get()) {
            return
        }

        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        
        // Update the lastPageRead field which stores paragraph index
        chapterRepository.insertChapter(
            chapter.copy(lastPageRead = paragraphIndex.toLong())
        )
    }

    override fun subscribeLastRead(bookId: Long): Flow<Long?> {
        return historyRepository.subscribeHistoryByBookId(bookId)
            .map { history -> history?.chapterId }
    }
}
