package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving chapters by book ID
 */
class GetChaptersByBookIdUseCase(
    private val chapterRepository: ChapterRepository
) {
    /**
     * Get chapters for a book as a one-time operation
     */
    suspend operator fun invoke(bookId: Long): List<Chapter> {
        return chapterRepository.findChaptersByBookId(bookId)
    }
    
    /**
     * Subscribe to chapter changes for a book
     */
    fun subscribe(bookId: Long): Flow<List<Chapter>> {
        return chapterRepository.subscribeChaptersByBookId(bookId)
    }
}
