package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

/**
 * Delete a chapter @DELETE
 */
class DeleteChapterByChapter @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return chapterRepository.deleteChapter(chapter)
    }
}
