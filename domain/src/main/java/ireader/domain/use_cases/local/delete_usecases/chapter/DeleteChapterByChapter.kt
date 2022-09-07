package ireader.domain.use_cases.local.delete_usecases.chapter

import ireader.common.data.repository.ChapterRepository
import ireader.common.models.entities.Chapter


/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return chapterRepository.deleteChapter(chapter)
    }
}
