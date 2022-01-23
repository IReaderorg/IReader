package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository

/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return localChapterRepository.deleteChapterByChapter(chapter)
    }
}