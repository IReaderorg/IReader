package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the last read chapter for a book
 */
class GetLastReadChapterUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Get last read chapter as a one-time operation
     */
    suspend operator fun invoke(bookId: Long): Chapter? {
        return chapterRepository.findLastReadChapter(bookId)
    }
    
    /**
     * Subscribe to last read chapter changes
     */
    suspend fun subscribe(bookId: Long): Flow<Chapter?> {
        return chapterRepository.subscribeLastReadChapter(bookId)
    }
}
