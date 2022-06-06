package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

class DeleteChapters @Inject constructor(private val chapterRepository: org.ireader.common_data.repository.ChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        return chapterRepository.deleteChapters(chapters)
    }
}
