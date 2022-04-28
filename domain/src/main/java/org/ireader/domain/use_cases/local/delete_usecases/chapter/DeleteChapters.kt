package org.ireader.domain.use_cases.local.delete_usecases.chapter

import org.ireader.common_data.repository.LocalChapterRepository
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

class DeleteChapters @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        return localChapterRepository.deleteChapters(chapters)
    }
}
