package ireader.domain.usecases.local.delete_usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import org.koin.core.annotation.Factory

/**
 * Delete All Chapters that is have a bookId
 */
@Factory
class DeleteChaptersByBookId(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return ireader.common.extensions.withIOContext {
            return@withIOContext chapterRepository.deleteChaptersByBookId(bookId)
        }
    }
}
