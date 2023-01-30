package ireader.domain.usecases.local.delete_usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter


/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return chapterRepository.deleteChapter(chapter)
    }
}
