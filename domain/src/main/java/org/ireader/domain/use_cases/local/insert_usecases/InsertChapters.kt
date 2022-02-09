package org.ireader.infinity.core.domain.use_cases.local.insert_usecases

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository

class InsertChapters(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>) {
        localChapterRepository.insertChapters(chapters)
    }
}