package org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository

class DeleteChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        return localChapterRepository.deleteChapters(chapters)
    }
}