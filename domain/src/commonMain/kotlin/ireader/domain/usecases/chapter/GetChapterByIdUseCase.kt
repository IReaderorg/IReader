package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a chapter by its ID
 */
class GetChapterByIdUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Get chapter by ID as a one-time operation
     */
    suspend operator fun invoke(chapterId: Long): Chapter? {
        return chapterRepository.findChapterById(chapterId)
    }
    
    /**
     * Subscribe to chapter changes by ID
     */
    fun subscribe(chapterId: Long): Flow<Chapter?> {
        return chapterRepository.subscribeChapterById(chapterId)
    }
}
