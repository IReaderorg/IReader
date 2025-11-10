package ireader.domain.usecases.history

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.LastReadInfo

/**
 * Use case to get the last read novel information
 * Queries reading history for the most recent chapter and returns complete information
 */
class GetLastReadNovelUseCase(
    private val historyRepository: HistoryRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) {
    /**
     * Gets the last read novel information
     * @return LastReadInfo if there is reading history, null otherwise
     */
    suspend operator fun invoke(): LastReadInfo? {
        // Get all histories sorted by readAt descending
        val histories = historyRepository.findHistories()
            .filter { it.readAt != null && it.readAt > 0 }
            .sortedByDescending { it.readAt }
        
        if (histories.isEmpty()) {
            return null
        }
        
        // Get the most recent history entry
        val lastHistory = histories.first()
        
        // Get chapter details
        val chapter = chapterRepository.findChapterById(lastHistory.chapterId) ?: return null
        
        // Get book details
        val book = bookRepository.findBookById(chapter.bookId) ?: return null
        
        return LastReadInfo(
            novelId = book.id,
            novelTitle = book.title,
            coverUrl = book.customCover.ifEmpty { book.cover },
            chapterId = chapter.id,
            chapterNumber = chapter.number,
            chapterTitle = chapter.name,
            progressPercent = lastHistory.progress,
            scrollPosition = chapter.lastPageRead,
            lastReadAt = lastHistory.readAt ?: 0L
        )
    }
}
