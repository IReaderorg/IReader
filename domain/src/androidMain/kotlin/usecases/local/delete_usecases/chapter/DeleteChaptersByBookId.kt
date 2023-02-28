package ireader.domain.usecases.local.delete_usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.utils.extensions.withIOContext


/**
 * Delete All Chapters that is have a bookId
 */

class DeleteChaptersByBookId(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(bookId: Long) {
        return withIOContext {
            return@withIOContext chapterRepository.deleteChaptersByBookId(bookId)
        }
    }
}
