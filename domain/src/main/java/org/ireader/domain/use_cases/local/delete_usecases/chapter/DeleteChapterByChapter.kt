package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import javax.inject.Inject

/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return localChapterRepository.deleteChapterByChapter(chapter)
    }
}