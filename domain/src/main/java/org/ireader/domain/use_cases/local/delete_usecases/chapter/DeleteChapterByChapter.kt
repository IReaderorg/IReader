package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return localChapterRepository.deleteChapterByChapter(chapter)
    }
}